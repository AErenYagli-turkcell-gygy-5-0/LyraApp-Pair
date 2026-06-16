package com.turkcell.lyraapp.data.createplaylist

import kotlinx.coroutines.delay
import javax.inject.Inject

class MockCreatePlaylistRepository @Inject constructor() : CreatePlaylistRepository {

    override suspend fun getAvailableSongs(): Result<List<AvailableSong>> {
        delay(NETWORK_DELAY_MS)
        return Result.success(AVAILABLE_SONGS)
    }

    override suspend fun createPlaylist(input: CreatePlaylistInput): Result<Unit> {
        delay(SAVE_DELAY_MS)
        return Result.success(Unit)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L
        const val SAVE_DELAY_MS = 800L

        val AVAILABLE_SONGS = listOf(
            AvailableSong("as-1", "Gece Yarısı", "Mavi Deniz", 0xFF4AC2A8, 0xFF1F6E5C),
            AvailableSong("as-2", "Sessiz Şehir", "Ela Tuna", 0xFF9B7FC4, 0xFF5A4480),
            AvailableSong("as-3", "Yıldız Tozu", "Polaris", 0xFF5AAFC9, 0xFF2A5F73),
            AvailableSong("as-4", "Sahil Yolu", "Kumsal", 0xFF4AC2A8, 0xFF1F6E5C),
            AvailableSong("as-5", "Mor Bulutlar", "Derin Kaya", 0xFF9B7FC4, 0xFF5A4480),
            AvailableSong("as-6", "İlk Işık", "Sabah Ezgisi", 0xFF6B5FB8, 0xFF3A3270),
            AvailableSong("as-7", "Kayıp Anlar", "Eko", 0xFFD98E4A, 0xFF8A5526),
        )
    }
}
