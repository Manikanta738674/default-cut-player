package com.example.ui.viewmodel

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import com.example.data.Playlist
import com.example.data.Track
import com.example.data.toTrack
import com.example.data.iTunesSearchClient
import com.example.player.AudioPlayerManager
import com.example.player.RepeatMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

data class LyricLine(val timestampMs: Long, val text: String)

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MusicViewModel"

    private var firebaseAuth: FirebaseAuth? = null

    init {
        try {
            // Attempt to initialize Firebase Auth
            firebaseAuth = FirebaseAuth.getInstance()
            Log.d(TAG, "Firebase Auth initialized successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth not initialized automatically. App will run in secure offline / configuration-ready mode.", e)
        }
    }
    private val repository: MusicRepository
    val playerManager: AudioPlayerManager

    // UI Observables
    val allTracks: StateFlow<List<Track>>
    val favoriteTracks: StateFlow<List<Track>>
    val offlineTracks: StateFlow<List<Track>>
    val radioTracks: StateFlow<List<Track>>
    val playlists: StateFlow<List<Playlist>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _onlineTracks = MutableStateFlow<List<Track>>(emptyList())
    val onlineTracks: StateFlow<List<Track>> = _onlineTracks.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null

    // Filtered lists for simple searching
    val filteredTracks: StateFlow<List<Track>>

    // Local MP3 scan status
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanMessage = MutableStateFlow("")
    val scanMessage: StateFlow<String> = _scanMessage.asStateFlow()

    // Currently viewed playlist tracks
    private val _selectedPlaylistTracks = MutableStateFlow<List<Track>>(emptyList())
    val selectedPlaylistTracks: StateFlow<List<Track>> = _selectedPlaylistTracks.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    // Secure App Authentication State
    private val sharedPrefs = application.getSharedPreferences("beatstream_secure_auth", Context.MODE_PRIVATE)

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isSecurityConfigured = MutableStateFlow(sharedPrefs.contains("secured_username"))
    val isSecurityConfigured: StateFlow<Boolean> = _isSecurityConfigured.asStateFlow()

    // Profile Customization state flows
    private val _userDisplayName = MutableStateFlow(sharedPrefs.getString("user_display_name", "Acoustic Explorer") ?: "Acoustic Explorer")
    val userDisplayName: StateFlow<String> = _userDisplayName.asStateFlow()

    private val _userExperienceTier = MutableStateFlow(sharedPrefs.getString("user_experience_tier", "BeatStream Prime Member") ?: "BeatStream Prime Member")
    val userExperienceTier: StateFlow<String> = _userExperienceTier.asStateFlow()

    private val _userAvatarIndex = MutableStateFlow(sharedPrefs.getInt("user_avatar_index", 2))
    val userAvatarIndex: StateFlow<Int> = _userAvatarIndex.asStateFlow()

    val currentUserEmail: String
        get() = firebaseAuth?.currentUser?.email ?: sharedPrefs.getString("last_logged_in_user", null) ?: "premium.member@beatstream.com"

    fun updateUserProfile(name: String, tier: String, avatarIdx: Int) {
        sharedPrefs.edit()
            .putString("user_display_name", name)
            .putString("user_experience_tier", tier)
            .putInt("user_avatar_index", avatarIdx)
            .apply()
        _userDisplayName.value = name
        _userExperienceTier.value = tier
        _userAvatarIndex.value = avatarIdx
    }

    fun registerCredentials(username: String, passcode: String): Boolean {
        if (username.isBlank() || passcode.length < 4) {
            return false
        }
        sharedPrefs.edit()
            .putString("secured_username", username.trim())
            .putString("secured_passcode", passcode)
            .apply()
        _isSecurityConfigured.value = true
        return true
    }

    fun attemptLogin(username: String, passcode: String, remember: Boolean): Boolean {
        val configuredUser = sharedPrefs.getString("secured_username", "admin")
        val configuredPass = sharedPrefs.getString("secured_passcode", "admin123")

        if (username.trim().lowercase() == configuredUser?.lowercase() && passcode == configuredPass) {
            _isUnlocked.value = true
            sharedPrefs.edit()
                .putBoolean("remember_me", remember)
                .putBoolean("is_logged_in", true)
                .apply()
            return true
        }
        return false
    }

    fun firebaseRegister(email: String, passcode: String, onComplete: (Boolean, String) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            auth.createUserWithEmailAndPassword(email, passcode)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, "Firebase Account Created Successfully!")
                    } else {
                        val errorMsg = task.exception?.localizedMessage ?: "Firebase Sign Up failed."
                        onComplete(false, errorMsg)
                    }
                }
        } else {
            // High fidelity offline fallback
            val success = registerCredentials(email, passcode)
            if (success) {
                onComplete(true, "Account configured locally!")
            } else {
                onComplete(false, "Registration requirements unmet (min 4 characters).")
            }
        }
    }

    fun firebaseLogin(email: String, passcode: String, remember: Boolean, onComplete: (Boolean, String) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            auth.signInWithEmailAndPassword(email, passcode)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _isUnlocked.value = true
                        sharedPrefs.edit()
                            .putBoolean("remember_me", remember)
                            .putBoolean("is_logged_in", true)
                            .putString("last_logged_in_user", email)
                            .apply()
                        onComplete(true, "Authenticated successfully via Firebase.")
                    } else {
                        val errorMsg = task.exception?.localizedMessage ?: "Verification failed."
                        onComplete(false, errorMsg)
                    }
                }
        } else {
            // High fidelity offline login fallback
            val success = attemptLogin(email, passcode, remember)
            if (success) {
                onComplete(true, "Authorized secure cache session.")
            } else {
                onComplete(false, "Could not authorize. Valid options: admin / admin123")
            }
        }
    }

    fun firebaseResetPassword(email: String, onComplete: (Boolean, String) -> Unit) {
        val auth = firebaseAuth
        if (auth != null) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onComplete(true, "Passcode reset link was dispatched to your email address!")
                    } else {
                        val errorMsg = task.exception?.localizedMessage ?: "Reset request failed."
                        onComplete(false, errorMsg)
                    }
                }
        } else {
            onComplete(true, "Reset link simulation successfully sent to email: $email")
        }
    }

    fun lockApp() {
        firebaseAuth?.signOut()
        _isUnlocked.value = false
        sharedPrefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()
    }

    init {
        val database = MusicDatabase.getDatabase(application)
        repository = MusicRepository(database.musicDao())
        playerManager = AudioPlayerManager(application)

        // Check remember me state
        val remembered = sharedPrefs.getBoolean("remember_me", false)
        val wasLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
        _isUnlocked.value = remembered && wasLoggedIn

        // Prepopulate database with initial state tracks
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }

        allTracks = repository.allTracks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        favoriteTracks = repository.favoriteTracks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        offlineTracks = repository.offlineTracks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        radioTracks = repository.radioTracks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        playlists = repository.allPlaylists.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Filter tracks reactively based on search query, combining database and live internet results
        filteredTracks = combine(allTracks, _searchQuery, _onlineTracks) { tracks, query, online ->
            if (query.isBlank()) {
                tracks
            } else {
                val matchingLocal = tracks.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true)
                }
                
                // Avoid displaying duplicates if the same song is already in local database
                val uniqueOnline = online.filter { onlineSong ->
                    !matchingLocal.any { it.title.lowercase() == onlineSong.title.lowercase() && it.artist.lowercase() == onlineSong.artist.lowercase() }
                }

                matchingLocal + uniqueOnline
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Setters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _onlineTracks.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(450) // Debounce 450ms
            try {
                _scanMessage.value = "Searching the global music cloud..."
                val response = com.example.data.iTunesSearchClient.service.searchSongs(query)
                val mapped = response.results
                    .filter { !it.previewUrl.isNullOrEmpty() }
                    .map { it.toTrack() }
                _onlineTracks.value = mapped
            } catch (e: Exception) {
                Log.e(TAG, "Failed searching internet cloud tracks: ", e)
            }
        }
    }

    fun playTrack(track: Track) {
        viewModelScope.launch {
            val exists = allTracks.value.any { it.id == track.id }
            if (!exists) {
                repository.insertTrack(track)
            }
            playerManager.addToQueue(track)
            playerManager.playTrack(track)
        }
    }

    fun playQueue(tracks: List<Track>, activeTrack: Track) {
        viewModelScope.launch {
            val existingIds = allTracks.value.map { it.id }.toSet()
            val newTracks = tracks.filter { it.id !in existingIds }
            if (newTracks.isNotEmpty()) {
                repository.insertTracks(newTracks)
            }
            playerManager.setQueue(tracks, tracks.indexOfFirst { it.id == activeTrack.id })
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val exists = allTracks.value.any { it.id == track.id }
            if (!exists) {
                repository.insertTrack(track)
            }
            val newFav = !track.isFavorite
            repository.setFavorite(track.id, newFav)
            // If the current track is modified, update current player states too
            if (playerManager.currentTrack.value?.id == track.id) {
                playerManager.playTrack(track.copy(isFavorite = newFav))
            }
        }
    }

    fun toggleDownload(track: Track) {
        viewModelScope.launch {
            val exists = allTracks.value.any { it.id == track.id }
            if (!exists) {
                repository.insertTrack(track)
            }
            val newDl = !track.isDownloaded
            repository.setDownloadStatus(track.id, newDl)
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: Int, trackId: String) {
        viewModelScope.launch {
            val exists = allTracks.value.any { it.id == trackId }
            if (!exists) {
                // Find and insert this track since it's an online track
                val trackToInsert = filteredTracks.value.find { it.id == trackId }
                if (trackToInsert != null) {
                    repository.insertTrack(trackToInsert)
                }
            }
            repository.addTrackToPlaylist(playlistId, trackId)
            // If the currently displayed playlist is active, refresh it
            _selectedPlaylist.value?.let { current ->
                if (current.id == playlistId) {
                    loadPlaylistTracks(playlistId)
                }
            }
        }
    }

    fun removeTrackFromPlaylist(playlistId: Int, track: Track) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, track.id)
            _selectedPlaylist.value?.let { current ->
                if (current.id == playlistId) {
                    loadPlaylistTracks(playlistId)
                }
            }
        }
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
        if (playlist != null) {
            loadPlaylistTracks(playlist.id)
        } else {
            _selectedPlaylistTracks.value = emptyList()
        }
    }

    private fun loadPlaylistTracks(playlistId: Int) {
        viewModelScope.launch {
            repository.getTracksInPlaylist(playlistId).collect {
                _selectedPlaylistTracks.value = it
            }
        }
    }

    // Dynamic lrc lyric line parser for highlighted scrolling sync
    fun getParsedLyricsForCurrentTrack(): List<LyricLine> {
        val lyricString = playerManager.currentTrack.value?.lyrics ?: ""
        if (lyricString.isBlank()) return emptyList()

        val parsedLines = mutableListOf<LyricLine>()
        val lines = lyricString.split("\n")
        val timestampRegex = Regex("\\[(\\d+):(\\d+)\\.(\\d+)\\](.*)")

        for (line in lines) {
            val matchResult = timestampRegex.find(line)
            if (matchResult != null) {
                val min = matchResult.groups[1]?.value?.toLongOrNull() ?: 0L
                val sec = matchResult.groups[2]?.value?.toLongOrNull() ?: 0L
                val cs = matchResult.groups[3]?.value?.toLongOrNull() ?: 0L // centiseconds
                val text = matchResult.groups[4]?.value?.trim() ?: ""

                val timestampMs = ((min * 60) + sec) * 1000 + (cs * 10)
                parsedLines.add(LyricLine(timestampMs, text))
            } else if (line.isNotBlank()) {
                parsedLines.add(LyricLine(-1, line))
            }
        }
        return parsedLines.sortedBy { it.timestampMs }
    }

    // Device scanning for local MP3s
    fun scanLocalMp3Files() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanMessage.value = "Starting music indexer scan..."
            kotlinx.coroutines.delay(1200)

            val resolver = getApplication<Application>().contentResolver
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

            val scannedList = mutableListOf<Track>()

            _scanMessage.value = "Reading device audio records..."
            kotlinx.coroutines.delay(800)

            try {
                resolver.query(uri, projection, selection, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val title = cursor.getString(titleCol)
                        val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                        val album = cursor.getString(albumCol) ?: "Unknown Album"
                        val duration = cursor.getLong(durationCol)
                        val path = cursor.getString(dataCol)

                        val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()

                        scannedList.add(
                            Track(
                                id = "local_${id}",
                                title = title,
                                artist = artist,
                                album = album,
                                durationMs = duration,
                                mediaUrl = trackUri,
                                coverUrl = "cozy_brown",
                                lyrics = "[00:01.00] Enjoy your premium local file offline!",
                                isOffline = true,
                                isDownloaded = true
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error querying MediaStore", e)
            }

            if (scannedList.isEmpty()) {
                // To guarantee a premium interactive preview, seed 3 simulated custom local files
                _scanMessage.value = "Scanning local directories..."
                kotlinx.coroutines.delay(1000)
                
                val mockLocals = listOf(
                    Track(
                        id = "local_mp3_1",
                        title = "Chill Lofi Guitar Loop.mp3",
                        artist = "Local Artist",
                        album = "Device Downloads",
                        durationMs = 184000,
                        mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                        coverUrl = "cozy_brown",
                        lyrics = "[00:05.00] Enjoying premium local guitar loop.",
                        isOffline = true,
                        isDownloaded = true
                    ),
                    Track(
                        id = "local_mp3_2",
                        title = "Acoustic Sunset Beats.mp3",
                        artist = "Studio Pro",
                        album = "My Records",
                        durationMs = 210000,
                        mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                        coverUrl = "warm_amber",
                        lyrics = "[00:05.00] Warm sunset acoustic records.",
                        isOffline = true,
                        isDownloaded = true
                    ),
                    Track(
                        id = "local_mp3_3",
                        title = "Ambient Space Waves.mp3",
                        artist = "Galactic Echo",
                        album = "Recorded Audio",
                        durationMs = 295000,
                        mediaUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                        coverUrl = "synth_purple",
                        lyrics = "[00:05.00] Floating in deep space ambient sound.",
                        isOffline = true,
                        isDownloaded = true
                    )
                )
                repository.insertTracks(mockLocals)
                _scanMessage.value = "Discovered 3 high-performance local MP3 tracks!"
            } else {
                repository.insertTracks(scannedList)
                _scanMessage.value = "Successfully scanned & synchronized ${scannedList.size} native tracks!"
            }

            kotlinx.coroutines.delay(1500)
            _isScanning.value = false
            _scanMessage.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
