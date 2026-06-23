package com.turkcell.lyraapp.data.remote.dto

import com.squareup.moshi.JsonClass

// region — OTP Request
@JsonClass(generateAdapter = false)
data class OtpRequestBodyDto(
    val phone: String,
)

@JsonClass(generateAdapter = false)
data class OtpRequestDataDto(
    val sent: Boolean,
    val firstTime: Boolean,
)

@JsonClass(generateAdapter = false)
data class OtpRequestResponseDto(
    val data: OtpRequestDataDto,
)
// endregion

// region — OTP Verify
@JsonClass(generateAdapter = false)
data class OtpVerifyBodyDto(
    val phone: String,
    val code: String,
)

@JsonClass(generateAdapter = false)
data class AuthSessionDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val user: UserDto,
    val firstTime: Boolean,
)

@JsonClass(generateAdapter = false)
data class OtpVerifyResponseDto(
    val data: AuthSessionDto,
)
// endregion

// region — Update Informations
@JsonClass(generateAdapter = false)
data class UpdateInformationsBodyDto(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
)

@JsonClass(generateAdapter = false)
data class UserDto(
    val id: String,
    val phone: String,
    val displayName: String?,
    val firstName: String?,
    val lastName: String?,
    val birthDate: String?,
    val createdAt: String,
    val profileCompleted: Boolean,
)

@JsonClass(generateAdapter = false)
data class UserResponseDto(
    val data: UserDto,
)
// endregion

// region — Error
@JsonClass(generateAdapter = false)
data class ApiErrorDetailDto(
    val code: String,
    val message: String,
)

@JsonClass(generateAdapter = false)
data class ApiErrorResponseDto(
    val error: ApiErrorDetailDto,
)
// endregion
