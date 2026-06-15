package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Playlist
import com.example.data.model.PlaylistWithTracks
import com.example.data.model.Track
import com.example.data.repository.MusicRepository
import com.example.player.AudioPlayerManager
import com.example.player.EqualizerBand
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MusicRepository
    private val playerManager: AudioPlayerManager

    // Expose DB flows
    val allTracks: StateFlow<List<Track>>
    val allPlaylists: StateFlow<List<Playlist>>
    val playlistsWithTracks: StateFlow<List<PlaylistWithTracks>>

    // Player state flows
    val currentTrack: StateFlow<Track?>
    val isPlaying: StateFlow<Boolean>
    val playbackProgress: StateFlow<Long>
    val isShuffle: StateFlow<Boolean>
    val isRepeat: StateFlow<Boolean>
    val isSingleTrackLoop: StateFlow<Boolean>

    // Equalizer state flows
    val bands: StateFlow<List<EqualizerBand>>
    val selectedPreset: StateFlow<String>
    val presets: List<String>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MusicRepository(database.musicDao())
        playerManager = AudioPlayerManager(application)

        // Initialize DB observer states
        allTracks = repository.allTracks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allPlaylists = repository.allPlaylists
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        playlistsWithTracks = repository.playlistsWithTracks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Connect player states
        currentTrack = playerManager.currentTrack
        isPlaying = playerManager.isPlaying
        playbackProgress = playerManager.playbackProgress
        isShuffle = playerManager.isShuffle
        isRepeat = playerManager.isRepeat
        isSingleTrackLoop = playerManager.isSingleTrackLoop

        // Connect Equalizer states
        bands = playerManager.bands
        selectedPreset = playerManager.selectedPreset
        presets = playerManager.getPresets()

        // Populate demo tracks on launch
        viewModelScope.launch {
            repository.populateDemoTracks()
        }
    }

    // Playback control functions
    fun playTrack(track: Track, fromTracks: List<Track>) {
        val index = fromTracks.indexOfFirst { it.id == track.id }
        playerManager.setQueue(fromTracks, if (index != -1) index else 0)
    }

    fun playPlaylist(playlistWithTracks: PlaylistWithTracks) {
        if (playlistWithTracks.tracks.isNotEmpty()) {
            playerManager.setQueue(playlistWithTracks.tracks, 0)
        }
    }

    fun togglePlayPause() = playerManager.togglePlayPause()
    fun playNext() = playerManager.playNext()
    fun playPrevious() = playerManager.playPrevious()
    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)
    fun toggleShuffle() = playerManager.toggleShuffle()
    fun toggleRepeat() = playerManager.toggleRepeat()
    fun toggleSingleEmptyTrackLoop() = playerManager.toggleSingleTrackLoop()

    // Database manipulation functions
    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.insertPlaylist(Playlist(name = name, description = description))
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrack(title: String, artist: String, url: String, durationMs: Long = 240000L) {
        viewModelScope.launch {
            repository.insertTrack(
                Track(
                    title = title,
                    artist = artist,
                    url = url,
                    duration = durationMs,
                    isDemo = false
                )
            )
        }
    }

    fun deleteTrack(trackId: Long) {
        viewModelScope.launch {
            repository.deleteTrack(trackId)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    // Equalizer Controls
    fun setEqualizerBandLevel(bandId: Short, levelMb: Short) {
        playerManager.setBandLevel(bandId, levelMb)
    }

    fun applyEqualizerPreset(presetName: String) {
        playerManager.applyPreset(presetName)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
