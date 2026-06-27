package com.turkcell.lyraapp.data.profile

import com.squareup.moshi.Moshi
import com.turkcell.lyraapp.data.auth.UserSessionManager
import com.turkcell.lyraapp.data.auth.UserSessionManager.Companion.calculateDaysLeft
import com.turkcell.lyraapp.data.remote.AuthApiService
import com.turkcell.lyraapp.data.remote.dto.ApiErrorResponseDto
import retrofit2.Response
import javax.inject.Inject

class RealProfileRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userSessionManager: UserSessionManager,
    private val moshi: Moshi,
) : ProfileRepository {

    override suspend fun getProfileData(): Result<ProfileData> = runCatching {
        val response = authApiService.getMe()
        if (!response.isSuccessful) throw apiException(response)

        val userDto = response.body()!!.data
        userSessionManager.setUser(userDto)

        val firstName = userDto.firstName.orEmpty()
        val lastName = userDto.lastName.orEmpty()
        val fullName = "$firstName $lastName".trim().ifEmpty {
            userDto.displayName ?: userDto.phone
        }
        val initials = buildString {
            if (firstName.isNotEmpty()) append(firstName.first().uppercaseChar())
            if (lastName.isNotEmpty()) append(lastName.first().uppercaseChar())
        }.ifEmpty { fullName.take(2).uppercase() }

        val membership = userDto.membership
        val isPremium = membership != null && membership.status == "active"
        val premiumDaysLeft = if (isPremium) calculateDaysLeft(membership!!.expiresAt) else 0

        ProfileData(
            initials = initials,
            fullName = fullName,
            username = userDto.displayName?.let { "@$it" } ?: userDto.phone,
            isPremium = isPremium,
            premiumDaysLeft = premiumDaysLeft,
            membershipType = membership?.type,
            playlistCount = "0",
            followerCount = "0",
            followingCount = "0",
            settings = DEFAULT_SETTINGS,
        )
    }

    private fun apiException(response: Response<*>): Exception {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                val parsed = moshi.adapter(ApiErrorResponseDto::class.java).fromJson(errorBody)
                if (parsed != null) return Exception(parsed.error.message)
            } catch (_: Exception) { }
        }
        return when (response.code()) {
            401 -> Exception("Oturum suresi dolmus. Lutfen tekrar giris yapin.")
            else -> Exception("Profil yuklenemedi (${response.code()}).")
        }
    }

    private companion object {
        val DEFAULT_SETTINGS = listOf(
            SettingItem("sound_quality", "Ses kalitesi", "Yüksek"),
            SettingItem("offline_download", "Çevrimdışı indirme", "Açık"),
            SettingItem("notifications", "Bildirimler"),
            SettingItem("privacy", "Gizlilik"),
            SettingItem("help", "Yardım ve destek"),
        )
    }
}
