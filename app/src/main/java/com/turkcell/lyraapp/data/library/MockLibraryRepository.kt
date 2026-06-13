package com.turkcell.lyraapp.data.library

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [LibraryRepository]'nin MOCK implementasyonu.
 *
 * Tasarım ekran görüntüsündeki 6 çalma listesini statik olarak döndürür.
 * Gerçek API geldiğinde bu sınıf ağ tabanlı implementasyonla değiştirilir.
 */
class MockLibraryRepository @Inject constructor() : LibraryRepository {

    override suspend fun getLibraryFeed(): Result<LibraryFeed> {
        delay(NETWORK_DELAY_MS)
        return Result.success(LibraryFeed(playlists = PLAYLISTS))
    }

    private companion object {
        const val NETWORK_DELAY_MS = 700L

        val PLAYLISTS = listOf(
            LibraryPlaylist(
                id = "pl-liked",
                title = "Beğenilen Şarkılar",
                songCount = 5,
                artworkStartColor = 0xFFFFB1C8,
                artworkEndColor = 0xFFFF6B9D,
                isPinned = true,
                isLikedSongs = true,
            ),
            LibraryPlaylist("pl-1", "Gece Sürüşü", 6, 0xFF8B6FB8, 0xFF4A3D6B),
            LibraryPlaylist("pl-2", "Sabah Kahvesi", 5, 0xFF7C83D9, 0xFF3E4486),
            LibraryPlaylist("pl-3", "Odaklan", 5, 0xFF4AC2A8, 0xFF1F6E5C),
            LibraryPlaylist("pl-4", "Yaz Anıları", 5, 0xFF5AAFC9, 0xFF2A5F73),
            LibraryPlaylist("pl-5", "Akustik Akşam", 4, 0xFF4AC2A8, 0xFF1F6E5C),
        )
    }
}
