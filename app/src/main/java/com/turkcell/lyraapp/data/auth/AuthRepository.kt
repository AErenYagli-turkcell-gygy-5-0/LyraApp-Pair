package com.turkcell.lyraapp.data.auth

data class OtpRequestResult(
    val sent: Boolean,
    val firstTime: Boolean,
)

data class OtpVerifyResult(
    val accessToken: String,
    val refreshToken: String,
    val firstTime: Boolean,
    val expiresIn: Int,
)

interface AuthRepository {

    suspend fun requestOtp(phoneNumber: String): Result<OtpRequestResult>

    suspend fun verifyOtp(phoneNumber: String, code: String): Result<OtpVerifyResult>

    suspend fun completeProfile(
        firstName: String,
        lastName: String,
        birthDate: String,
    ): Result<Unit>
}
