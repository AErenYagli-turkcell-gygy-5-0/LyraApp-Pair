package com.turkcell.lyraapp.data.profile

/**
 * Profil ekranı verilerinin veri kaynağı soyutlaması.
 *
 * Backend REST API'si hazır olmadığından geçici implementasyon [MockProfileRepository]'dir;
 * gerçek API geldiğinde yalnızca implementasyon ve `di/ProfileModule.kt` bağlaması değişir.
 */
interface ProfileRepository {

    /** Kullanıcının profil verilerini döndürür. */
    suspend fun getProfileData(): Result<ProfileData>
}
