package com.turkcell.lyraapp.data.playlistdetail

import kotlinx.coroutines.delay
import javax.inject.Inject

class MockPlaylistDetailRepository @Inject constructor() : PlaylistDetailRepository {

    override suspend fun getPlaylistDetail(playlistId: String): Result<PlaylistDetail> {
        delay(NETWORK_DELAY_MS)
        val detail = PLAYLISTS[playlistId] ?: PLAYLISTS.values.first()
        return Result.success(detail)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 600L

        val DEFAULT_SONGS = listOf(
            PlaylistSong(
                id = "ps-1",
                title = "Neon Sokaklar",
                artist = "Şehir Işıkları",
                duration = "3:43",
                artworkStartColor = 0xFFD98E4A,
                artworkEndColor = 0xFF8A5526,
                isLiked = true,
                isPlaying = true,
            ),
            PlaylistSong(
                id = "ps-2",
                title = "Gece Yarısı",
                artist = "Mavi Deniz",
                duration = "3:34",
                artworkStartColor = 0xFF4AC2A8,
                artworkEndColor = 0xFF1F6E5C,
                isLiked = true,
            ),
            PlaylistSong(
                id = "ps-3",
                title = "Mor Bulutlar",
                artist = "Derin Kaya",
                duration = "3:52",
                artworkStartColor = 0xFF9B7FC4,
                artworkEndColor = 0xFF5A4480,
            ),
            PlaylistSong(
                id = "ps-4",
                title = "Son Tren",
                artist = "Peron",
                duration = "3:37",
                artworkStartColor = 0xFF6B5FB8,
                artworkEndColor = 0xFF3A3270,
            ),
            PlaylistSong(
                id = "ps-5",
                title = "Yıldız Tozu",
                artist = "Polaris",
                duration = "4:07",
                artworkStartColor = 0xFF5AAFC9,
                artworkEndColor = 0xFF2A5F73,
                isLiked = true,
            ),
        )

        val PLAYLISTS = mapOf(
            "pl-gece" to PlaylistDetail(
                id = "pl-gece",
                title = "Gece Sürüşü",
                description = "Karanlık yollar için synth-pop",
                ownerName = "Zeynep Kaya",
                songCount = 5,
                totalDuration = "23 dk",
                artworkStartColor = 0xFF8B6FB8,
                artworkEndColor = 0xFF4A3D6B,
                songs = DEFAULT_SONGS,
            ),
        )
    }
}
