package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.SongListResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApiService {

    @GET("api/v1/me/recently-played")
    suspend fun getRecentlyPlayed(
        @Query("limit") limit: Int = 20,
    ): SongListResponseDto

    @GET("api/v1/me/for-you")
    suspend fun getForYou(
        @Query("limit") limit: Int = 20,
    ): SongListResponseDto

    @GET("api/v1/me/recommendations")
    suspend fun getRecommendations(
        @Query("limit") limit: Int = 20,
    ): SongListResponseDto
}
