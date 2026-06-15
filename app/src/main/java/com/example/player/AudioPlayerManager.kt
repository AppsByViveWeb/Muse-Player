package com.example.player

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.util.Log
import com.example.data.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class EqualizerBand(
    val id: Short,
    val centerFrequency: Int, // in Hz
    val minLevel: Short,      // in millibels (usually -1500)
    val maxLevel: Short,      // in millibels (usually 1500)
    val currentLevel: Short   // in millibels
)

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Tracks currently in queue
    private var trackQueue: List<Track> = emptyList()
    private var currentTrackIndex = -1

    // Playback state flows
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playbackProgress = MutableStateFlow(0L)
    val playbackProgress: StateFlow<Long> = _playbackProgress

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle

    private val _isRepeat = MutableStateFlow(false) // loops entire list/current track
    val isRepeat: StateFlow<Boolean> = _isRepeat

    private val _isSingleTrackLoop = MutableStateFlow(false) // loop same song
    val isSingleTrackLoop: StateFlow<Boolean> = _isSingleTrackLoop

    // Equalizer state flows
    private val _bands = MutableStateFlow<List<EqualizerBand>>(emptyList())
    val bands: StateFlow<List<EqualizerBand>> = _bands

    private val _selectedPreset = MutableStateFlow("Flat")
    val selectedPreset: StateFlow<String> = _selectedPreset

    init {
        initEqualizerFallback()
    }

    private fun initEqualizerFallback() {
        // Create 5 standard equalizer bands as fallback (simulated / default)
        val defaultBands = listOf(
            EqualizerBand(0, 60, -1500, 1500, 0),
            EqualizerBand(1, 230, -1500, 1500, 0),
            EqualizerBand(2, 910, -1500, 1500, 0),
            EqualizerBand(3, 4000, -1500, 1500, 0),
            EqualizerBand(4, 14000, -1500, 1500, 0)
        )
        _bands.value = defaultBands
    }

    private fun setupRealEqualizer(audioSessionId: Int) {
        try {
            // Release prior equalizer if any
            equalizer?.release()
            equalizer = null

            val eq = Equalizer(0, audioSessionId)
            eq.enabled = true
            val numBands = eq.numberOfBands
            val levelRange = eq.bandLevelRange // [min, max]

            val minL = levelRange[0]
            val maxL = levelRange[1]

            val realBands = ArrayList<EqualizerBand>()
            for (i in 0 until numBands) {
                val bandId = i.toShort()
                val centerFreqHz = eq.getCenterFreq(bandId) / 1000 // mHz to Hz
                val currentL = eq.getBandLevel(bandId)
                realBands.add(EqualizerBand(bandId, centerFreqHz, minL, maxL, currentL))
            }
            _bands.value = realBands
            equalizer = eq
            applyPresetDirectly(_selectedPreset.value)
            Log.d("MusicPlayer", "Real Equalizer initialized with $numBands bands.")
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Failed to initialize real equalizer, keeping fallback: ${e.message}")
            // Fallback stays active
        }
    }

    fun setQueue(tracks: List<Track>, startWithIndex: Int = 0) {
        trackQueue = tracks
        if (tracks.isNotEmpty() && startWithIndex in tracks.indices) {
            playTrackAtIndex(startWithIndex)
        }
    }

    fun playTrack(track: Track) {
        val index = trackQueue.indexOfFirst { it.id == track.id }
        if (index != -1) {
            playTrackAtIndex(index)
        } else {
            trackQueue = listOf(track)
            playTrackAtIndex(0)
        }
    }

    private fun playTrackAtIndex(index: Int) {
        if (index !in trackQueue.indices) return
        currentTrackIndex = index
        val targetTrack = trackQueue[index]
        _currentTrack.value = targetTrack

        stopProgressTracker()

        try {
            mediaPlayer?.release()
            mediaPlayer = null

            val mp = MediaPlayer()
            mp.setDataSource(targetTrack.url)
            mp.setOnPreparedListener { player ->
                player.start()
                _isPlaying.value = true
                setupRealEqualizer(player.audioSessionId)
                startProgressTracker()
            }
            mp.setOnCompletionListener {
                handlePlaybackCompletion()
            }
            mp.setOnErrorListener { _, what, extra ->
                Log.e("MusicPlayer", "MediaPlayer Error: what=$what, extra=$extra")
                playNext() // Skip broken links
                true
            }
            _playbackProgress.value = 0L
            mediaPlayer = mp
            mp.prepareAsync()
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error preparing track: ${e.message}")
            // Attempt to jump to next
            playNext()
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            _isPlaying.value = false
            stopProgressTracker()
        } else {
            mp.start()
            _isPlaying.value = true
            startProgressTracker()
        }
    }

    fun playPrevious() {
        if (trackQueue.isEmpty()) return
        var prevIndex = currentTrackIndex - 1
        if (prevIndex < 0) {
            prevIndex = if (_isRepeat.value) trackQueue.size - 1 else 0
        }
        playTrackAtIndex(prevIndex)
    }

    fun playNext() {
        if (trackQueue.isEmpty()) return
        if (_isShuffle.value) {
            val randomIndex = trackQueue.indices.random()
            playTrackAtIndex(randomIndex)
            return
        }

        var nextIndex = currentTrackIndex + 1
        if (nextIndex >= trackQueue.size) {
            nextIndex = if (_isRepeat.value) 0 else -1
        }

        if (nextIndex != -1) {
            playTrackAtIndex(nextIndex)
        } else {
            // End of queue and repeat is off
            _isPlaying.value = false
            mediaPlayer?.pause()
            stopProgressTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let {
            it.seekTo(positionMs.toInt())
            _playbackProgress.value = positionMs
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    fun toggleSingleTrackLoop() {
        _isSingleTrackLoop.value = !_isSingleTrackLoop.value
    }

    private fun handlePlaybackCompletion() {
        if (_isSingleTrackLoop.value) {
            // Loop same song
            seekTo(0L)
            mediaPlayer?.start()
            _isPlaying.value = true
            startProgressTracker()
        } else {
            playNext()
        }
    }

    private fun startProgressTracker() {
        progressJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        _playbackProgress.value = it.currentPosition.toLong()
                    }
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    // Equalizer Band Controls
    fun setBandLevel(bandId: Short, levelMb: Short) {
        _bands.value = _bands.value.map { band ->
            if (band.id == bandId) {
                // Apply update locally to state
                band.copy(currentLevel = levelMb)
            } else {
                band
            }
        }

        // Apply to Android Equalizer object if initialized
        try {
            equalizer?.setBandLevel(bandId, levelMb)
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error writing band level to hardware equalizer: ${e.message}")
        }
        _selectedPreset.value = "Custom"
    }

    // Preset configurations mapping (Standard millibel levels for 5 bands)
    private val presetLevels = mapOf(
        "Flat" to listOf<Short>(0, 0, 0, 0, 0),
        "Rock" to listOf<Short>(500, 300, -200, 400, 600),
        "Pop" to listOf<Short>(-200, 100, 500, 200, -300),
        "Jazz" to listOf<Short>(400, 200, -300, 300, -100),
        "Clássica" to listOf<Short>(500, 300, -100, 200, 400),
        "Reforço de Graves" to listOf<Short>(900, 450, 0, 0, -200)
    )

    fun getPresets(): List<String> = presetLevels.keys.toList()

    fun applyPreset(presetName: String) {
        _selectedPreset.value = presetName
        applyPresetDirectly(presetName)
    }

    private fun applyPresetDirectly(presetName: String) {
        val levels = presetLevels[presetName] ?: return
        val currentBands = _bands.value
        _bands.value = currentBands.mapIndexed { index, band ->
            val level = if (index in levels.indices) levels[index] else 0.toShort()
            try {
                equalizer?.setBandLevel(band.id, level)
            } catch (e: Exception) {
                // Ignore failure (e.g., emulator/fallback state)
            }
            band.copy(currentLevel = level)
        }
    }

    fun release() {
        stopProgressTracker()
        scope.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        equalizer?.release()
        equalizer = null
    }
}
