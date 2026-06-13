package com.turkcell.lyraapp.data.likedsongs

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [LikedSongsRepository]'nin MOCK implementasyonu.
 *
 * Tasarım ekran görüntüsündeki 5 şarkıyı statik olarak döndürür; Neon Sokaklar çalınıyor.
 * Gerçek API geldiğinde bu sınıf ağ tabanlı implementasyonla değiştirilir.
 */
class MockLikedSongsRepository @Inject constructor() : LikedSongsRepository {

    override suspend fun getLikedSongs(): Result<LikedSongsFeed> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            LikedSongsFeed(
                songCount = SONGS.size,
                totalDuration = "19 dk",
                songs = SONGS,
            ),
        )
    }

    private companion object {
        const val NETWORK_DELAY_MS = 650L

        val SONGS = listOf(
            LikedSong("ls-1", "Gece Yarısı", "Mavi Deniz", "3:34", 0xFF4AC2A8, 0xFF1F6E5C),
            LikedSong("ls-2", "Yıldız Tozu", "Polaris", "4:07", 0xFF5AAFC9, 0xFF2A5F73),
            LikedSong("ls-3", "İlk Işık", "Sabah Ezgisi", "3:25", 0xFF5AAFC9, 0xFF2A6080),
            LikedSong("ls-4", "Neon Sokaklar", "Şehir Işıkları", "3:43", 0xFFD98E4A, 0xFF8A5526, isPlaying = true),
            LikedSong("ls-5", "Derin Mavi", "Okyanus", "4:29", 0xFF4AC2A8, 0xFF1F6E5C),
        )
    }
}
