package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.OtpRequestBodyDto
import com.turkcell.lyraapp.data.remote.dto.OtpRequestResponseDto
import com.turkcell.lyraapp.data.remote.dto.OtpVerifyBodyDto
import com.turkcell.lyraapp.data.remote.dto.OtpVerifyResponseDto
import com.turkcell.lyraapp.data.remote.dto.UpdateInformationsBodyDto
import com.turkcell.lyraapp.data.remote.dto.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequestBodyDto): Response<OtpRequestResponseDto>

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyBodyDto): Response<OtpVerifyResponseDto>

    @POST("api/v1/me/update-informations")
    suspend fun updateInformations(@Body body: UpdateInformationsBodyDto): Response<UserResponseDto>
}
