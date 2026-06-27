package com.turkcell.lyraapp.ui.player

import com.turkcell.lyraapp.data.playback.Song

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isLiked: Boolean = false,
    val progress: Float = 0f,
    val isPlayingAd: Boolean = false,
    val adTitle: String? = null,
    val adAdvertiser: String? = null,
)

sealed interface PlayerIntent {
    data object PlayPauseClicked : PlayerIntent
    data object NextClicked : PlayerIntent
    data object LikeClicked : PlayerIntent
    data object ExpandClicked : PlayerIntent
}

sealed interface PlayerEffect {
    data object OpenNowPlaying : PlayerEffect
}
