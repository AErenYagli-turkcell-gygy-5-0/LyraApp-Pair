package com.turkcell.lyraapp.ui.playlistdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.playlistdetail.PlaylistDetail
import com.turkcell.lyraapp.data.playlistdetail.PlaylistSong
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun PlaylistDetailRoute(
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PlaylistDetailEffect.NavigateBack -> onNavigateBack()
                is PlaylistDetailEffect.NavigateToNowPlaying -> onNavigateToNowPlaying()
                is PlaylistDetailEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    PlaylistDetailScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
fun PlaylistDetailScreen(
    state: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val playlist = state.playlist ?: return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                DetailTopBar(
                    onBack = { onIntent(PlaylistDetailIntent.BackClicked) },
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                CoverSection(
                    startColor = playlist.artworkStartColor,
                    endColor = playlist.artworkEndColor,
                    title = playlist.title,
                    description = playlist.description,
                    metaLine = "${playlist.ownerName} · ${playlist.songCount} şarkı · ${playlist.totalDuration}",
                )
            }
            item {
                Spacer(Modifier.height(20.dp))
                ActionRow(
                    onLike = { onIntent(PlaylistDetailIntent.LikePlaylistClicked) },
                    onDownload = { onIntent(PlaylistDetailIntent.DownloadClicked) },
                    onShuffle = { onIntent(PlaylistDetailIntent.ShuffleClicked) },
                    onPlayAll = { onIntent(PlaylistDetailIntent.PlayAllClicked) },
                )
                Spacer(Modifier.height(12.dp))
            }
            items(playlist.songs, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    isCurrentlyPlaying = song.id == state.currentlyPlayingId,
                    onClick = { onIntent(PlaylistDetailIntent.SongClicked(song.id)) },
                )
            }
        }
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = LyraIcons.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = MoreVertIcon,
                contentDescription = "Daha fazla",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CoverSection(
    startColor: Long,
    endColor: Long,
    title: String,
    description: String,
    metaLine: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(startColor), Color(endColor)),
                    ),
                ),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = metaLine,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionRow(
    onLike: () -> Unit,
    onDownload: () -> Unit,
    onShuffle: () -> Unit,
    onPlayAll: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onLike) {
            Icon(
                imageVector = LyraIcons.FavoriteOutlined,
                contentDescription = "Beğen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDownload) {
            Icon(
                imageVector = LyraIcons.Download,
                contentDescription = "İndir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = {}) {
            Icon(
                imageVector = AddCircleOutlineIcon,
                contentDescription = "Ekle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onShuffle) {
            Icon(
                imageVector = ShuffleIcon,
                contentDescription = "Karıştır",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onPlayAll),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = PlayIcon,
                contentDescription = "Çal",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun SongRow(
    song: PlaylistSong,
    isCurrentlyPlaying: Boolean,
    onClick: () -> Unit,
) {
    val rowBackground = if (isCurrentlyPlaying) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(song.artworkStartColor),
                            Color(song.artworkEndColor),
                        ),
                    ),
                ),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = song.duration,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = if (song.isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
            contentDescription = "Beğeni",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = MoreVertIcon,
            contentDescription = "Seçenekler",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun buildIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.Black),
    ).build()

private val MoreVertIcon: ImageVector by lazy {
    buildIcon(
        "MoreVert",
        "M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2z" +
            "M12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z" +
            "M12,16c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z",
    )
}

private val PlayIcon: ImageVector by lazy {
    buildIcon("Play", "M8,5v14l11,-7z")
}

private val ShuffleIcon: ImageVector by lazy {
    buildIcon(
        "Shuffle",
        "M10.59,9.17L5.41,4 4,5.41l5.17,5.17 1.42,-1.41z" +
            "M14.5,4l2.04,2.04L4,18.59 5.41,20 17.96,7.46 20,9.5V4h-5.5z" +
            "M14.83,13.41l-1.41,1.41 3.13,3.13L14.5,20H20v-5.5l-2.04,2.04 -3.13,-3.13z",
    )
}

private val AddCircleOutlineIcon: ImageVector by lazy {
    buildIcon(
        "AddCircleOutline",
        "M13,7h-2v4H7v2h4v4h2v-4h4v-2h-4V7zM12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 " +
            "10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8z",
    )
}

