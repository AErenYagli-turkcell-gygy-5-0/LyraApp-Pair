package com.turkcell.lyraapp.data.search

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [SearchRepository]'nin MOCK implementasyonu.
 *
 * Tasarım ekran görüntüsündeki 8 türü statik olarak döndürür ve ağ davranışını taklit eder.
 * Gerçek API geldiğinde bu sınıf ağ tabanlı implementasyonla değiştirilir.
 */
class MockSearchRepository @Inject constructor() : SearchRepository {

    override suspend fun getSearchFeed(): Result<SearchFeed> {
        delay(NETWORK_DELAY_MS)
        return Result.success(SearchFeed(genres = GENRES))
    }

    private companion object {
        const val NETWORK_DELAY_MS = 600L

        val GENRES = listOf(
            Genre("g-1", "Pop", 0xFF00C9A7, 0xFF005F73),
            Genre("g-2", "Elektronik", 0xFF9B79F5, 0xFF5B3FA0),
            Genre("g-3", "Akustik", 0xFF8B5CF6, 0xFF5B21B6, hasDecorativeCircles = true),
            Genre("g-4", "Lo-fi", 0xFF2DD4BF, 0xFF0D9488),
            Genre("g-5", "Indie", 0xFF6366F1, 0xFF3730A3, hasDecorativeCircles = true),
            Genre("g-6", "Jazz", 0xFF22C55E, 0xFF15803D),
            Genre("g-7", "Klasik", 0xFFEC4899, 0xFFBE185D, hasDecorativeCircles = true),
            Genre("g-8", "Yolculuk", 0xFFF97316, 0xFFEA580C),
        )
    }
}
