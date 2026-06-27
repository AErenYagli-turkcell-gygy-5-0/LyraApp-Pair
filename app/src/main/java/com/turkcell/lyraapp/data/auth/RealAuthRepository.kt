package com.turkcell.lyraapp.data.auth

import com.squareup.moshi.Moshi
import com.turkcell.lyraapp.data.remote.AuthApiService
import com.turkcell.lyraapp.data.remote.dto.ApiErrorResponseDto
import com.turkcell.lyraapp.data.remote.dto.OtpRequestBodyDto
import com.turkcell.lyraapp.data.remote.dto.OtpVerifyBodyDto
import com.turkcell.lyraapp.data.remote.dto.UpdateInformationsBodyDto
import retrofit2.Response
import javax.inject.Inject

class RealAuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val moshi: Moshi,
    private val userSessionManager: UserSessionManager,
) : AuthRepository {

    override suspend fun requestOtp(phoneNumber: String): Result<OtpRequestResult> =
        runCatching {
            val response = authApiService.requestOtp(OtpRequestBodyDto(phone = phoneNumber))
            if (response.isSuccessful) {
                val body = response.body()!!.data
                OtpRequestResult(sent = body.sent, firstTime = body.firstTime)
            } else {
                throw apiException(response)
            }
        }

    override suspend fun verifyOtp(phoneNumber: String, code: String): Result<OtpVerifyResult> =
        runCatching {
            val response = authApiService.verifyOtp(OtpVerifyBodyDto(phone = phoneNumber, code = code))
            if (response.isSuccessful) {
                val session = response.body()!!.data
                userSessionManager.setUser(session.user)
                OtpVerifyResult(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    firstTime = session.firstTime,
                    expiresIn = session.expiresIn,
                )
            } else {
                throw apiException(response)
            }
        }

    override suspend fun completeProfile(
        firstName: String,
        lastName: String,
        birthDate: String,
    ): Result<Unit> =
        runCatching {
            val response = authApiService.updateInformations(
                UpdateInformationsBodyDto(
                    firstName = firstName,
                    lastName = lastName,
                    birthDate = birthDate,
                ),
            )
            if (response.isSuccessful) {
                response.body()?.data?.let { userSessionManager.setUser(it) }
            } else {
                throw apiException(response)
            }
        }

    private fun apiException(response: Response<*>): Exception {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                val adapter = moshi.adapter(ApiErrorResponseDto::class.java)
                val parsed = adapter.fromJson(errorBody)
                if (parsed != null) {
                    return Exception(parsed.error.message)
                }
            } catch (_: Exception) { }
        }
        return when (response.code()) {
            400 -> Exception("Gecersiz bilgi gonderildi.")
            401 -> Exception("Oturum suresi dolmus. Lutfen tekrar giris yapin.")
            else -> Exception("Beklenmeyen bir hata olustu (${response.code()}).")
        }
    }
}
