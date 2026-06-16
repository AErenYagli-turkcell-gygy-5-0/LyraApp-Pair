package com.turkcell.lyraapp.data.playback

import kotlinx.coroutines.flow.StateFlow

interface PlaybackRepository {
    val playbackState: StateFlow<PlaybackState>

    suspend fun playSong(song: Song)
    suspend fun pause()
    suspend fun resume()
    suspend fun next()
    suspend fun previous()
    suspend fun toggleLike()
    suspend fun toggleShuffle()
    suspend fun toggleRepeat()
    suspend fun seekTo(progress: Float)
}
