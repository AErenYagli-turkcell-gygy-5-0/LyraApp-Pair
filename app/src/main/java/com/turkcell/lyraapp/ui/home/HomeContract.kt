package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.HomeSong

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
)

sealed interface HomeIntent {
    data object ScreenResumed : HomeIntent
    data object Retry : HomeIntent
    data object ToggleTheme : HomeIntent
    data class SongClicked(val song: HomeSong) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
    data object NavigateToNowPlaying : HomeEffect
}
