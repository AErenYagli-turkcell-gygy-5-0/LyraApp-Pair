package com.turkcell.lyraapp.ui.library

import com.turkcell.lyraapp.data.library.LibraryPlaylist

/**
 * Kütüphane ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 */
data class LibraryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: LibraryTab = LibraryTab.Playlists,
    val playlists: List<LibraryPlaylist> = emptyList(),
    val playlistPendingDelete: LibraryPlaylist? = null,
    val isDeleting: Boolean = false,
)

enum class LibraryTab(val label: String) {
    Playlists("Çalma listeleri"),
    Artists("Sanatçılar"),
    Albums("Albümler"),
}

sealed interface LibraryIntent {
    data class TabSelected(val tab: LibraryTab) : LibraryIntent
    data object OpenLikedSongs : LibraryIntent
    data class PlaylistClicked(val playlistId: String) : LibraryIntent
    data object CreatePlaylistClicked : LibraryIntent
    data object Retry : LibraryIntent
    data class DeletePlaylistClicked(val playlist: LibraryPlaylist) : LibraryIntent
    data object DeleteConfirmed : LibraryIntent
    data object DeleteDismissed : LibraryIntent
}

sealed interface LibraryEffect {
    data object NavigateToLikedSongs : LibraryEffect
    data class NavigateToPlaylistDetail(val playlistId: String) : LibraryEffect
    data object NavigateToCreatePlaylist : LibraryEffect
    data class ShowError(val message: String) : LibraryEffect
}
