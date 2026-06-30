package com.turkcell.lyraapp.ui.playlistdetail

import com.turkcell.lyraapp.data.createplaylist.AvailableSong
import com.turkcell.lyraapp.data.playlistdetail.PlaylistDetail

data class PlaylistDetailUiState(
    val isLoading: Boolean = false,
    val playlist: PlaylistDetail? = null,
    val currentlyPlayingId: String? = null,
    val isSongPickerVisible: Boolean = false,
    val isSongPickerLoading: Boolean = false,
    val availableSongs: List<AvailableSong> = emptyList(),
    val isDeleteConfirmVisible: Boolean = false,
    val isDeleting: Boolean = false,
)

sealed interface PlaylistDetailIntent {
    data object BackClicked : PlaylistDetailIntent
    data class SongClicked(val songId: String) : PlaylistDetailIntent
    data object PlayAllClicked : PlaylistDetailIntent
    data object ShuffleClicked : PlaylistDetailIntent
    data class RemoveTrackClicked(val songId: String) : PlaylistDetailIntent
    data object AddSongClicked : PlaylistDetailIntent
    data class SongPickerSongSelected(val songId: String) : PlaylistDetailIntent
    data object SongPickerDismissed : PlaylistDetailIntent
    data object DeletePlaylistClicked : PlaylistDetailIntent
    data object DeleteConfirmed : PlaylistDetailIntent
    data object DeleteDismissed : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect {
    data object NavigateBack : PlaylistDetailEffect
    data object NavigateToNowPlaying : PlaylistDetailEffect
    data class ShowError(val message: String) : PlaylistDetailEffect
}
