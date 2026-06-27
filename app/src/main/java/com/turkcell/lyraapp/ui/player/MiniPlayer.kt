package com.turkcell.lyraapp.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.turkcell.lyraapp.data.playback.Song
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

@Composable
fun MiniPlayer(
    state: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val song = state.currentSong ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable { onIntent(PlayerIntent.ExpandClicked) },
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = state.progress.coerceIn(0f, 1f))
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
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

                Column(modifier = Modifier.weight(1f)) {
                    if (state.isPlayingAd) {
                        Text(
                            text = "Reklam",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            maxLines = 1,
                        )
                        Text(
                            text = state.adTitle ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                IconButton(
                    onClick = { onIntent(PlayerIntent.LikeClicked) },
                    modifier = Modifier.size(36.dp),
                    enabled = !state.isPlayingAd,
                ) {
                    Icon(
                        imageVector = if (state.isLiked) LyraIcons.Favorite else LyraIcons.FavoriteOutlined,
                        contentDescription = "Beğeni",
                        tint = if (state.isPlayingAd) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }

                IconButton(
                    onClick = { onIntent(PlayerIntent.PlayPauseClicked) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) PauseIcon else PlayIcon,
                        contentDescription = if (state.isPlaying) "Duraklat" else "Oynat",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }

                IconButton(
                    onClick = { onIntent(PlayerIntent.NextClicked) },
                    modifier = Modifier.size(36.dp),
                    enabled = !state.isPlayingAd,
                ) {
                    Icon(
                        imageVector = SkipNextIcon,
                        contentDescription = "Sonraki",
                        tint = if (state.isPlayingAd) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
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

private val PlayIcon: ImageVector by lazy {
    buildIcon("Play", "M8,5v14l11,-7z")
}

private val PauseIcon: ImageVector by lazy {
    buildIcon("Pause", "M6,19h4V5H6v14zM14,5v14h4V5h-4z")
}

private val SkipNextIcon: ImageVector by lazy {
    buildIcon("SkipNext", "M6,18l8.5,-6L6,6v12zM16,6v12h2V6h-2z")
}

private val previewSong = Song(
    id = "1",
    title = "Neon Sokaklar",
    artist = "Şehir Işıkları",
    duration = "3:43",
    artworkStartColor = 0xFFD98E4A,
    artworkEndColor = 0xFF8A5526,
)

@Preview(name = "MiniPlayer - Dark", showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun MiniPlayerDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        MiniPlayer(
            state = PlayerUiState(currentSong = previewSong, isPlaying = true, progress = 0.42f),
            onIntent = {},
        )
    }
}

@Preview(name = "MiniPlayer - Light", showBackground = true)
@Composable
private fun MiniPlayerLightPreview() {
    LyraAppTheme(darkTheme = false) {
        MiniPlayer(
            state = PlayerUiState(currentSong = previewSong, isPlaying = false, isLiked = true, progress = 0.42f),
            onIntent = {},
        )
    }
}
