package com.turkcell.lyraapp.ui.nowplaying

import com.turkcell.lyraapp.data.download.DownloadStatus
import com.turkcell.lyraapp.data.playback.Song

data class NowPlayingUiState(
    val currentSong: Song? = null,
    val sourceName: String = "Gece Vardiyası",
    val isPlaying: Boolean = false,
    val isLiked: Boolean = false,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val progress: Float = 0f,
    val currentPositionLabel: String = "0:00",
    val downloadStatus: DownloadStatus = DownloadStatus.NotDownloaded,
)

sealed interface NowPlayingIntent {
    data object PlayPauseClicked : NowPlayingIntent
    data object NextClicked : NowPlayingIntent
    data object PreviousClicked : NowPlayingIntent
    data object LikeClicked : NowPlayingIntent
    data object ShuffleClicked : NowPlayingIntent
    data object RepeatClicked : NowPlayingIntent
    data class SeekTo(val progress: Float) : NowPlayingIntent
    data object CollapseClicked : NowPlayingIntent
    data object DownloadClicked : NowPlayingIntent
    data object RemoveDownloadClicked : NowPlayingIntent
}

sealed interface NowPlayingEffect {
    data object Collapse : NowPlayingEffect
    data class ShowSnackbar(val message: String) : NowPlayingEffect
}
