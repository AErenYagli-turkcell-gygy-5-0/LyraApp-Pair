package com.turkcell.lyraapp.data.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class MockPlaybackRepository @Inject constructor() : PlaybackRepository {

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val queue: MutableList<Song> = mutableListOf()
    private var queueIndex: Int = -1

    override suspend fun playSong(song: Song) {
        val existingIndex = queue.indexOfFirst { it.id == song.id }
        if (existingIndex >= 0) {
            queueIndex = existingIndex
        } else {
            queue.add(song)
            queueIndex = queue.lastIndex
        }
        _playbackState.update {
            it.copy(
                currentSong = song,
                isPlaying = true,
                progress = 0f,
                currentPositionLabel = "0:00",
            )
        }
    }

    override suspend fun pause() {
        _playbackState.update { it.copy(isPlaying = false) }
    }

    override suspend fun resume() {
        if (_playbackState.value.currentSong != null) {
            _playbackState.update { it.copy(isPlaying = true) }
        }
    }

    override suspend fun next() {
        if (queue.isEmpty()) return
        queueIndex = (queueIndex + 1) % queue.size
        val nextSong = queue[queueIndex]
        _playbackState.update {
            it.copy(
                currentSong = nextSong,
                isPlaying = true,
                progress = 0f,
                currentPositionLabel = "0:00",
            )
        }
    }

    override suspend fun previous() {
        if (queue.isEmpty()) return
        queueIndex = if (queueIndex <= 0) queue.lastIndex else queueIndex - 1
        val prevSong = queue[queueIndex]
        _playbackState.update {
            it.copy(
                currentSong = prevSong,
                isPlaying = true,
                progress = 0f,
                currentPositionLabel = "0:00",
            )
        }
    }

    override suspend fun toggleLike() {
        _playbackState.update { it.copy(isLiked = !it.isLiked) }
    }

    override suspend fun toggleShuffle() {
        _playbackState.update { it.copy(isShuffle = !it.isShuffle) }
    }

    override suspend fun toggleRepeat() {
        _playbackState.update { it.copy(isRepeat = !it.isRepeat) }
    }

    override suspend fun seekTo(progress: Float) {
        val song = _playbackState.value.currentSong ?: return
        val totalSeconds = parseDurationToSeconds(song.duration)
        val currentSeconds = (progress * totalSeconds).toInt()
        val label = formatSeconds(currentSeconds)
        _playbackState.update { it.copy(progress = progress, currentPositionLabel = label) }
    }

    private fun parseDurationToSeconds(duration: String): Int {
        val parts = duration.split(":")
        if (parts.size != 2) return 0
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    }

    private fun formatSeconds(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }
}
