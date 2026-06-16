package com.turkcell.lyraapp.ui.playlistdetail

import com.turkcell.lyraapp.data.playlistdetail.PlaylistDetail

data class PlaylistDetailUiState(
    val isLoading: Boolean = false,
    val playlist: PlaylistDetail? = null,
    val currentlyPlayingId: String? = null,
)

sealed interface PlaylistDetailIntent {
    data object BackClicked : PlaylistDetailIntent
    data class SongClicked(val songId: String) : PlaylistDetailIntent
    data object PlayAllClicked : PlaylistDetailIntent
    data object ShuffleClicked : PlaylistDetailIntent
    data object LikePlaylistClicked : PlaylistDetailIntent
    data object DownloadClicked : PlaylistDetailIntent
}

sealed interface PlaylistDetailEffect {
    data object NavigateBack : PlaylistDetailEffect
    data object NavigateToNowPlaying : PlaylistDetailEffect
    data class ShowError(val message: String) : PlaylistDetailEffect
}
