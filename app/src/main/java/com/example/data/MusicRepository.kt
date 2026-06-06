package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MusicRepository(private val musicDao: MusicDao) {

    val allTracks: Flow<List<Track>> = musicDao.getAllTracks()
    val favoriteTracks: Flow<List<Track>> = musicDao.getFavoriteTracks()
    val offlineTracks: Flow<List<Track>> = musicDao.getOfflineTracks()
    val radioTracks: Flow<List<Track>> = musicDao.getRadioTracks()
    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    suspend fun insertTrack(track: Track) = musicDao.insertTrack(track)
    
    suspend fun insertTracks(tracks: List<Track>) = musicDao.insertTracks(tracks)

    suspend fun setFavorite(trackId: String, isFavorite: Boolean) = 
        musicDao.updateFavorite(trackId, isFavorite)

    suspend fun setDownloadStatus(trackId: String, isDownloaded: Boolean) = 
        musicDao.updateDownloadStatus(trackId, isDownloaded)

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        val playlist = Playlist(name = name, description = description)
        return musicDao.insertPlaylist(playlist)
    }

    suspend fun deletePlaylist(playlistId: Int) = musicDao.deletePlaylist(playlistId)

    suspend fun addTrackToPlaylist(playlistId: Int, trackId: String) {
        musicDao.insertPlaylistTrackRef(PlaylistTrackRef(playlistId, trackId))
        musicDao.updatePlaylistSongCount(playlistId)
    }

    suspend fun removeTrackFromPlaylist(playlistId: Int, trackId: String) {
        musicDao.removeTrackFromPlaylist(playlistId, trackId)
        musicDao.updatePlaylistSongCount(playlistId)
    }

    fun getTracksInPlaylist(playlistId: Int): Flow<List<Track>> = 
        musicDao.getTracksInPlaylist(playlistId)

    // Prepopulate database with initial high-quality music
    suspend fun checkAndSeedDatabase() {
        val currentTracks = allTracks.first()
        if (currentTracks.isEmpty()) {
            val seedTracks = listOf(
                Track(
                    id = "starry_night",
                    title = "Starry Night",
                    artist = "Summer Haze",
                    album = "Milky Way",
                    durationMs = 372000,
                    mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    coverUrl = "synth_purple", // custom graphic color reference
                    lyrics = "[00:10.00] In the quiet of the night\n[00:15.00] Under stellar-painted sky\n[00:20.00] Whispers run into the light\n[00:25.00] Watch the shooting stars go by\n[00:40.00] We are glowing in the dark\n[00:45.00] Sparking fires with a spark\n[00:55.00] Stars will align as we fly higher",
                    isOffline = false,
                    isFavorite = true
                ),
                Track(
                    id = "neon_dreams",
                    title = "Neon Dreams",
                    artist = "Retro Future",
                    album = "Outrun 1984",
                    durationMs = 315000,
                    mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    coverUrl = "mint_green",
                    lyrics = "[00:05.00] Electric lights on wet concrete\n[00:12.00] Driving speedward in the heat\n[00:19.00] Synthetic drums begin to beat\n[00:26.00] Escape the sirens on the street\n[00:38.00] Neon dreams are in your eyes\n[00:45.00] Chrome-colored digital skies",
                    isOffline = false,
                    isFavorite = false
                ),
                Track(
                    id = "rainy_days",
                    title = "Rainy Days",
                    artist = "Lofi Beats Collective",
                    album = "Coffee & Rain",
                    durationMs = 302000,
                    mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                    coverUrl = "cozy_brown",
                    lyrics = "[00:10.00] Rainy drops on window glass\n[00:20.00] Sipping coffee, hours pass\n[00:30.00] Dusty vinyl rotates slow\n[00:40.00] Warm fires gently glow\n[00:50.00] Finding peace in times of rain\n[01:00.00] Letting go of all the pain",
                    isOffline = true,
                    isFavorite = true
                ),
                Track(
                    id = "starlight",
                    title = "Starlight Rush",
                    artist = "Vibrant Waves",
                    album = "Glitch Universe",
                    durationMs = 285000,
                    mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                    coverUrl = "cyber_pink",
                    lyrics = "[00:08.00] Zooming out of our horizon\n[00:15.00] Dynamic cosmic forces rising\n[00:22.00] Take my hand, we are finalizing\n[00:30.00] Universal joy summarizing\n[00:45.00] Starlight rush in my head\n[00:52.00] Walking where the angels tread",
                    isOffline = false,
                    isFavorite = false
                ),
                Track(
                    id = "ethereal_echoes",
                    title = "Ethereal Echoes",
                    artist = "Acoustic Whispers",
                    album = "Timber cabin",
                    durationMs = 340000,
                    mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                    coverUrl = "warm_amber",
                    lyrics = "[00:15.00] Quiet echoes in the trees\n[00:25.00] Carried by a gentle breeze\n[00:35.00] Hollow guitars, silver strings\n[00:45.00] Flying high on wooden wings\n[00:55.00] Life is simple, life is sweet\n[01:05.00] Where the forest and rivers meet",
                    isOffline = true,
                    isFavorite = false
                ),
                // Internet Radio Channels
                Track(
                    id = "radio_lofi",
                    title = "Lofi Chill FM",
                    artist = "Global Radio Network",
                    album = "Internet Stream",
                    durationMs = -1, // Live representation
                    mediaUrl = "https://stream.zeno.fm/f3b6v784u8zuv",
                    coverUrl = "radio_neon",
                    lyrics = "[00:00.00] Live Radio: Lofi Chill FM\n[00:05.00] Broadcasting lo-fi vibes worldwide!\n[00:10.00] Grab a warm drink, sit back and enjoy.",
                    isOffline = false,
                    isRadio = true
                ),
                Track(
                    id = "radio_electronica",
                    title = "Ibiza Deep Radio",
                    artist = "Ibiza Network",
                    album = "Live Live FM",
                    durationMs = -1,
                    mediaUrl = "https://stream.zeno.fm/096t67t208zuv",
                    coverUrl = "radio_sunset",
                    lyrics = "[00:00.00] Live stream from the white island.\n[00:10.00] Pure electronic deep house music.",
                    isOffline = false,
                    isRadio = true
                ),
                Track(
                    id = "radio_synthwave",
                    title = "Synthentic Outrun FM",
                    artist = "Cyber Broadcast Corp",
                    album = "Outrun Stream",
                    durationMs = -1,
                    mediaUrl = "https://stream.zeno.fm/4gqfcn8y08zuv",
                    coverUrl = "radio_synth",
                    lyrics = "[00:00.00] Cruising into the retro-future...\n[00:10.00] Synthesizers, lasers, and custom basslines.",
                    isOffline = false,
                    isRadio = true
                )
            )
            insertTracks(seedTracks)
            
            // Seed a default playlist
            val playlistId = createPlaylist("My Summer Vibes", "Vibrant grooves for perfect weather")
            addTrackToPlaylist(playlistId.toInt(), "starry_night")
            addTrackToPlaylist(playlistId.toInt(), "neon_dreams")
            addTrackToPlaylist(playlistId.toInt(), "starlight")
        }
    }
}
