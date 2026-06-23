package com.turkcell.lyraapp.data.home

import com.turkcell.lyraapp.data.remote.HomeApiService
import com.turkcell.lyraapp.data.remote.dto.SongDto
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class RemoteHomeRepository @Inject constructor(
    private val homeApiService: HomeApiService,
) : HomeRepository {

    override suspend fun getHomeFeed(): Result<HomeFeed> = runCatching {
        coroutineScope {
            val forYouDeferred = async { homeApiService.getForYou() }
            val recentlyPlayedDeferred = async { homeApiService.getRecentlyPlayed() }
            val recommendationsDeferred = async { homeApiService.getRecommendations() }

            HomeFeed(
                forYouSongs = forYouDeferred.await().data.map { it.toHomeSong() },
                recentlyPlayedSongs = recentlyPlayedDeferred.await().data.map { it.toHomeSong() },
                recommendationSongs = recommendationsDeferred.await().data.map { it.toHomeSong() },
            )
        }
    }

    private fun SongDto.toHomeSong(): HomeSong {
        val (startColor, endColor) = artworkColorsFor(id)
        return HomeSong(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            artworkStartColor = startColor,
            artworkEndColor = endColor,
        )
    }
}
