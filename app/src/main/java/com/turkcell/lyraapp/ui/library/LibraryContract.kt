package com.turkcell.lyraapp.ui.library

import com.turkcell.lyraapp.data.library.LibraryPlaylist

/**
 * Kütüphane ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val selectedTab: LibraryTab = LibraryTab.Playlists,
    val playlists: List<LibraryPlaylist> = emptyList(),
)

enum class LibraryTab(val label: String) {
    Playlists("Çalma listeleri"),
    Artists("Sanatçılar"),
    Albums("Albümler"),
}

sealed interface LibraryIntent {
    data class TabSelected(val tab: LibraryTab) : LibraryIntent
    data object OpenLikedSongs : LibraryIntent
}

sealed interface LibraryEffect {
    data object NavigateToLikedSongs : LibraryEffect
    data class ShowError(val message: String) : LibraryEffect
}
