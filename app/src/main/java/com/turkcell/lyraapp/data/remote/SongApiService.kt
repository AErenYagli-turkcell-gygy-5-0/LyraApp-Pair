package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.SongsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface SongApiService {

    @GET("api/v1/songs")
    suspend fun getSongs(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
    ): SongsResponseDto
}
