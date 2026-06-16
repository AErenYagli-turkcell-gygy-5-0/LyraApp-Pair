package com.turkcell.lyraapp.ui.createplaylist

import com.turkcell.lyraapp.data.createplaylist.AvailableSong

data class CreatePlaylistUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val selectedSongIds: Set<String> = emptySet(),
    val availableSongs: List<AvailableSong> = emptyList(),
    val isSaveEnabled: Boolean = false,
)

sealed interface CreatePlaylistIntent {
    data class NameChanged(val name: String) : CreatePlaylistIntent
    data class DescriptionChanged(val description: String) : CreatePlaylistIntent
    data object PublicToggled : CreatePlaylistIntent
    data class SongToggled(val songId: String) : CreatePlaylistIntent
    data object SaveClicked : CreatePlaylistIntent
    data object CloseClicked : CreatePlaylistIntent
}

sealed interface CreatePlaylistEffect {
    data object Dismiss : CreatePlaylistEffect
    data class ShowError(val message: String) : CreatePlaylistEffect
}
