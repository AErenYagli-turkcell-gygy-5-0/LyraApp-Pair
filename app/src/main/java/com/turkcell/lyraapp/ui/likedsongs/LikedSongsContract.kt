package com.turkcell.lyraapp.ui.likedsongs

import com.turkcell.lyraapp.data.likedsongs.LikedSong

/**
 * Beğenilen Şarkılar ekranının MVI sözleşmesi: UiState + Intent + Effect (bkz. mvi-contracts.md).
 *
 * Bu iterasyonda çalma işlemleri (Play, Shuffle, Download) ve 3 nokta menü yalnızca
 * UI katmanında tetiklenir; backend hazır olduğunda ilgili Intent'ler eklenir.
 */
data class LikedSongsUiState(
    val isLoading: Boolean = false,
    val songCount: Int = 0,
    val totalDuration: String = "",
    val songs: List<LikedSong> = emptyList(),
    val currentlyPlayingId: String? = null,
)

sealed interface LikedSongsIntent {
    data object BackClicked : LikedSongsIntent
    data class SongClicked(val songId: String) : LikedSongsIntent
}

sealed interface LikedSongsEffect {
    data object NavigateBack : LikedSongsEffect
}
