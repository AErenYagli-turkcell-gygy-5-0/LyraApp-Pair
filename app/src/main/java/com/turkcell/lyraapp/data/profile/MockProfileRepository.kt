package com.turkcell.lyraapp.data.profile

import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * [ProfileRepository]'nin MOCK implementasyonu.
 *
 * Tasarım ekran görüntüsündeki kullanıcı ve ayar verilerini statik olarak döndürür.
 * Gerçek API geldiğinde bu sınıf ağ tabanlı implementasyonla değiştirilir.
 */
class MockProfileRepository @Inject constructor() : ProfileRepository {

    override suspend fun getProfileData(): Result<ProfileData> {
        delay(NETWORK_DELAY_MS)
        return Result.success(PROFILE)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L

        val PROFILE = ProfileData(
            initials = "ZK",
            fullName = "Zeynep Kaya",
            username = "@zeynepk",
            isPremium = true,
            playlistCount = "127",
            followerCount = "1.2B",
            followingCount = "348",
            settings = listOf(
                SettingItem("sound_quality", "Ses kalitesi", "Yüksek"),
                SettingItem("offline_download", "Çevrimdışı indirme", "Açık"),
                SettingItem("notifications", "Bildirimler"),
                SettingItem("privacy", "Gizlilik"),
                SettingItem("help", "Yardım ve destek"),
            ),
        )
    }
}
