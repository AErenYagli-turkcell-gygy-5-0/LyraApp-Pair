package com.turkcell.lyraapp.ui.profile

import com.turkcell.lyraapp.data.profile.SettingItem

/**
 * Profil ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 *
 * [isDarkTheme] DataStore'dan gelen Flow ile senkronize tutulur; tema geçişi
 * [ProfileIntent.ThemeChanged] üzerinden [ThemePreferenceRepository]'ye yazılır ve
 * tüm uygulama geneline anında yayılır.
 */
data class ProfileUiState(
    val isLoading: Boolean = false,
    val initials: String = "",
    val fullName: String = "",
    val username: String = "",
    val isPremium: Boolean = false,
    val playlistCount: String = "",
    val followerCount: String = "",
    val followingCount: String = "",
    val settings: List<SettingItem> = emptyList(),
    val isDarkTheme: Boolean = false,
)

sealed interface ProfileIntent {
    /** Kullanıcı Açık/Koyu segment kontrolünden birini seçti. */
    data class ThemeChanged(val isDark: Boolean) : ProfileIntent
    /** Bir ayar satırına tıklandı; ileriki iterasyonda detay navigasyonuna bağlanacak. */
    data class SettingClicked(val settingId: String) : ProfileIntent
}

sealed interface ProfileEffect {
    data class ShowError(val message: String) : ProfileEffect
}
