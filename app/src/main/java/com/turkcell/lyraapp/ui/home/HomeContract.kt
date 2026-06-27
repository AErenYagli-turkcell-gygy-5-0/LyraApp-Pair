package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.HomeSong
import com.turkcell.lyraapp.data.membership.MembershipPlan

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val greeting: String = "",
    val userInitials: String = "",
    val isDarkTheme: Boolean = false,
    val forYouSongs: List<HomeSong> = emptyList(),
    val recentlyPlayedSongs: List<HomeSong> = emptyList(),
    val recommendationSongs: List<HomeSong> = emptyList(),
    val downloadedSongs: List<HomeSong> = emptyList(),
    val showPremiumWarning: Boolean = false,
    val premiumDaysLeft: Int = 0,
    val premiumPlans: List<MembershipPlan> = emptyList(),
)

sealed interface HomeIntent {
    data object ScreenResumed : HomeIntent
    data object Retry : HomeIntent
    data object ToggleTheme : HomeIntent
    data class SongClicked(val song: HomeSong) : HomeIntent
    data object DismissPremiumWarning : HomeIntent
    data class PremiumWarningAction(val planType: String) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
    data object NavigateToNowPlaying : HomeEffect
    data object NavigateToPremium : HomeEffect
    data class NavigateToPayment(val planType: String) : HomeEffect
}
