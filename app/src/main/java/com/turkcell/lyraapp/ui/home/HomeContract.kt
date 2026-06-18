package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.PlaylistForYou
import com.turkcell.lyraapp.data.home.QuickPick
import com.turkcell.lyraapp.data.home.RecentlyPlayed

data class HomeUiState(
    val isLoading: Boolean = false,
    val greeting: String = "",
    val userInitials: String = "",
    val isDarkTheme: Boolean = false,
    val quickPicks: List<QuickPick> = emptyList(),
    val recentlyPlayed: List<RecentlyPlayed> = emptyList(),
    val playlistsForYou: List<PlaylistForYou> = emptyList(),
)

sealed interface HomeIntent {
    data object Retry : HomeIntent
    data object ToggleTheme : HomeIntent
    data class QuickPickClicked(
        val id: String,
        val title: String,
        val artist: String,
        val durationMs: Int,
        val artworkStartColor: Long,
        val artworkEndColor: Long,
    ) : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
    data object NavigateToNowPlaying : HomeEffect
}
