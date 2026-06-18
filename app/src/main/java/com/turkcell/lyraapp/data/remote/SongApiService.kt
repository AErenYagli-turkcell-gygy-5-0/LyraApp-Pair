package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.SongsResponseDto
import com.turkcell.lyraapp.data.remote.dto.StreamUrlResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SongApiService {

    @GET("api/v1/songs")
    suspend fun getSongs(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
    ): SongsResponseDto

    @GET("api/v1/songs/{id}/stream-url")
    suspend fun getStreamUrl(
        @Path("id") id: String,
    ): StreamUrlResponseDto
}
