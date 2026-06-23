package com.turkcell.lyraapp.data.home

import kotlinx.coroutines.delay
import javax.inject.Inject

class MockHomeRepository @Inject constructor() : HomeRepository {

    override suspend fun getHomeFeed(): Result<HomeFeed> {
        delay(NETWORK_DELAY_MS)
        return Result.success(
            HomeFeed(
                forYouSongs = FOR_YOU,
                recentlyPlayedSongs = RECENTLY_PLAYED,
                recommendationSongs = RECOMMENDATIONS,
            ),
        )
    }

    private companion object {
        const val NETWORK_DELAY_MS = 800L

        val FOR_YOU = listOf(
            HomeSong("s_neon-tide", "Neon Tide", "Aurora Drift", "City Lights", 32000, 0xFF8B6FB8, 0xFF4A3D6B),
            HomeSong("s_solar-flare", "Solar Flare", "Neon Pulse", "Cosmic Rays", 28000, 0xFF7C83D9, 0xFF3E4486),
            HomeSong("s_kervan", "Kervan", "City Echo", "Yolculuk", 45000, 0xFFD98E4A, 0xFF8A5526),
            HomeSong("s_midnight-run", "Midnight Run", "Deep Wave", "Night Shift", 38000, 0xFF4AC2A8, 0xFF1F6E5C),
            HomeSong("s_ocean-drive", "Ocean Drive", "Solar Flare", "Summer Nights", 41000, 0xFF6FBF5A, 0xFF356B2A),
            HomeSong("s_starlight", "Starlight", "Cosmo Beat", "Galaxy", 35000, 0xFF5AAFC9, 0xFF2A5F73),
        )

        val RECENTLY_PLAYED = listOf(
            HomeSong("s_neon-tide", "Neon Tide", "Aurora Drift", "City Lights", 32000, 0xFFD98E4A, 0xFF8A5526),
            HomeSong("s_ocean-drive", "Ocean Drive", "Solar Flare", "Summer Nights", 41000, 0xFF6FBF5A, 0xFF356B2A),
            HomeSong("s_starlight", "Starlight", "Cosmo Beat", "Galaxy", 35000, 0xFF3D5A80, 0xFF1B2A45),
        )

        val RECOMMENDATIONS = listOf(
            HomeSong("s_midnight-run", "Midnight Run", "Deep Wave", "Night Shift", 38000, 0xFF9B7FC4, 0xFF5A4480),
            HomeSong("s_solar-flare", "Solar Flare", "Neon Pulse", "Cosmic Rays", 28000, 0xFF6B5FB8, 0xFF3A3270),
            HomeSong("s_kervan", "Kervan", "City Echo", "Yolculuk", 45000, 0xFF3FAE9C, 0xFF1E5D52),
        )
    }
}
