package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mediaUrl: String,
    val coverUrl: String, // Can be a local resource, custom color gradient index or web cover
    val lyrics: String = "",
    val isOffline: Boolean = false,
    val isFavorite: Boolean = false,
    val isRadio: Boolean = false,
    val isDownloaded: Boolean = false
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val songCount: Int = 0
)

@Entity(tableName = "playlist_track_ref", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrackRef(
    val playlistId: Int,
    val trackId: String
)
