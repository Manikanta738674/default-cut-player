package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM tracks")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1")
    fun getFavoriteTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isOffline = 1 OR isDownloaded = 1")
    fun getOfflineTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isRadio = 1")
    fun getRadioTracks(): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Query("UPDATE tracks SET isFavorite = :isFav WHERE id = :trackId")
    suspend fun updateFavorite(trackId: String, isFav: Boolean)

    @Query("UPDATE tracks SET isDownloaded = :isDown WHERE id = :trackId")
    suspend fun updateDownloadStatus(trackId: String, isDown: Boolean)

    // Playlist Queries
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Int)

    // Cross reference for Playlist Songs
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistTrackRef(ref: PlaylistTrackRef)

    @Query("DELETE FROM playlist_track_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Int, trackId: String)

    @Query("""
        SELECT t.* FROM tracks t 
        INNER JOIN playlist_track_ref r ON t.id = r.trackId 
        WHERE r.playlistId = :playlistId
    """)
    fun getTracksInPlaylist(playlistId: Int): Flow<List<Track>>

    @Query("UPDATE playlists SET songCount = (SELECT COUNT(*) FROM playlist_track_ref WHERE playlistId = :playlistId) WHERE id = :playlistId")
    suspend fun updatePlaylistSongCount(playlistId: Int)
}
