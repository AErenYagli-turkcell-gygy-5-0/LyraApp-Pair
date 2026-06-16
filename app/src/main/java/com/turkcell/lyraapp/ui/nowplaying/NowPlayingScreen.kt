package com.turkcell.lyraapp.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.playback.Song
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun NowPlayingRoute(
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NowPlayingEffect.Collapse -> onCollapse()
            }
        }
    }

    NowPlayingScreen(
        state = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun NowPlayingScreen(
    state: NowPlayingUiState,
    onIntent: (NowPlayingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = state.currentSong
    val bgStart = if (song != null) Color(song.artworkStartColor).copy(alpha = 0.85f) else Color(0xFF3A2010)
    val bgEnd = Color(0xFF121212)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgStart, bgEnd))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))

            TopBar(
                sourceName = state.sourceName,
                onCollapse = { onIntent(NowPlayingIntent.CollapseClicked) },
            )

            Spacer(Modifier.height(32.dp))

            Artwork(
                startColor = song?.artworkStartColor ?: 0xFFD98E4A,
                endColor = song?.artworkEndColor ?: 0xFF8A5526,
            )

            Spacer(Modifier.height(32.dp))

            SongInfo(
                title = song?.title ?: "",
                artist = song?.artist ?: "",
                isLiked = state.isLiked,
                onLike = { onIntent(NowPlayingIntent.LikeClicked) },
            )

            Spacer(Modifier.height(24.dp))

            SeekBar(
                progress = state.progress,
                currentLabel = state.currentPositionLabel,
                totalLabel = song?.duration ?: "0:00",
                onSeek = { onIntent(NowPlayingIntent.SeekTo(it)) },
            )

            Spacer(Modifier.height(20.dp))

            Controls(
                isPlaying = state.isPlaying,
                isShuffle = state.isShuffle,
                isRepeat = state.isRepeat,
                onPlayPause = { onIntent(NowPlayingIntent.PlayPauseClicked) },
                onNext = { onIntent(NowPlayingIntent.NextClicked) },
                onPrevious = { onIntent(NowPlayingIntent.PreviousClicked) },
                onShuffle = { onIntent(NowPlayingIntent.ShuffleClicked) },
                onRepeat = { onIntent(NowPlayingIntent.RepeatClicked) },
            )

            Spacer(Modifier.height(24.dp))

            BottomActions()
        }
    }
}

@Composable
private fun TopBar(
    sourceName: String,
    onCollapse: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onCollapse,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = ExpandMoreIcon,
                contentDescription = "Kapat",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ŞİMDİ ÇALIYOR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = sourceName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        IconButton(
            onClick = {},
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Icon(
                imageVector = MoreVertIcon,
                contentDescription = "Daha fazla",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun Artwork(
    startColor: Long,
    endColor: Long,
) {
    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(startColor), Color(endColor)),
                ),
            ),
    )
}

@Composable
private fun SongInfo(
    title: String,
    artist: String,
    isLiked: Boolean,
    onLike: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onLike) {
            Icon(
                imageVector = if (isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                contentDescription = "Beğeni",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun SeekBar(
    progress: Float,
    currentLabel: String,
    totalLabel: String,
    onSeek: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progress.coerceIn(0f, 1f),
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = currentLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = totalLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Controls(
    isPlaying: Boolean,
    isShuffle: Boolean,
    isRepeat: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onShuffle) {
            Icon(
                imageVector = ShuffleIcon,
                contentDescription = "Karıştır",
                tint = if (isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp),
            )
        }

        IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = SkipPreviousIcon,
                contentDescription = "Önceki",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(36.dp),
            )
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) PauseIcon else PlayIcon,
                contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp),
            )
        }

        IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
            Icon(
                imageVector = SkipNextIcon,
                contentDescription = "Sonraki",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(36.dp),
            )
        }

        IconButton(onClick = onRepeat) {
            Icon(
                imageVector = RepeatIcon,
                contentDescription = "Tekrar",
                tint = if (isRepeat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun BottomActions() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = CastIcon,
                contentDescription = "Cihaz",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }

        Text(
            text = "Arkaplan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        IconButton(onClick = {}) {
            Icon(
                imageVector = QueueMusicIcon,
                contentDescription = "Kuyruk",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
        }
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

private val ExpandMoreIcon: ImageVector by lazy {
    buildIcon("ExpandMore", "M16.59,8.59L12,13.17 7.41,8.59 6,10l6,6 6,-6z")
}

private val MoreVertIcon: ImageVector by lazy {
    buildIcon("MoreVert", "M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2z" +
        "M12,10c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z" +
        "M12,16c-1.1,0 -2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z")
}

private val PlayIcon: ImageVector by lazy {
    buildIcon("Play", "M8,5v14l11,-7z")
}

private val PauseIcon: ImageVector by lazy {
    buildIcon("Pause", "M6,19h4V5H6v14zM14,5v14h4V5h-4z")
}

private val SkipPreviousIcon: ImageVector by lazy {
    buildIcon("SkipPrevious", "M6,6h2v12H6zM9.5,12l8.5,6V6z")
}

private val SkipNextIcon: ImageVector by lazy {
    buildIcon("SkipNext", "M6,18l8.5,-6L6,6v12zM16,6v12h2V6h-2z")
}

private val ShuffleIcon: ImageVector by lazy {
    buildIcon(
        "Shuffle",
        "M10.59,9.17L5.41,4 4,5.41l5.17,5.17 1.42,-1.41z" +
            "M14.5,4l2.04,2.04L4,18.59 5.41,20 17.96,7.46 20,9.5V4h-5.5z" +
            "M14.83,13.41l-1.41,1.41 3.13,3.13L14.5,20H20v-5.5l-2.04,2.04 -3.13,-3.13z",
    )
}

private val RepeatIcon: ImageVector by lazy {
    buildIcon("Repeat", "M7,7h10v3l4,-4 -4,-4v3H5v6h2V7zM17,17H7v-3l-4,4 4,4v-3h12v-6h-2v5z")
}

private val CastIcon: ImageVector by lazy {
    buildIcon(
        "Cast",
        "M21,3H3C1.9,3 1,3.9 1,5v3h2V5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2V5C23,3.9 22.1,3 21,3z" +
            "M1,18v3h3C4,19.34 2.66,18 1,18zM1,14v2c2.76,0 5,2.24 5,5h2C8,17.13 4.87,14 1,14z" +
            "M1,10v2c4.97,0 9,4.03 9,9h2C12,15.07 7,10 1,10z",
    )
}

private val QueueMusicIcon: ImageVector by lazy {
    buildIcon(
        "QueueMusic",
        "M15,6H3v2h12V6zM15,10H3v2h12v-2zM3,16h8v-2H3v2zM17,6v8.18C16.69,14.07 16.35,14 16,14" +
            "c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3V8h3V6h-5z",
    )
}

private val previewState = NowPlayingUiState(
    currentSong = Song(
        id = "ps-1",
        title = "Neon Sokaklar",
        artist = "Şehir Işıkları",
        duration = "3:43",
        artworkStartColor = 0xFFD98E4A,
        artworkEndColor = 0xFF8A5526,
    ),
    sourceName = "Gece Vardiyası",
    isPlaying = true,
    isLiked = true,
    progress = 0.41f,
    currentPositionLabel = "1:33",
)

@Preview(name = "NowPlaying - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun NowPlayingDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        NowPlayingScreen(state = previewState, onIntent = {})
    }
}

@Preview(name = "NowPlaying - Light", showBackground = true, showSystemUi = true)
@Composable
private fun NowPlayingLightPreview() {
    LyraAppTheme(darkTheme = false) {
        NowPlayingScreen(state = previewState.copy(isPlaying = false), onIntent = {})
    }
}
