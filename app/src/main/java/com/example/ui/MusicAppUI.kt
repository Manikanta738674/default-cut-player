package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.data.Playlist
import com.example.data.Track
import com.example.player.RepeatMode
import com.example.player.AudioQuality
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos

enum class AppScreen {
    HOME, SEARCH, LIBRARY, EQUALIZER, LYRICS, PLAYER_DETAIL, PLAYLIST_DETAIL, PROFILE, QUEUE
}

@Composable
fun MusicAppUI(viewModel: MusicViewModel) {
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val isSecurityConfigured by viewModel.isSecurityConfigured.collectAsStateWithLifecycle()

    if (!isUnlocked) {
        SecureLoginScreen(viewModel = viewModel)
        return
    }

    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    val scope = rememberCoroutineScope()

    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.playerManager.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.playerManager.duration.collectAsStateWithLifecycle()

    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(DeepBlack)
                    .navigationBarsPadding()
            ) {
                // Bottom Mini Player
                if (currentTrack != null && currentScreen != AppScreen.PLAYER_DETAIL && currentScreen != AppScreen.LYRICS) {
                    MiniPlayer(
                        track = currentTrack!!,
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        onPlayPauseToggle = {
                            if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                        },
                        onSkipNext = { viewModel.playerManager.skipToNext() },
                        onMiniPlayerTap = { currentScreen = AppScreen.PLAYER_DETAIL }
                    )
                }

                // Main navigation bar
                NavigationBar(
                    containerColor = DeepBlack,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { currentScreen = AppScreen.HOME },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGreen,
                            selectedTextColor = AccentGreen,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.SEARCH,
                        onClick = { currentScreen = AppScreen.SEARCH },
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Search", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGreen,
                            selectedTextColor = AccentGreen,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.LIBRARY,
                        onClick = { currentScreen = AppScreen.LIBRARY },
                        icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGreen,
                            selectedTextColor = AccentGreen,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.EQUALIZER,
                        onClick = { currentScreen = AppScreen.EQUALIZER },
                        icon = { Icon(Icons.Filled.Equalizer, contentDescription = "Equalizer") },
                        label = { Text("Equalizer", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentGreen,
                            selectedTextColor = AccentGreen,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF03160B), // Deep elegant green aurora glass backing
                            Color(0xFF060907), // Glass glow transition
                            DeepBlack
                        )
                    )
                )
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onTrackSelected = { track, list ->
                            viewModel.playQueue(list, track)
                        },
                        onPlaylistSelected = { playlist ->
                            viewModel.selectPlaylist(playlist)
                            currentScreen = AppScreen.PLAYLIST_DETAIL
                        },
                        onNavigateToScreen = { currentScreen = it }
                    )
                    AppScreen.SEARCH -> SearchScreen(
                        viewModel = viewModel,
                        onTrackSelected = { track ->
                            viewModel.playTrack(track)
                        }
                    )
                    AppScreen.LIBRARY -> LibraryScreen(
                        viewModel = viewModel,
                        onCreatePlaylistTap = { showCreatePlaylistDialog = true },
                        onPlaylistSelected = { playlist ->
                            viewModel.selectPlaylist(playlist)
                            currentScreen = AppScreen.PLAYLIST_DETAIL
                        },
                        onTrackSelected = { track, list ->
                            viewModel.playQueue(list, track)
                        }
                    )
                    AppScreen.EQUALIZER -> EqualizerScreen(viewModel = viewModel)
                    AppScreen.LYRICS -> LyricsScreen(
                        viewModel = viewModel,
                        onBackTap = { currentScreen = AppScreen.PLAYER_DETAIL }
                    )
                    AppScreen.PLAYER_DETAIL -> PlayerDetailScreen(
                        viewModel = viewModel,
                        onBackTap = { currentScreen = AppScreen.HOME },
                        onLyricsTap = { currentScreen = AppScreen.LYRICS },
                        onQueueTap = { currentScreen = AppScreen.QUEUE }
                    )
                    AppScreen.PLAYLIST_DETAIL -> PlaylistDetailScreen(
                        viewModel = viewModel,
                        playlistName = selectedPlaylist?.name ?: "Playlist",
                        playlistDesc = selectedPlaylist?.description ?: "Your custom curation",
                        onPlayAll = { tracks ->
                            if (tracks.isNotEmpty()) {
                                viewModel.playQueue(tracks, tracks.first())
                            }
                        },
                        onTrackSelected = { track, list ->
                            viewModel.playQueue(list, track)
                        },
                        onBackTap = { currentScreen = AppScreen.HOME }
                    )
                    AppScreen.PROFILE -> ProfileScreen(
                        viewModel = viewModel,
                        onBackTap = { currentScreen = AppScreen.HOME }
                    )
                    AppScreen.QUEUE -> QueueScreen(
                        viewModel = viewModel,
                        onBackTap = { currentScreen = AppScreen.PLAYER_DETAIL }
                    )
                }
            }

            if (showCreatePlaylistDialog) {
                CreatePlaylistDialog(
                    onDismiss = { showCreatePlaylistDialog = false },
                    onConfirm = { name, desc ->
                        viewModel.createPlaylist(name, desc)
                        showCreatePlaylistDialog = false
                    }
                )
            }
        }
    }
}

// Subcomponent: Procedural Gradient Artist/Album Cover Generator (No copyrighted assets)
@Composable
fun ProceduralCover(
    coverId: String,
    titleLetter: String,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false,
    isRadio: Boolean = false
) {
    val isNetworkUrl = coverId.startsWith("http://") || coverId.startsWith("https://")

    if (isNetworkUrl) {
        Box(
            modifier = modifier.background(PremiumCharcoal),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = coverId,
                contentDescription = "Cover of $titleLetter",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        val brush = remember(coverId) {
            val c1 = when (coverId) {
                "synth_purple" -> CoverSynthPurple
                "mint_green" -> AccentGreen
                "cozy_brown" -> CoverCozyBrown
                "cyber_pink" -> CoverCyberPink
                "warm_amber" -> CoverWarmAmber
                "radio_neon" -> CoverLiveRadio
                "radio_sunset" -> CoverLiveSunset
                "radio_synth" -> CoverLiveSynth
                else -> PremiumCharcoal
            }
            val c2 = when (coverId) {
                "synth_purple" -> CoverCyberPink
                "mint_green" -> CoverMintGreen
                "cozy_brown" -> CoverWarmAmber
                "cyber_pink" -> CoverSynthPurple
                "warm_amber" -> CoverLiveSunset
                "radio_neon" -> CoverLiveSynth
                "radio_sunset" -> CoverWarmAmber
                "radio_synth" -> CoverSynthPurple
                else -> MediumGray
            }
            Brush.linearGradient(colors = listOf(c1, c2))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .background(brush)
        ) {
            if (isRadio) {
                Icon(
                    imageVector = Icons.Filled.Radio,
                    contentDescription = "Radio Stream",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(if (isLarge) 44.dp else 22.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (titleLetter.isNotBlank()) titleLetter.take(1).uppercase() else "♫",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isLarge) 32.sp else 16.sp
                    )
                    if (isLarge) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = "MusicNote",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// 1. HOME SCREEN LAYOUT
@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onTrackSelected: (Track, List<Track>) -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onNavigateToScreen: (AppScreen) -> Unit = {}
) {
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val radioTracks by viewModel.radioTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsStateWithLifecycle()

    var activeCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = listOf("All Tracks", "Live Streams", "Playlists")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. GLASS SEARCH & PROFILE HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Glass Search Pill
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BlurGlass)
                        .clickable { onNavigateToScreen(AppScreen.SEARCH) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "SearchIcon",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search music, artists...",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }                // Profile Avatar icon - Navigates to User Profile Preferences Screen
                val avatarIndex by viewModel.userAvatarIndex.collectAsStateWithLifecycle()
                val avatarColors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF3B82F6))
                val avatarColor = avatarColors[avatarIndex % avatarColors.size]
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(avatarColor, avatarColor.copy(alpha = 0.5f), avatarColor)
                            )
                        )
                        .clickable { onNavigateToScreen(AppScreen.PROFILE) }
                        .padding(2.dp)
                        .testTag("user_profile_badge")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A1A))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile Preferences",
                            tint = avatarColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Lock Session Action Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(Color(0xFF22C55E), Color(0xFF4ADE80), Color(0xFF22C55E))
                            )
                        )
                        .clickable { viewModel.lockApp() }
                        .padding(2.dp)
                        .testTag("lock_session_badge")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF1A1A1A))
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = "Lock Session",
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Welcoming JioSaavn Grid Banner
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "Good Day,",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                    color = TextSecondary
                )
                Text(
                    text = "Welcome to BeatStream",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        // 2. FEATURED ARTIST GLASS CARD (SPOTLIGHT)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clickable {
                        // Play a spotlight track (e.g. "Starry Night")
                        val spotlightTrack = allTracks.find { it.id == "starry_night" } ?: allTracks.firstOrNull()
                        if (spotlightTrack != null) {
                            if (currentTrack?.id == spotlightTrack.id) {
                                if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                            } else {
                                onTrackSelected(spotlightTrack, allTracks)
                            }
                        }
                    }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Soft gradient background behind to simulate glassmorphic depth
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0x3F673AB7), Color(0x7F22C55E))
                                )
                            )
                    )

                    // Card contents
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "NEW RELEASE",
                                    color = AccentGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Midnight Sessions",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "The Weeknd & Daft Punk",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Spinning music disc or symbol representing live session
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exclusive Spotlight Release",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )

                            // Play Button resembling HTML design
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AccentGreen)
                                    .clickable {
                                        val spotlightTrack = allTracks.find { it.id == "starry_night" } ?: allTracks.firstOrNull()
                                        if (spotlightTrack != null) {
                                            if (currentTrack?.id == spotlightTrack.id) {
                                                if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                                            } else {
                                                onTrackSelected(spotlightTrack, allTracks)
                                            }
                                        }
                                    }
                            ) {
                                val isSpotlightPlaying = currentTrack?.id == "starry_night" && isPlaying
                                Icon(
                                    imageVector = if (isSpotlightPlaying) Icons.Filled.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play Spotlight",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. QUICK ACTIONS GRID
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Offline Shortcut
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PremiumCharcoal)
                        .clickable {
                            // Offline scanner or playlist
                            onNavigateToScreen(AppScreen.LIBRARY)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentGreen.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Offline Cache",
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Offline",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Device Music",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                // Radio stream Shortcut
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PremiumCharcoal)
                        .clickable {
                            // Turn to Radio category
                            activeCategoryIndex = 1
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE65100).copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = "Radio Link",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Radio Streams",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Live Broadcast",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Horizontal Category Chips
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { idx, label ->
                    val isSelected = activeCategoryIndex == idx
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) AccentGreen else PremiumCharcoal
                        ),
                        modifier = Modifier
                            .clickable { activeCategoryIndex = idx }
                            .testTag("home_category_${idx}")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Selected Category Content
        when (activeCategoryIndex) {
            0 -> {
                // Trending Tracks Header
                item {
                    Text(
                        text = "Trending Now",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                items(allTracks.filter { !it.isRadio }) { track ->
                    val isActive = currentTrack?.id == track.id
                    SongRow(
                        track = track,
                        onRowClick = {
                            if (isActive) {
                                if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                            } else {
                                onTrackSelected(track, allTracks.filter { !it.isRadio })
                            }
                        },
                        onFavClick = { viewModel.toggleFavorite(track) },
                        isActive = isActive,
                        isPlaying = isPlaying
                    )
                }
            }
            1 -> {
                item {
                    Text(
                        text = "Internet Live Radios",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                if (radioTracks.isEmpty()) {
                    item {
                        EmptyResultState("No dynamic radio stations ready.", Icons.Default.Radio)
                    }
                } else {
                    items(radioTracks) { track ->
                        val isActive = currentTrack?.id == track.id
                        SongRow(
                            track = track,
                            onRowClick = {
                                if (isActive) {
                                    if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                                } else {
                                    onTrackSelected(track, radioTracks)
                                }
                            },
                            onFavClick = { viewModel.toggleFavorite(track) },
                            isActive = isActive,
                            isPlaying = isPlaying,
                            isRadioMode = true
                        )
                    }
                }
            }
            2 -> {
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Featured Curations",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                if (playlists.isEmpty()) {
                    item {
                        EmptyResultState("Create custom playlists from your library!", Icons.Default.BookmarkAdd)
                    }
                } else {
                    items(playlists) { playlist ->
                        PlaylistCardCell(
                            playlist = playlist,
                            onTap = { onPlaylistSelected(playlist) }
                        )
                    }
                }
            }
        }

        // Artist Grid Section
        item {
            Text(
                text = "Artist Spotlights",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val artists = listOf(
                    "Summer Haze" to "synth_purple",
                    "Retro Future" to "mint_green",
                    "Lofi Beats Collective" to "cozy_brown",
                    "Vibrant Waves" to "cyber_pink",
                    "Acoustic Whispers" to "warm_amber"
                )
                items(artists) { artist ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(88.dp)
                            .clickable {
                                viewModel.updateSearchQuery(artist.first)
                            }
                    ) {
                        ProceduralCover(
                            coverId = artist.second,
                            titleLetter = artist.first,
                            isLarge = true,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = artist.first,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Artist",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// 2. SEARCH SCREEN LAYOUT
@Composable
fun SearchScreen(
    viewModel: MusicViewModel,
    onTrackSelected: (Track) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsStateWithLifecycle()

    val recommendedMoods = listOf(
        "Lofi Chill" to "cozy_brown",
        "Synthwave" to "synth_purple",
        "Electronic" to "mint_green",
        "Acoustic" to "warm_amber",
        "Live Radio" to "radio_neon"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Explore & Search",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Glassmorphism Styled Search Text Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Tracks, Artist or Album name", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "SearchIcon") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "ClearIcon")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PremiumCharcoal,
                unfocusedContainerColor = PremiumCharcoal.copy(alpha = 0.5f),
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = MediumGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isEmpty()) {
            Text(
                text = "Mood Categories",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recommendedMoods) { mood ->
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .height(84.dp)
                            .clickable { viewModel.updateSearchQuery(mood.first) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
                        colors = CardDefaults.cardColors(containerColor = PremiumCharcoal)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ProceduralCover(
                                coverId = mood.second,
                                titleLetter = mood.first,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )
                            Text(
                                text = mood.first,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Enter query to search database or tap dynamic circles above.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Results for '${searchQuery}'",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            if (filteredTracks.isEmpty()) {
                EmptyResultState("No songs found matching query.", Icons.Outlined.SentimentVeryDissatisfied)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredTracks) { track ->
                        val isActive = currentTrack?.id == track.id
                        SongRow(
                            track = track,
                            onRowClick = {
                                if (isActive) {
                                    if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                                } else {
                                    onTrackSelected(track)
                                }
                            },
                            onFavClick = { viewModel.toggleFavorite(track) },
                            isActive = isActive,
                            isPlaying = isPlaying
                        )
                    }
                }
            }
        }
    }
}

// 3. LIBRARY SCREEN LAYOUT
@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    onCreatePlaylistTap: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onTrackSelected: (Track, List<Track>) -> Unit
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val offlineTracks by viewModel.offlineTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsStateWithLifecycle()

    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanMessage by viewModel.scanMessage.collectAsStateWithLifecycle()

    var activeLibIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Playlists", "Offline Saved", "Favorites")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Music Space",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom indexers
        TabRow(
            selectedTabIndex = activeLibIndex,
            containerColor = Color.Transparent,
            contentColor = AccentGreen,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeLibIndex == index,
                    onClick = { activeLibIndex = index },
                    modifier = Modifier.testTag("lib_tab_${index}")
                ) {
                    Text(
                        text = title,
                        color = if (activeLibIndex == index) AccentGreen else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activeLibIndex) {
            0 -> {
                // Playlists view
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCreatePlaylistTap() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MediumGray)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Playlist", tint = AccentGreen)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Create Playlist", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Curate your signature tracks list", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(playlists) { playlist ->
                        PlaylistRow(
                            playlist = playlist,
                            onTap = { onPlaylistSelected(playlist) },
                            onDelete = { viewModel.playerManager.stop(); viewModel.deletePlaylist(playlist.id) }
                        )
                    }
                }
            }
            1 -> {
                // Offline Media files scans
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Internal Disk Scanning",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Synchronize native .mp3 music files",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (isScanning) {
                        CircularProgressIndicator(
                            color = AccentGreen,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Button(
                            onClick = { viewModel.scanLocalMp3Files() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("scan_mp3_btn")
                        ) {
                            Text("Scan", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (scanMessage.isNotBlank()) {
                    Text(
                        text = scanMessage,
                        color = AccentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                }

                if (offlineTracks.isEmpty()) {
                    EmptyResultState("No synchronized/downloaded files.", Icons.Filled.CloudDownload)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(offlineTracks) { track ->
                            val isActive = currentTrack?.id == track.id
                            SongRow(
                                track = track,
                                onRowClick = {
                                    if (isActive) {
                                        if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                                    } else {
                                        onTrackSelected(track, offlineTracks)
                                    }
                                },
                                onFavClick = { viewModel.toggleFavorite(track) },
                                isActive = isActive,
                                isPlaying = isPlaying
                            )
                        }
                    }
                }
            }
            2 -> {
                // Favorite Tracks
                if (favoriteTracks.isEmpty()) {
                    EmptyResultState("Your favorite songs will show here.", Icons.Filled.FavoriteBorder)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        items(favoriteTracks) { track ->
                            val isActive = currentTrack?.id == track.id
                            SongRow(
                                track = track,
                                onRowClick = {
                                    if (isActive) {
                                        if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                                    } else {
                                        onTrackSelected(track, favoriteTracks)
                                    }
                                },
                                onFavClick = { viewModel.toggleFavorite(track) },
                                isActive = isActive,
                                isPlaying = isPlaying
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. EQUALIZER CONTROLS SCREEN
@Composable
fun EqualizerScreen(viewModel: MusicViewModel) {
    val enabled by viewModel.playerManager.equalizerEnabled.collectAsStateWithLifecycle()
    val preset by viewModel.playerManager.equalizerPreset.collectAsStateWithLifecycle()
    val bands by viewModel.playerManager.equalizerBands.collectAsStateWithLifecycle()

    val presets = listOf("Flat", "Bass Boost", "Vocal", "Electronic", "Acoustic", "Rock")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Professional EQ",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "Customize acoustic frequencies",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { viewModel.playerManager.setEqualizerEnabled(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = AccentGreen,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = MediumGray
                ),
                modifier = Modifier.testTag("eq_switch")
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Visual equalizer columns
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val sortedBands = bands.keys.sorted()
            sortedBands.forEach { hz ->
                val db = bands[hz] ?: 0
                val label = if (hz >= 1000) "${hz / 1000}kHz" else "${hz}Hz"

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    Text(
                        text = "${if (db > 0) "+" else ""}${db}dB",
                        color = if (enabled) AccentGreen else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = db.toFloat(),
                        onValueChange = { newValue ->
                            if (enabled) viewModel.playerManager.updateBand(hz, newValue.toInt())
                        },
                        valueRange = -12f..12f,
                        steps = 24,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("slider_${hz}"),
                        colors = SliderDefaults.colors(
                            thumbColor = if (enabled) AccentGreen else TextSecondary,
                            activeTrackColor = if (enabled) AccentGreen else MediumGray,
                            inactiveTrackColor = MediumGray.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preset collection chips
        Text(
            text = "PRESETS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { p ->
                val isSelected = preset == p
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) AccentGreen else PremiumCharcoal
                    ),
                    modifier = Modifier
                        .clickable { if (enabled) viewModel.playerManager.applyPreset(p) }
                        .testTag("preset_${p}")
                ) {
                    Text(
                        text = p,
                        color = if (isSelected) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// 5. LYRICS SYNC SCREEN
@Composable
fun LyricsScreen(
    viewModel: MusicViewModel,
    onBackTap: () -> Unit
) {
    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val currentPosition by viewModel.playerManager.currentPosition.collectAsStateWithLifecycle()

    val parsedLines = remember(currentTrack) { viewModel.getParsedLyricsForCurrentTrack() }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Find current active LRC lyric index based on player millisecond position
    val activeIndex = remember(currentPosition, parsedLines) {
        parsedLines.indexOfLast { currentPosition >= it.timestampMs }
    }

    // Auto-scroll logic matching position transitions
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && parsedLines.isNotEmpty()) {
            scope.launch {
                lazyListState.animateScrollToItem(activeIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BlurGlass)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackTap) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Live Lyrics Sync",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = currentTrack?.title ?: "No track playing",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (parsedLines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Lyrics not found for this stream.",
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("lyrics_sync_scroller")
            ) {
                itemsIndexed(parsedLines) { idx, line ->
                    val isActive = idx == activeIndex
                    val scale by animateFloatAsState(
                        targetValue = if (isActive) 1.05f else 0.95f,
                        animationSpec = spring(),
                        label = "lyric_scale"
                    )

                    Text(
                        text = line.text,
                        color = if (isActive) AccentGreen else Color.White.copy(alpha = 0.45f),
                        fontSize = 20.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .rotate(if (isActive) -1f else 0f)
                            .padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

// 6. DETAILED FULL-SCREEN PLAYER SCREEN (Inspired by JioSaavn Design)
@Composable
fun PlayerDetailScreen(
    viewModel: MusicViewModel,
    onBackTap: () -> Unit,
    onLyricsTap: () -> Unit,
    onQueueTap: () -> Unit = {}
) {
    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.playerManager.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.playerManager.duration.collectAsStateWithLifecycle()

    val isFavorite = currentTrack?.isFavorite ?: false
    val isDownloaded = currentTrack?.isDownloaded ?: false

    val shuffleEnabled by viewModel.playerManager.shuffleEnabled.collectAsStateWithLifecycle()
    val repeatMode by viewModel.playerManager.repeatMode.collectAsStateWithLifecycle()

    val selectedQuality by viewModel.playerManager.selectedQuality.collectAsStateWithLifecycle()
    val activeQualityInUse by viewModel.playerManager.activeQualityInUse.collectAsStateWithLifecycle()
    var showQualitySelector by remember { mutableStateOf(false) }

    // Smooth vinyl rotational continuous animation
    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotator")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    if (currentTrack == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No music is currently selected.", color = Color.White)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackTap) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (currentTrack!!.isRadio) "LIVE BROADCAST" else "NOW PLAYING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    letterSpacing = 2.sp
                )
                Text(
                    text = currentTrack!!.album,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onQueueTap,
                    modifier = Modifier.testTag("player_queue_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.QueueMusic,
                        contentDescription = "Playback Queue",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { viewModel.toggleFavorite(currentTrack!!) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "FavTag",
                        tint = if (isFavorite) AccentGreen else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large procedural rotating cover representing high-performance disc players
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .rotate(if (isPlaying && !currentTrack!!.isRadio) rotationAngle else 0f)
        ) {
            // Vinyl Outer Shadow Frame
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                // Procedural Gradient Disk Content
                ProceduralCover(
                    coverId = currentTrack!!.coverUrl,
                    titleLetter = currentTrack!!.title,
                    isLarge = true,
                    isRadio = currentTrack!!.isRadio,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Central Vinyl grooves hole
            Canvas(modifier = Modifier.size(52.dp)) {
                drawCircle(color = Color.Black)
                drawCircle(color = AccentGreen, radius = 6.dp.toPx(), style = Stroke(width = 2.dp.toPx()))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title and Subtitle Row
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentTrack!!.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentTrack!!.artist,
                color = TextSecondary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PremiumCharcoal.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, if (selectedQuality == AudioQuality.AUTO) AccentGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f)),
                modifier = Modifier
                    .clickable { showQualitySelector = true }
                    .testTag("quality_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "Quality",
                        tint = if (selectedQuality == AudioQuality.AUTO) AccentGreen else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedQuality == AudioQuality.AUTO) "Auto • ${activeQualityInUse.bitrate}" else activeQualityInUse.label,
                        color = if (selectedQuality == AudioQuality.AUTO) AccentGreen else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Live Audio Equalizer dance analyzer (animated lines visualizer)
        AnimatedEqualizerVisualizer(isAnimating = isPlaying, viewModel = viewModel)

        // Custom seek slider bar
        Column(modifier = Modifier.fillMaxWidth()) {
            if (!currentTrack!!.isRadio) {
                var localSliderPos by remember { mutableStateOf<Float?>(null) }
                val displayPos = localSliderPos ?: currentPosition.toFloat()

                Slider(
                    value = displayPos,
                    onValueChange = { localSliderPos = it },
                    onValueChangeFinished = {
                        localSliderPos?.let {
                            viewModel.playerManager.seekTo(it.toLong())
                        }
                        localSliderPos = null
                    },
                    valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGreen,
                        activeTrackColor = AccentGreen,
                        inactiveTrackColor = MediumGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_seekbar")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(displayPos.toLong()), color = TextSecondary, fontSize = 11.sp)
                    Text(formatTime(duration), color = TextSecondary, fontSize = 11.sp)
                }
            } else {
                Text(
                    text = "Live Stream Buffered - Tap lyrics below to read live captions",
                    color = AccentGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                )
            }
        }

        // Play controllers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.playerManager.toggleShuffle() },
                modifier = Modifier.testTag("shuffle_btn")
            ) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleEnabled) AccentGreen else Color.White
                )
            }

            IconButton(
                onClick = { viewModel.playerManager.skipToPrevious() },
                modifier = Modifier.testTag("prev_btn")
            ) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
            }

            // Big play/pause controller circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AccentGreen)
                    .clickable {
                        if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                    }
                    .testTag("play_pause_detail_btn")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "PlayPause",
                    tint = Color.Black,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(
                onClick = { viewModel.playerManager.skipToNext() },
                modifier = Modifier.testTag("next_btn")
            ) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
            }

            IconButton(
                onClick = { viewModel.playerManager.toggleRepeatMode() },
                modifier = Modifier.testTag("repeat_btn")
            ) {
                val tint = if (repeatMode != RepeatMode.NONE) AccentGreen else Color.White
                val icon = if (repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat
                Icon(icon, contentDescription = "Repeat", tint = tint)
            }
        }

        // Lyrics mini-translucent banner matching modern design
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PremiumCharcoal),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLyricsTap() }
                .testTag("lyrics_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.TextFormat,
                        contentDescription = "LyricsIcon",
                        tint = AccentGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Synchronized Live Lyrics",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "ArrowLyric",
                    tint = TextSecondary
                )
            }
        }

        if (showQualitySelector) {
            AlertDialog(
                onDismissRequest = { showQualitySelector = false },
                containerColor = PremiumCharcoal,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Tune, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Audio Streaming Quality", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Customize your audio bitrates to save mobile cellular packets or experience studio-grade high fidelity CD-quality Masters.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Text("Real-time Connection Scanner", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viewModel.playerManager.getNetworkSpeedLabel(),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Resolved Bitrate: ${activeQualityInUse.bitrate} • ${activeQualityInUse.label}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        AudioQuality.values().forEach { quality ->
                            val isSelected = selectedQuality == quality
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AccentGreen.copy(alpha = 0.08f) else Color.Transparent)
                                    .clickable {
                                        viewModel.playerManager.setAudioQuality(quality)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = quality.label,
                                        color = if (isSelected) AccentGreen else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = quality.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.playerManager.setAudioQuality(quality) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AccentGreen,
                                        unselectedColor = Color.White.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQualitySelector = false }) {
                        Text("Apply & Close", color = AccentGreen, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

// Subcomponent: Live Equalizer Dancing bars view
@Composable
fun AnimatedEqualizerVisualizer(
    isAnimating: Boolean,
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val eqBands by viewModel.playerManager.equalizerBands.collectAsStateWithLifecycle()
    val eqEnabled by viewModel.playerManager.equalizerEnabled.collectAsStateWithLifecycle()

    val count = 24
    val barHeights = remember { mutableStateListOf<Float>().apply { repeat(count) { add(4f) } } }

    LaunchedEffect(isAnimating, currentTrack, eqBands, eqEnabled) {
        var lastTime = System.currentTimeMillis()
        while (isActive) {
            val now = System.currentTimeMillis()
            val dt = (now - lastTime) / 1000f
            lastTime = now

            val t = now / 1000f
            val trackHash = currentTrack?.id?.hashCode() ?: 0
            val trackSpeedFactor = 1.0f + (Math.abs(trackHash) % 5) * 0.2f
            val trackPhaseOffset = (Math.abs(trackHash) % 10) * 0.5f

            for (i in 0 until count) {
                val target = if (isAnimating) {
                    val wave1 = sin(t * 8f * trackSpeedFactor + i * 0.4f + trackPhaseOffset)
                    val wave2 = cos(t * 14f * trackSpeedFactor - i * 0.6f + trackPhaseOffset)
                    val wave3 = sin(t * 22f + i * 0.8f)
                    val rawHeight = (wave1 * 0.45f + wave2 * 0.35f + wave3 * 0.2f) * 0.5f + 0.5f

                    val hz = when (i) {
                        in 0..3 -> 60
                        in 4..7 -> 230
                        in 8..12 -> 910
                        in 13..17 -> 4000
                        else -> 14000
                    }
                    val eqFactor = if (eqEnabled) {
                        val db = eqBands[hz] ?: 0
                        1.0f + (db / 12f) * 0.8f
                    } else {
                        1.0f
                    }

                    (4f + (40f - 4f) * rawHeight * eqFactor).coerceIn(4f, 40f)
                } else {
                    4f
                }

                val k = if (isAnimating) 15f else 10f
                val currentVal = barHeights[i]
                val newVal = currentVal + (target - currentVal) * (k * dt).coerceAtMost(1f)
                barHeights[i] = newVal
            }

            delay(16)
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .height(48.dp)
            .padding(vertical = 4.dp)
    ) {
        for (i in 0 until count) {
            val h = barHeights[i]
            val barColor = remember(i) {
                when (i) {
                    in 0..3 -> Brush.verticalGradient(listOf(Color(0xFFE91E63), AccentGreen))
                    in 4..12 -> Brush.verticalGradient(listOf(Color(0xFF00BCD4), AccentGreen))
                    else -> Brush.verticalGradient(listOf(Color(0xFFFF9800), AccentGreen))
                }
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(topStart = 1.5.dp, topEnd = 1.5.dp))
                    .background(barColor)
            )
        }
    }
}

val DpProverter = TwoWayConverter<androidx.compose.ui.unit.Dp, AnimationVector1D>(
    convertToVector = { AnimationVector1D(it.value) },
    convertFromVector = { androidx.compose.ui.unit.Dp(it.value) }
)

// Helper: Formatter minutes & seconds
private fun formatTime(ms: Long): String {
    if (ms < 0) return "--:--"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}

// 7. PLAYLIST DETAIL SCREEN
@Composable
fun PlaylistDetailScreen(
    viewModel: MusicViewModel,
    playlistName: String,
    playlistDesc: String,
    onPlayAll: (List<Track>) -> Unit,
    onTrackSelected: (Track, List<Track>) -> Unit,
    onBackTap: () -> Unit
) {
    val playlistTracks by viewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.playerManager.isPlaying.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackTap) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = playlistName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = playlistDesc,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Play All and Add tracks buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onPlayAll(playlistTracks) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("playlist_play_all")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play All", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (playlistTracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "This playlist is currently empty.",
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Add standard trending songs to populate it instantly for great testing experience
                    Button(
                        onClick = {
                            val activeId = viewModel.selectedPlaylist.value?.id ?: 0
                            allTracks.take(3).forEach {
                                viewModel.addTrackToPlaylist(activeId, it.id)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MediumGray)
                    ) {
                        Text("Add Trending Tracks List", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(playlistTracks) { track ->
                    val isActive = currentTrack?.id == track.id
                    val isSongPlaying = isActive && isPlaying
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isActive) {
                                    if (isPlaying) viewModel.playerManager.pause() else viewModel.playerManager.resume()
                                } else {
                                    onTrackSelected(track, playlistTracks)
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(48.dp)
                        ) {
                            ProceduralCover(
                                coverId = track.coverUrl,
                                titleLetter = track.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSongPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "Active",
                                        tint = AccentGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                track.title,
                                color = if (isActive) AccentGreen else Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(track.artist, color = TextSecondary, fontSize = 12.sp)
                        }

                        // Remove icon
                        IconButton(
                            onClick = {
                                viewModel.selectedPlaylist.value?.let { current ->
                                    viewModel.removeTrackFromPlaylist(current.id, track)
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.61f))
                        }
                    }
                }
            }
        }
    }
}

// 8. COMMONS / SUB-WIDGET COMPONENTS

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPauseToggle: () -> Unit,
    onSkipNext: () -> Unit,
    onMiniPlayerTap: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCharcoal),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onMiniPlayerTap() }
            .testTag("mini_player_tap_area")
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProceduralCover(
                    coverId = track.coverUrl,
                    titleLetter = track.title,
                    isRadio = track.isRadio,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayPauseToggle,
                        modifier = Modifier.testTag("mini_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "PlayPauseMini",
                            tint = AccentGreen
                        )
                    }
                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.testTag("mini_next")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "SkipNextMini",
                            tint = Color.White
                        )
                    }
                }
            }

            // Real-time micro progress bar under the mini-player (Only for regular tracks, not radio streams)
            if (!track.isRadio) {
                val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    color = AccentGreen,
                    trackColor = MediumGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                )
            }
        }
    }
}

@Composable
fun SongRow(
    track: Track,
    onRowClick: () -> Unit,
    onFavClick: () -> Unit,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    isRadioMode: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(52.dp)
        ) {
            ProceduralCover(
                coverId = track.coverUrl,
                titleLetter = track.title,
                isRadio = track.isRadio,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Active PlayState",
                        tint = AccentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isActive) AccentGreen else Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isRadioMode) {
            // Shiny neon red indicator for active radio broadcast streams
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("LIVE", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        IconButton(
            onClick = onFavClick,
            modifier = Modifier.testTag("fav_btn_${track.id}")
        ) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "FavoriteIcon",
                tint = if (track.isFavorite) AccentGreen else TextSecondary
            )
        }
    }
}

@Composable
fun PlaylistCardCell(
    playlist: Playlist,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCharcoal),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentGreen.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Filled.QueueMusic, contentDescription = "Queue", tint = AccentGreen)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = if (playlist.songCount == 1) "1 trace" else "${playlist.songCount} tracks",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = TextSecondary
            )
        }
    }
}

@Composable
fun PlaylistRow(
    playlist: Playlist,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MediumGray)
            ) {
                Icon(Icons.Filled.QueueMusic, contentDescription = "PlaylistIcon", tint = AccentGreen)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text("${playlist.songCount} items", color = TextSecondary, fontSize = 11.sp)
            }
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun EmptyResultState(message: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "EmptyState",
            tint = TextSecondary,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// 9. CREATE PLAYLIST SUB-DIALOG
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PremiumCharcoal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "New Playlist",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = AccentGreen,
                        focusedBorderColor = AccentGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("playlist_name_field")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = AccentGreen,
                        focusedBorderColor = AccentGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("playlist_desc_field")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name, desc) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SecureLoginScreen(viewModel: MusicViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Navigation and dialog toggles
    var isRegisterMode by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmailAddress by remember { mutableStateOf("") }
    var resetStatusMessage by remember { mutableStateOf<String?>(null) }
    var isResetError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B132B), // Deep Space Blue
                        Color(0xFF1C2541), // Galaxy Blue-Gray
                        DeepBlack
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // High fidelity decorative glowing particle canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            drawCircle(
                color = AccentGreen.copy(alpha = 0.05f),
                radius = w * 0.7f,
                center = androidx.compose.ui.geometry.Offset(w / 2f, h * 0.15f),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = AccentGreen.copy(alpha = 0.03f),
                radius = w * 1.0f,
                center = androidx.compose.ui.geometry.Offset(w / 2f, h * 0.15f),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Frosted Card Container
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(28.dp))
                .background(PremiumCharcoal)
                .border(BorderStroke(1.dp, MediumGray), RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visual Head Icon representing security & flow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CardGreen)
            ) {
                Icon(
                    imageVector = if (isRegisterMode) Icons.Default.Shield else Icons.Default.Lock,
                    contentDescription = "Security Status Image",
                    tint = AccentGreen,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = if (isRegisterMode) "Create Free Account" else "Sign In to BeatStream",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isRegisterMode)
                    "Access unlimited playlists, offline sync, and sound customization by creating an account."
                    else "Experience high fidelity sound quality with adaptive bitrate streaming.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email Address Input Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    statusMessage = null
                },
                label = { Text("Email Address") },
                placeholder = { Text("your.email@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input"),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = AccentGreen)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = MediumGray,
                    focusedLabelColor = AccentGreen,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Password Input Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    statusMessage = null
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = AccentGreen)
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = TextSecondary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = MediumGray,
                    focusedLabelColor = AccentGreen,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Helper features: Remember me & Forgot Password Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Remember Me Option
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { rememberMe = !rememberMe }
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        modifier = Modifier.testTag("remember_me_checkbox"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentGreen,
                            uncheckedColor = TextSecondary,
                            checkmarkColor = DeepBlack
                        )
                    )
                    Text(
                        text = "Remember me",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Forgot Password text link
                if (!isRegisterMode) {
                    Text(
                        text = "Forgot Password?",
                        color = AccentGreen,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .clickable {
                                resetEmailAddress = email
                                resetStatusMessage = null
                                showForgotPasswordDialog = true
                            }
                            .testTag("forgot_password_btn")
                    )
                }
            }

            // High Fidelity Animated Info/Status/Error notification
            AnimatedVisibility(
                visible = statusMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = if (isError) Color(0xFFEF4444) else AccentGreen,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Main Action Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        statusMessage = "Please fulfill all required mail and passcode values."
                        isError = true
                        return@Button
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        statusMessage = "Please specify a valid email address."
                        isError = true
                        return@Button
                    }
                    isLoading = true
                    statusMessage = "Authenticating secure channel..."
                    isError = false

                    if (isRegisterMode) {
                        viewModel.firebaseRegister(email.trim(), password) { success, message ->
                            isLoading = false
                            isError = !success
                            statusMessage = message
                            if (success) {
                                // Auto login upon registering the credentials
                                viewModel.firebaseLogin(email.trim(), password, rememberMe) { _, _ -> }
                            }
                        }
                    } else {
                        viewModel.firebaseLogin(email.trim(), password, rememberMe) { success, message ->
                            isLoading = false
                            isError = !success
                            statusMessage = message
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(if (isRegisterMode) "firebase_register_action" else "firebase_login_action"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = DeepBlack
                ),
                shape = RoundedCornerShape(14.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = DeepBlack,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) "Register Account" else "Authorize Access",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Social Logins Divider Label
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MediumGray)
                Text(
                    text = " OR SIGN IN WITH ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MediumGray)
            }

            // Modern, Dark-Mode Social Login Option Buttons list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Google Sign In Mock-Less action button
                Button(
                    onClick = {
                        statusMessage = "Google identity requested. (Simulating SSO redirection...)"
                        isError = false
                        viewModel.attemptLogin("admin", "admin123", rememberMe)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("google_sso_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MediumGray),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Google Play Icon Badge",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Google", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }

                // GitHub Sign In Mock-Less action button
                Button(
                    onClick = {
                        statusMessage = "GitHub identity requested. (Simulating OAuth redirection...)"
                        isError = false
                        viewModel.attemptLogin("admin", "admin123", rememberMe)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("github_sso_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MediumGray),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "GitHub Badge Icon",
                            tint = AccentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GitHub", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Footer Navigation link toggling between login and sign up mode
            Text(
                text = if (isRegisterMode) "Already have an account? Sign In" else "New to BeatStream? Register Now",
                color = AccentGreen,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .clickable {
                        isRegisterMode = !isRegisterMode
                        statusMessage = null
                        isError = false
                    }
                    .padding(vertical = 4.dp)
                    .testTag("auth_mode_toggle_btn")
            )
        }
    }

    // High fidelity Modern Forgot Password Dialog Screen
    if (showForgotPasswordDialog) {
        Dialog(
            onDismissRequest = { showForgotPasswordDialog = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(PremiumCharcoal)
                    .border(BorderStroke(1.dp, MediumGray), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CardGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Forgot Password Shield",
                            tint = AccentGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Reset Private Key",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Provide your credential email below. If associated with an active Firebase account, we will dispatch a reset link.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        textAlign = TextAlign.Center
                    )

                    // Email Field
                    OutlinedTextField(
                        value = resetEmailAddress,
                        onValueChange = {
                            resetEmailAddress = it
                            resetStatusMessage = null
                        },
                        label = { Text("Associated Email") },
                        placeholder = { Text("name@example.com") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = MediumGray,
                            focusedLabelColor = AccentGreen,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Status Notification messages
                    AnimatedVisibility(visible = resetStatusMessage != null) {
                        resetStatusMessage?.let { msg ->
                            Text(
                                text = msg,
                                color = if (isResetError) Color(0xFFEF4444) else AccentGreen,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Action buttons Row inside Dialog
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Dismiss button
                        OutlinedButton(
                            onClick = { showForgotPasswordDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("cancel_reset_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, MediumGray),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dismiss")
                        }

                        // Reset confirmation action
                        Button(
                            onClick = {
                                if (resetEmailAddress.isBlank()) {
                                    resetStatusMessage = "Please input your active email."
                                    isResetError = true
                                    return@Button
                                }
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmailAddress.trim()).matches()) {
                                    resetStatusMessage = "Please specify a valid email address."
                                    isResetError = true
                                    return@Button
                                }
                                viewModel.firebaseResetPassword(resetEmailAddress.trim()) { success, msg ->
                                    isResetError = !success
                                    resetStatusMessage = msg
                                    if (success) {
                                        // Auto dismiss dialog shortly after successful initiation
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("confirm_reset_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGreen,
                                contentColor = DeepBlack
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// 7. PROFILE SCREEN LAYOUT (High-fidelity custom dark aesthetic)
@Composable
fun ProfileScreen(
    viewModel: MusicViewModel,
    onBackTap: () -> Unit
) {
    val displayName by viewModel.userDisplayName.collectAsStateWithLifecycle()
    val experienceTier by viewModel.userExperienceTier.collectAsStateWithLifecycle()
    val avatarIndex by viewModel.userAvatarIndex.collectAsStateWithLifecycle()
    val currentQuality by viewModel.playerManager.selectedQuality.collectAsStateWithLifecycle()
    val emailString = viewModel.currentUserEmail
    
    var tempName by remember { mutableStateOf(displayName) }
    var showSavedToast by remember { mutableStateOf(false) }
    
    val avatarColors = listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF3B82F6))
    val avatarColor = avatarColors[avatarIndex % avatarColors.size]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackTap,
                    modifier = Modifier.testTag("profile_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "User Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            
            // Save Profile Button
            Button(
                onClick = {
                    viewModel.updateUserProfile(tempName.trim().ifEmpty { "Explorer" }, experienceTier, avatarIndex)
                    showSavedToast = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = DeepBlack),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom interactive profile badge with gradient and visual status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PremiumCharcoal),
            border = BorderStroke(1.dp, MediumGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive avatar picker
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(avatarColor, avatarColor.copy(alpha = 0.4f), avatarColor)
                            )
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF161616))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = avatarColor,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Color Dot Avatar Index Picker
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    avatarColors.forEachIndexed { idx, color ->
                        val isSelected = idx == avatarIndex % avatarColors.size
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.updateUserProfile(tempName, experienceTier, idx)
                                }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = experienceTier,
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Account Details Section
        Text(
            text = "ACCOUNT DETAILS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGreen,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PremiumCharcoal)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Email Field (Read Only with status)
                Column {
                    Text("Registered Login Email", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = emailString, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x3310B981)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Verified", 
                                color = Color(0xFF10B981), 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = MediumGray.copy(alpha = 0.3f))
                
                // Username Editing field
                Column {
                    Text("Display Name", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input"),
                        placeholder = { Text("E.g. Sonic Wanderer", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = MediumGray,
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Preferences Section
        Text(
            text = "SAVED PREFERENCES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGreen,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PremiumCharcoal)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Streaming Quality Selector
                Column {
                    Text("Streaming Audio Bitrate", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(AudioQuality.LOW, AudioQuality.MEDIUM, AudioQuality.HIGH, AudioQuality.AUTO).forEach { qual ->
                            val isSelected = qual == currentQuality
                            val chipBg = if (isSelected) AccentGreen else Color.Black.copy(alpha = 0.35f)
                            val chipTextCol = if (isSelected) DeepBlack else Color.White
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .clickable { viewModel.playerManager.setAudioQuality(qual) }
                                    .padding(vertical = 8.dp)
                                    .testTag("quality_chip_${qual.name}")
                            ) {
                                Text(
                                    text = when (qual) {
                                        AudioQuality.LOW -> "Low"
                                        AudioQuality.MEDIUM -> "Med"
                                        AudioQuality.HIGH -> "High"
                                        AudioQuality.AUTO -> "Auto"
                                    },
                                    color = chipTextCol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentQuality.description,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                HorizontalDivider(color = MediumGray.copy(alpha = 0.3f))
                
                // Account Level Selection
                Column {
                    Text("Account Experience Level", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val tiers = listOf("Premium Member", "Audiophile Pro", "Studio Master")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tiers.forEach { tier ->
                            val isSelected = experienceTier.contains(tier)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) AccentGreen else Color.Black.copy(alpha = 0.35f))
                                    .clickable {
                                        viewModel.updateUserProfile(tempName, "BeatStream $tier", avatarIndex)
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = tier.replace(" Member", ""),
                                    color = if (isSelected) DeepBlack else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign Out/Lock Control Row
        Button(
            onClick = {
                viewModel.lockApp()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sign_out_lock_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock Player Session", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Success Toast Notification simulation inside view
        if (showSavedToast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2E7D32))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Profile updates successfully registered!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            LaunchedEffect(Unit) {
                delay(3000)
                showSavedToast = false
            }
        }
    }
}

// 8. QUEUE SCREEN LAYOUT (View, reorder, remove tracks)
@Composable
fun QueueScreen(
    viewModel: MusicViewModel,
    onBackTap: () -> Unit
) {
    val queue by viewModel.playerManager.playlistQueue.collectAsStateWithLifecycle()
    val currentTrack by viewModel.playerManager.currentTrack.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp)
    ) {
        // Queue Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackTap,
                    modifier = Modifier.testTag("queue_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upcoming Queue",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            
            if (queue.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.playerManager.clearQueue() },
                    modifier = Modifier.testTag("clear_queue_btn")
                ) {
                    Text(
                        text = "Clear All",
                        color = Color.Red.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your queue is currently empty.",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select any track or playlist to start playing music.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(queue) { index, track ->
                    val isCurrent = track.id == currentTrack?.id
                    val itemBorder = if (isCurrent) BorderStroke(1.dp, AccentGreen) else BorderStroke(1.dp, PremiumCharcoal)
                    val cardBg = if (isCurrent) CardGreen else PremiumCharcoal
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("queue_item_$index"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = itemBorder
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cover Art placeholder with index / letter
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF262626)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (track.title.isNotBlank()) track.title.take(1).uppercase() else "♫",
                                        color = if (isCurrent) AccentGreen else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isCurrent) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(AccentGreen)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = track.title,
                                            color = Color.White,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${track.artist} • ${track.album}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Edit Queue actions (Reorder up/down and Remove)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Up button (disabled for index 0)
                                IconButton(
                                    onClick = { viewModel.playerManager.reorderQueue(index, index - 1) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) Color.White else Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                // Down button (disabled for last item)
                                IconButton(
                                    onClick = { viewModel.playerManager.reorderQueue(index, index + 1) },
                                    enabled = index < queue.size - 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Move Down",
                                        tint = if (index < queue.size - 1) Color.White else Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                
                                // Remove button
                                IconButton(
                                    onClick = { viewModel.playerManager.removeFromQueue(index) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("remove_queue_item_$index")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.Red.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
