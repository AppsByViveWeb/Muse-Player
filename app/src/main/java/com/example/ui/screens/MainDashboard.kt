package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Playlist
import com.example.data.model.PlaylistWithTracks
import com.example.data.model.Track
import com.example.player.EqualizerBand
import com.example.viewmodel.MusicViewModel
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Músicas", "Playlists", "Equalizador")
    val icons = listOf(Icons.Default.MusicNote, Icons.Default.QueueMusic, Icons.Default.GraphicEq)

    // Room lists
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val allPlaylists by viewModel.allPlaylists.collectAsStateWithLifecycle()
    val playlistsWithTracks by viewModel.playlistsWithTracks.collectAsStateWithLifecycle()

    // Player States
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val progress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val isShuffle by viewModel.isShuffle.collectAsStateWithLifecycle()
    val isRepeat by viewModel.isRepeat.collectAsStateWithLifecycle()
    val isSingleTrackLoop by viewModel.isSingleTrackLoop.collectAsStateWithLifecycle()

    // Dialog state controllers
    var showAddTrackDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var activePlaylistDetail by remember { mutableStateOf<PlaylistWithTracks?>(null) }
    var showFullPlayer by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "MUSE PLAYER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { showAddTrackDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Adicionar Música",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (selectedTab == 1) {
                        IconButton(onClick = { showCreatePlaylistDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Nova Playlist",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Column {
                // Persistent mini player shown if a song is loaded
                currentTrack?.let { track ->
                    MiniPlayer(
                        track = track,
                        isPlaying = isPlaying,
                        progress = progress,
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onNext = { viewModel.playNext() },
                        onClick = { showFullPlayer = true }
                    )
                }

                // Minimalist Bottom Tab Navigation Rule
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp
                ) {
                    tabs.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = icons[index],
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> SongsTab(
                    tracks = allTracks,
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    playlists = allPlaylists,
                    onTrackClick = { track ->
                        viewModel.playTrack(track, allTracks)
                    },
                    onAddToPlaylist = { playlistId, trackId ->
                        viewModel.addTrackToPlaylist(playlistId, trackId)
                    },
                    onDeleteTrack = { trackId ->
                        viewModel.deleteTrack(trackId)
                    }
                )
                1 -> PlaylistsTab(
                    playlistsWithTracks = playlistsWithTracks,
                    onPlaylistClick = { playlist ->
                        activePlaylistDetail = playlist
                    },
                    onDeletePlaylist = { playlistId ->
                        viewModel.deletePlaylist(playlistId)
                    }
                )
                2 -> EqualizerTab(viewModel = viewModel)
            }
        }
    }

    // Modal Dialog to import tracks
    if (showAddTrackDialog) {
        AddTrackDialog(
            onDismiss = { showAddTrackDialog = false },
            onAdd = { title, artist, url ->
                viewModel.addTrack(title, artist, url)
                showAddTrackDialog = false
            }
        )
    }

    // Modal Dialog to create playlist
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { name, desc ->
                viewModel.createPlaylist(name, desc)
                showCreatePlaylistDialog = false
            }
        )
    }

    // Playlist Details Screen (Overlay sheet/view)
    activePlaylistDetail?.let { playlist ->
        // Retrieve live update of this specific playlist to react correctly
        val livePlaylist = playlistsWithTracks.find { it.playlist.id == playlist.playlist.id }
        if (livePlaylist == null) {
            activePlaylistDetail = null
        } else {
            PlaylistDetailView(
                playlistWithTracks = livePlaylist,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                onDismiss = { activePlaylistDetail = null },
                onPlayPlaylist = {
                    viewModel.playPlaylist(livePlaylist)
                },
                onTrackClick = { track ->
                    viewModel.playTrack(track, livePlaylist.tracks)
                },
                onRemoveTrack = { trackId ->
                    viewModel.removeTrackFromPlaylist(livePlaylist.playlist.id, trackId)
                }
            )
        }
    }

    // Full immersive bottom-sheet player
    if (showFullPlayer && currentTrack != null) {
        FullPlayerView(
            track = currentTrack!!,
            isPlaying = isPlaying,
            progress = progress,
            isShuffle = isShuffle,
            isRepeat = isRepeat,
            isSingleTrackLoop = isSingleTrackLoop,
            onPlayPauseToggle = { viewModel.togglePlayPause() },
            onNext = { viewModel.playNext() },
            onPrevious = { viewModel.playPrevious() },
            onSeek = { pos -> viewModel.seekTo(pos) },
            onToggleShuffle = { viewModel.toggleShuffle() },
            onToggleRepeat = { viewModel.toggleRepeat() },
            onToggleSingleTrackLoop = { viewModel.toggleSingleEmptyTrackLoop() },
            onDismiss = { showFullPlayer = false }
        )
    }
}

// ---------------- SONGS LIST VIEW ----------------

@Composable
fun SongsTab(
    tracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    playlists: List<Playlist>,
    onTrackClick: (Track) -> Unit,
    onAddToPlaylist: (Long, Long) -> Unit,
    onDeleteTrack: (Long) -> Unit
) {
    if (tracks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Nenhuma música cadastrada",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "Toque no ícone '+' acima para adicionar.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    isSelected = currentTrack?.id == track.id,
                    isPlaying = isPlaying,
                    playlists = playlists,
                    onClick = { onTrackClick(track) },
                    onAddToPlaylist = { playlistId -> onAddToPlaylist(playlistId, track.id) },
                    onDelete = if (!track.isDemo) { { onDeleteTrack(track.id) } } else null
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackListItem(
    track: Track,
    isSelected: Boolean,
    isPlaying: Boolean,
    playlists: List<Playlist>,
    onClick: () -> Unit,
    onAddToPlaylist: (Long) -> Unit,
    onDelete: (() -> Unit)?
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { menuOpen = true }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded icon displaying music cover
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isSelected) {
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            } else {
                                listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected && isPlaying) {
                    AudioWaveAnimation()
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = track.artist,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = track.durationString,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opções",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    if (playlists.isNotEmpty()) {
                        Text(
                            "Adicionar à Playlist:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        playlists.forEach { playlist ->
                            DropdownMenuItem(
                                text = { Text(playlist.name, fontSize = 14.sp) },
                                onClick = {
                                    onAddToPlaylist(playlist.id)
                                    menuOpen = false
                                }
                            )
                        }
                    } else {
                        DropdownMenuItem(
                            text = { Text("Nenhuma playlist criada", fontSize = 14.sp) },
                            enabled = false,
                            onClick = {}
                        )
                    }

                    onDelete?.let { deleteAction ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Excluir Música", color = MaterialTheme.colorScheme.error, fontSize = 14.sp) },
                            onClick = {
                                deleteAction()
                                menuOpen = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// ---------------- PLAYLISTS TAB VIEW ----------------

@Composable
fun PlaylistsTab(
    playlistsWithTracks: List<PlaylistWithTracks>,
    onPlaylistClick: (PlaylistWithTracks) -> Unit,
    onDeletePlaylist: (Long) -> Unit
) {
    if (playlistsWithTracks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.QueueMusic,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Nenhuma playlist disponível",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    "Crie uma nova tocando no ícone '+' acima.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(playlistsWithTracks) { playlist ->
                PlaylistGridItem(
                    playlistTracks = playlist,
                    onClick = { onPlaylistClick(playlist) },
                    onDelete = { onDeletePlaylist(playlist.playlist.id) }
                )
            }
        }
    }
}

@Composable
fun PlaylistGridItem(
    playlistTracks: PlaylistWithTracks,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Stack representation of album cover
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = playlistTracks.playlist.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${playlistTracks.tracks.size} músicas",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Excluir Playlist", color = MaterialTheme.colorScheme.error, fontSize = 14.sp) },
                        onClick = {
                            onDelete()
                            menuOpen = false
                        }
                    )
                }
            }
        }
    }
}

// ---------------- PLAYLIST DETAIL WINDOW ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailView(
    playlistWithTracks: PlaylistWithTracks,
    currentTrack: Track?,
    isPlaying: Boolean,
    onDismiss: () -> Unit,
    onPlayPlaylist: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onRemoveTrack: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(playlistWithTracks.playlist.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            playlistWithTracks.playlist.description.ifEmpty { "Playlist" },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (playlistWithTracks.tracks.isNotEmpty()) {
                        Button(
                            onClick = onPlayPlaylist,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tocar", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (playlistWithTracks.tracks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Playlist vazia",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            "Adicione músicas através da aba principal.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playlistWithTracks.tracks) { track ->
                        var subMenuOpen by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackClick(track) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentTrack?.id == track.id) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = if (currentTrack?.id == track.id) {
                                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                                } else {
                                                    listOf(Color(0xFF23314B), Color(0xFF141D2D))
                                                }
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (currentTrack?.id == track.id && isPlaying) {
                                        AudioWaveAnimation(size = 18)
                                    } else {
                                        Icon(
                                            Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (currentTrack?.id == track.id) Color.Black else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (currentTrack?.id == track.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = track.artist,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                Text(
                                    text = track.durationString,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Box {
                                    IconButton(onClick = { subMenuOpen = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Configuração",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = subMenuOpen,
                                        onDismissRequest = { subMenuOpen = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Remover da Playlist", color = MaterialTheme.colorScheme.error, fontSize = 14.sp) },
                                            onClick = {
                                                onRemoveTrack(track.id)
                                                subMenuOpen = false
                                            }
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
}

// ---------------- EQUALIZER SCREEN VIEW ----------------

@Composable
fun EqualizerTab(viewModel: MusicViewModel) {
    val bands by viewModel.bands.collectAsStateWithLifecycle()
    val selectedPreset by viewModel.selectedPreset.collectAsStateWithLifecycle()
    val presets = viewModel.presets

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Equalizador de Frequências",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Ajuste as bandas ou selecione um preset padrão",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Horizontal Preset chip row
                ScrollableTabRow(
                    selectedTabIndex = maxOf(0, presets.indexOf(selectedPreset)),
                    edgePadding = 0.dp,
                    indicator = {},
                    divider = {},
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEach { preset ->
                        val isSelected = preset == selectedPreset
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.applyEqualizerPreset(preset) },
                            label = { Text(preset, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.Black,
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Visual board sliders representing physical bands
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            bands.forEach { band ->
                EqualizerBandSlider(
                    band = band,
                    onValueChange = { newMbValue ->
                        viewModel.setEqualizerBandLevel(band.id, newMbValue)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom bouncing interactive waveform visualizer (creates dynamic mood!)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val isPlayingState by viewModel.isPlaying.collectAsStateWithLifecycle()
                (1..18).forEach { index ->
                    BouncingBar(index = index, isPlaying = isPlayingState)
                }
            }
        }
    }
}

@Composable
fun EqualizerBandSlider(
    band: EqualizerBand,
    onValueChange: (Short) -> Unit
) {
    // Convert to Decibels for display range
    val currentDb = (band.currentLevel / 100).toFloat()
    val minDb = (band.minLevel / 100).toFloat()
    val maxDb = (band.maxLevel / 100).toFloat()

    // Render Center Frequencies nicely (e.g., center > 1000 rendered as kHz)
    val freqLabel = if (band.centerFrequency >= 1000) {
        String.format(Locale.US, "%.1fk", band.centerFrequency / 1000.0)
    } else {
        "${band.centerFrequency}"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight()
            .width(54.dp)
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = String.format(Locale.US, "%+.1fdB", currentDb),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Custom vertical Slider layout
        Box(
            modifier = Modifier
                .weight(1f)
                .width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = currentDb,
                valueRange = minDb..maxDb,
                onValueChange = { floatDb ->
                    val mB = (floatDb * 100).toInt().toShort()
                    onValueChange(mB)
                },
                modifier = Modifier
                    .height(200.dp)
                    .rotate(-90f)
                    .offset(x = 0.dp), // Compensate rotation shift
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = freqLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun BouncingBar(index: Int, isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "BouncingWave")
    val randomDelay = remember(index) { Random.nextInt(150, 450) }
    val heightMultiplier by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(randomDelay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BarHeight$index"
        )
    } else {
        remember { mutableStateOf(0.15f) }
    }

    Box(
        modifier = Modifier
            .width(6.dp)
            .fillMaxHeight(heightMultiplier)
            .clip(RoundedCornerShape(3.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    )
}

// ---------------- DIALOG COMPONENT SCREENS ----------------

@Composable
fun AddTrackDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Adicionar Música MP3",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; titleError = false },
                    label = { Text("Título da Música") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (titleError) {
                    Text("Título é obrigatório", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artista/Banda") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it; urlError = false },
                    label = { Text("Endereço URL (MP3)") },
                    isError = urlError,
                    singleLine = true,
                    placeholder = { Text("https://exemplo.com/audio.mp3") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (urlError) {
                    Text("URL do MP3 é obrigatório", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            var hasError = false
                            if (title.isBlank()) {
                                titleError = true
                                hasError = true
                            }
                            if (url.isBlank()) {
                                urlError = true
                                hasError = true
                            }
                            if (!hasError) {
                                onAdd(title, artist, url)
                            }
                        }
                    ) {
                        Text("Adicionar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Criar Nova Playlist",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Nome da Playlist") },
                    isError = nameError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError) {
                    Text("Nome é obrigatório", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descrição (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                            } else {
                                onCreate(name, desc)
                            }
                        }
                    ) {
                        Text("Criar", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ---------------- AUDIO MINI-WAVE ANIMATION ----------------

@Composable
fun AudioWaveAnimation(size: Int = 20) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
    val heights = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.15f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(220 + index * 90, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Height$index"
        )
    }

    Row(
        modifier = Modifier.size(size.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(height.value)
                    .background(Color.Black, shape = RoundedCornerShape(1.dp))
            )
        }
    }
}

// ---------------- PERSISTENT MINI PLAYER ----------------

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    progress: Long,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit
) {
    val progressFraction = if (track.duration > 0) {
        progress.toFloat() / track.duration.toFloat()
    } else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 12.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background mini-progress tracker line
            LinearProgressIndicator(
                progress = { progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Disk visualizer
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F1A28)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controls row
                IconButton(onClick = onPlayPauseToggle) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Tocar/Pausar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onNext) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Próxima",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// ---------------- IMMERSIVE FULL PLAYER SCREEN ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerView(
    track: Track,
    isPlaying: Boolean,
    progress: Long,
    isShuffle: Boolean,
    isRepeat: Boolean,
    isSingleTrackLoop: Boolean,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleSingleTrackLoop: () -> Unit,
    onDismiss: () -> Unit
) {
    val progressFraction = if (track.duration > 0) progress.toFloat() / track.duration.toFloat() else 0f

    // Animated rotating vinyl disk according to playback state
    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotate")
    val rotationAngle by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "Rotation"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFF16151A) // ElegantDarkSurface
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Dropdown arrow and Active Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Fechar Reprodutor",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "REPRODUZINDO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                IconButton(onClick = { /* Decorational info */ }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Sobre",
                        tint = Color.White.copy(alpha = 0.4f)
                    )
                }
            }

            // High Contrast glowing Vinyl disc setup
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF07070B))
                    .rotate(rotationAngle),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl outer ridges
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.95f)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(Color.DarkGray, Color.Black, Color.DarkGray, Color.Black)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Center Album Cover Sticker
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.45f)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Track detailed summary
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = track.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = track.artist,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Responsive Timeline Seek bars
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progressFraction,
                    onValueChange = { scaleValue ->
                        val targetMs = (scaleValue * track.duration).toLong()
                        onSeek(targetMs)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentLabel = String.format(
                        "%02d:%02d",
                        (progress / 1000) / 60,
                        (progress / 1000) % 60
                    )
                    Text(currentLabel, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text(track.durationString, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }

            // Heavy controls core setup (play, pause, skipping, toggles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle mode toggle
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription = "Modo Aleatório",
                        tint = if (isShuffle) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous song
                IconButton(onClick = onPrevious) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Música Anterior",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Core Play/Pause huge Floating FAB
                FloatingActionButton(
                    onClick = onPlayPauseToggle,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Tocar/Pausar",
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Next song
                IconButton(onClick = onNext) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Próxima Música",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Modes combined trigger
                IconButton(onClick = {
                    if (isRepeat) {
                        // Switch repeat off, turns single track loop on
                        onToggleRepeat()
                        onToggleSingleTrackLoop()
                    } else if (isSingleTrackLoop) {
                        // Switch single track loop off
                        onToggleSingleTrackLoop()
                    } else {
                        // If all off, turn repeat on
                        onToggleRepeat()
                    }
                }) {
                    val repeatIcon = if (isSingleTrackLoop) Icons.Default.RepeatOne else Icons.Default.Repeat
                    val repeatTint = if (isRepeat || isSingleTrackLoop) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.White.copy(alpha = 0.4f)
                    }

                    Icon(
                        repeatIcon,
                        contentDescription = "Modos de Repetição",
                        tint = repeatTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
