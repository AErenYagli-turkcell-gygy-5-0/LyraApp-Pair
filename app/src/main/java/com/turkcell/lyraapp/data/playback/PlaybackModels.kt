package com.turkcell.lyraapp.data.playback

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val duration: String,
    val artworkStartColor: Long,
    val artworkEndColor: Long,
)

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isLiked: Boolean = false,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val progress: Float = 0f,
    val currentPositionLabel: String = "0:00",
)
