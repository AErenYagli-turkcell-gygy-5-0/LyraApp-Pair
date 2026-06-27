package com.turkcell.lyraapp.ui.profile

import com.turkcell.lyraapp.data.profile.SettingItem

data class ProfileUiState(
    val isLoading: Boolean = false,
    val initials: String = "",
    val fullName: String = "",
    val username: String = "",
    val isPremium: Boolean = false,
    val premiumDaysLeft: Int = 0,
    val membershipType: String? = null,
    val playlistCount: String = "",
    val followerCount: String = "",
    val followingCount: String = "",
    val settings: List<SettingItem> = emptyList(),
    val isDarkTheme: Boolean = false,
)

sealed interface ProfileIntent {
    data class ThemeChanged(val isDark: Boolean) : ProfileIntent
    data class SettingClicked(val settingId: String) : ProfileIntent
    data object PremiumCardClicked : ProfileIntent
}

sealed interface ProfileEffect {
    data class ShowError(val message: String) : ProfileEffect
    data object NavigateToPremium : ProfileEffect
}
