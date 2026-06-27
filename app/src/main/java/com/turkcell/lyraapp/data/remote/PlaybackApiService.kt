package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.AdCompleteBodyDto
import com.turkcell.lyraapp.data.remote.dto.AdCompleteResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaybackNextBodyDto
import com.turkcell.lyraapp.data.remote.dto.PlaybackNextResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface PlaybackApiService {

    @POST("api/v1/me/playback/next")
    suspend fun playbackNext(
        @Body body: PlaybackNextBodyDto,
    ): PlaybackNextResponseDto

    @POST("api/v1/me/playback/ad-complete")
    suspend fun adComplete(
        @Body body: AdCompleteBodyDto,
    ): AdCompleteResponseDto
}
