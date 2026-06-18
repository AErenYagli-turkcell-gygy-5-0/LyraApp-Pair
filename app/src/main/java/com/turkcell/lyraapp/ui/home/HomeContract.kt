package com.turkcell.lyraapp.ui.home

import com.turkcell.lyraapp.data.home.PlaylistForYou
import com.turkcell.lyraapp.data.home.QuickPick
import com.turkcell.lyraapp.data.home.RecentlyPlayed

/**
 * Home ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 *
 * Bu iterasyonda kartlara tıklama ve "Tümü" davranışsızdır (hedef ekranlar henüz yok);
 * bu nedenle yalnızca yükleme akışına ait niyetler tanımlıdır.
 */
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
    /** Besleme yüklemesi başarısız olduğunda kullanıcı yeniden dener. */
    data object Retry : HomeIntent
    /** Kullanıcı tema ikonuna tıkladığında mevcut temayı tersine çevirir. */
    data object ToggleTheme : HomeIntent
}

sealed interface HomeEffect {
    data class ShowError(val message: String) : HomeEffect
}
