package com.example.data.repository

import com.example.data.local.MusicDao
import com.example.data.model.Track
import com.example.data.model.Playlist
import com.example.data.model.PlaylistTrackCrossRef
import com.example.data.model.PlaylistWithTracks
import kotlinx.coroutines.flow.Flow

class MusicRepository(private val musicDao: MusicDao) {

    val allTracks: Flow<List<Track>> = musicDao.getAllTracks()
    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()
    val playlistsWithTracks: Flow<List<PlaylistWithTracks>> = musicDao.getPlaylistsWithTracks()

    fun getPlaylistWithTracks(playlistId: Long): Flow<PlaylistWithTracks?> {
        return musicDao.getPlaylistWithTracks(playlistId)
    }

    suspend fun insertTrack(track: Track): Long {
        return musicDao.insertTrack(track)
    }

    suspend fun deleteTrack(trackId: Long) {
        musicDao.deleteTrack(trackId)
    }

    suspend fun getTracksCount(): Int {
        return musicDao.getTracksCount()
    }

    suspend fun insertPlaylist(playlist: Playlist): Long {
        return musicDao.insertPlaylist(playlist)
    }

    suspend fun deletePlaylist(playlistId: Long) {
        musicDao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        musicDao.insertPlaylistTrackCrossRef(PlaylistTrackCrossRef(playlistId, trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        musicDao.deletePlaylistTrackCrossRef(playlistId, trackId)
    }

    // Helper to populate default track listings if empty
    suspend fun populateDemoTracks() {
        if (getTracksCount() == 0) {
            val demoTracks = listOf(
                Track(
                    title = "Ambient Chill Out",
                    artist = "SoundHelix Band",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    duration = 372000, // 6:12
                    isDemo = true
                ),
                Track(
                    title = "Future Beat Dream",
                    artist = "SoundHelix Band",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    duration = 423000, // 7:03
                    isDemo = true
                ),
                Track(
                    title = "Electric Echoes",
                    artist = "SoundHelix Band",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    duration = 302000, // 5:02
                    isDemo = true
                ),
                Track(
                    title = "Velvet Grooves",
                    artist = "SoundHelix Band",
                    url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    duration = 318000, // 5:18
                    isDemo = true
                )
            )
            for (track in demoTracks) {
                insertTrack(track)
            }

            // Create a default playlist and include the first two tracks
            val defaultPlaylistId = insertPlaylist(
                Playlist(name = "Favoritas", description = "Minhas músicas prediletas")
            )
            val allInsertedTracks = listOf(1L, 2L)
            for (trackId in allInsertedTracks) {
                addTrackToPlaylist(defaultPlaylistId, trackId)
            }
        }
    }
}
