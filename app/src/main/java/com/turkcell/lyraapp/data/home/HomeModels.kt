package com.turkcell.lyraapp.data.home

import kotlin.math.abs

data class HomeFeed(
    val forYouSongs: List<HomeSong>,
    val recentlyPlayedSongs: List<HomeSong>,
    val recommendationSongs: List<HomeSong>,
)

data class HomeSong(
    val id: String,
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Int,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
) {
    companion object {
        fun fromDownloaded(entity: com.turkcell.lyraapp.data.download.DownloadedSongEntity): HomeSong {
            val colors = artworkColorsFor(entity.songId)
            return HomeSong(
                id = entity.songId,
                title = entity.title,
                artist = entity.artist,
                album = null,
                durationMs = 0,
                artworkStartColor = colors.first,
                artworkEndColor = colors.second,
            )
        }
    }
}

fun artworkColorsFor(id: String): Pair<Long, Long> {
    val index = abs(id.hashCode()) % ARTWORK_PALETTE.size
    return ARTWORK_PALETTE[index]
}

private val ARTWORK_PALETTE = listOf(
    Pair(0xFF8B6FB8L, 0xFF4A3D6BL),
    Pair(0xFF7C83D9L, 0xFF3E4486L),
    Pair(0xFFD98E4AL, 0xFF8A5526L),
    Pair(0xFF4AC2A8L, 0xFF1F6E5CL),
    Pair(0xFF6FBF5AL, 0xFF356B2AL),
    Pair(0xFF5AAFC9L, 0xFF2A5F73L),
    Pair(0xFF9B7FC4L, 0xFF5A4480L),
    Pair(0xFF6B5FB8L, 0xFF3A3270L),
    Pair(0xFF3FAE9CL, 0xFF1E5D52L),
    Pair(0xFFD96060L, 0xFF8A2626L),
    Pair(0xFFBFA84AL, 0xFF7A6A1FL),
    Pair(0xFF4A8BD9L, 0xFF1F4A8AL),
)
