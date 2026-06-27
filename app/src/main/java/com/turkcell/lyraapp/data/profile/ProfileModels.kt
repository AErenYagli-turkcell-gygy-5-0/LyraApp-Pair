package com.turkcell.lyraapp.data.profile

/**
 * Profil ekranının kullanıcı verisi.
 *
 * [playlistCount], [followerCount], [followingCount] görüntüleme formatında taşınır
 * (ör. "1.2B"); formatlama backend veya Mock katmanında yapılır, UI yalnızca string'i gösterir.
 */
data class ProfileData(
    val initials: String,
    val fullName: String,
    val username: String,
    val isPremium: Boolean,
    val premiumDaysLeft: Int = 0,
    val membershipType: String? = null,
    val playlistCount: String,
    val followerCount: String,
    val followingCount: String,
    val settings: List<SettingItem>,
)

/**
 * Ayarlar listesindeki tek bir satır.
 *
 * [currentValue] null olduğunda sağda yalnızca chevron gösterilir.
 * [id] ViewModel'in hangi ayara tıklandığını ayırt etmesi için kullanılır.
 */
data class SettingItem(
    val id: String,
    val title: String,
    val currentValue: String? = null,
)
