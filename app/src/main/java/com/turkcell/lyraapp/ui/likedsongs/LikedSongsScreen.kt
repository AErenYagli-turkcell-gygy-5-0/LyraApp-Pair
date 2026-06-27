package com.turkcell.lyraapp.ui.likedsongs

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
import com.turkcell.lyraapp.data.likedsongs.LikedSong
import com.turkcell.lyraapp.ui.icons.LyraIcons

@Composable
fun LikedSongsRoute(
    modifier: Modifier = Modifier,
    viewModel: LikedSongsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LikedSongsEffect.NavigateBack -> onBack()
                is LikedSongsEffect.NavigateToNowPlaying -> onNavigateToNowPlaying()
            }
        }
    }

    LikedSongsScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun LikedSongsScreen(
    state: LikedSongsUiState,
    onIntent: (LikedSongsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (state.isLoading && state.songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item {
                    BackButton(onClick = { onIntent(LikedSongsIntent.BackClicked) })
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    CoverHeader(
                        songCount = state.songCount,
                        totalDuration = state.totalDuration,
                    )
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    ActionRow()
                    Spacer(Modifier.height(16.dp))
                }
                items(state.songs, key = { it.id }) { song ->
                    SongRow(
                        song = song,
                        isPlaying = song.id == state.currentlyPlayingId,
                        onClick = { onIntent(LikedSongsIntent.SongClicked(song.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
    ) {
        Icon(
            imageVector = LyraIcons.ArrowBack,
            contentDescription = "Geri",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CoverHeader(
    songCount: Int,
    totalDuration: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFB1C8), Color(0xFFFF6B9D)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = null,
                tint = Color(0xFF8F004A),
                modifier = Modifier.size(64.dp),
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = "Beğenilen\nŞarkılar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$songCount şarkı · $totalDuration",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActionRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "▶  Çal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        IconActionButton(
            icon = ShuffleIcon,
            contentDescription = "Karıştır",
        )
        IconActionButton(
            icon = DownloadIcon,
            contentDescription = "İndir",
        )
    }
}

@Composable
private fun IconActionButton(
    icon: ImageVector,
    contentDescription: String,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun SongRow(
    song: LikedSong,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    val rowBackground = if (isPlaying) {
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
                        listOf(Color(song.artworkStartColor), Color(song.artworkEndColor)),
                    ),
                ),
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
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
        Spacer(Modifier.width(12.dp))
        Icon(
            imageVector = LyraIcons.ArrowForward,
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

private val ShuffleIcon: ImageVector by lazy {
    buildIcon(
        name = "Shuffle",
        pathData = "M10.59,9.17L5.41,4 4,5.41l5.17,5.17 1.42,-1.41z" +
            "M14.5,4l2.04,2.04L4,18.59 5.41,20 17.96,7.46 20,9.5V4h-5.5z" +
            "M14.83,13.41l-1.41,1.41 3.13,3.13L14.5,20H20v-5.5l-2.04,2.04 -3.13,-3.13z",
    )
}

private val DownloadIcon: ImageVector by lazy {
    buildIcon(
        name = "Download",
        pathData = "M19,9h-4V3H9v6H5l7,7 7,-7zM5,18v2h14v-2H5z",
    )
}

