package com.turkcell.lyraapp.data.playback

import android.content.Context
import android.os.Looper
import androidx.annotation.MainThread
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.remote.SongApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoPlayerPlaybackRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songApiService: SongApiService,
) : PlaybackRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // ExoPlayer'ın main looper üzerinde çalışması için Looper.getMainLooper() verilir.
    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setLooper(Looper.getMainLooper())
        .build()

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val queue = mutableListOf<Song>()
    private var queueIndex = -1
    private var progressJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            @MainThread
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressTicker() else stopProgressTicker()
            }

            @MainThread
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    scope.launch { handlePlaybackEnded() }
                }
            }
        })
    }

    override suspend fun playSong(song: Song) {
        val existingIndex = queue.indexOfFirst { it.id == song.id }
        if (existingIndex >= 0) {
            queueIndex = existingIndex
        } else {
            queue.add(song)
            queueIndex = queue.lastIndex
        }
        loadAndPlay(song)
    }

    override suspend fun pause() {
        withContext(Dispatchers.Main) { player.pause() }
    }

    override suspend fun resume() {
        if (_playbackState.value.currentSong == null) return
        withContext(Dispatchers.Main) { player.play() }
    }

    override suspend fun next() {
        if (queue.isEmpty()) return
        queueIndex = (queueIndex + 1) % queue.size
        loadAndPlay(queue[queueIndex])
    }

    override suspend fun previous() {
        if (queue.isEmpty()) return
        val positionMs = withContext(Dispatchers.Main) { player.currentPosition }
        if (positionMs > RESTART_THRESHOLD_MS) {
            withContext(Dispatchers.Main) { player.seekTo(0L) }
        } else {
            queueIndex = if (queueIndex <= 0) queue.lastIndex else queueIndex - 1
            loadAndPlay(queue[queueIndex])
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
        val durationMs = withContext(Dispatchers.Main) { player.duration }
        if (durationMs <= 0L) return
        val targetMs = (progress * durationMs).toLong()
        withContext(Dispatchers.Main) { player.seekTo(targetMs) }
        _playbackState.update {
            it.copy(progress = progress, currentPositionLabel = formatMs(targetMs))
        }
    }

    private suspend fun loadAndPlay(song: Song) {
        _playbackState.update {
            it.copy(
                currentSong = song,
                isPlaying = false,
                progress = 0f,
                currentPositionLabel = "0:00",
            )
        }
        try {
            val url = withContext(Dispatchers.IO) {
                songApiService.getStreamUrl(song.id).data.url
            }
            withContext(Dispatchers.Main) {
                player.setMediaItem(MediaItem.fromUri(url))
                player.prepare()
                player.play()
            }
        } catch (e: Exception) {
            _playbackState.update { it.copy(isPlaying = false) }
        }
    }

    private fun handlePlaybackEnded() {
        if (_playbackState.value.isRepeat) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    player.seekTo(0L)
                    player.play()
                }
            }
        } else {
            scope.launch { next() }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                delay(PROGRESS_INTERVAL_MS)
                val durationMs = player.duration
                val positionMs = player.currentPosition
                if (durationMs > 0L) {
                    _playbackState.update {
                        it.copy(
                            progress = positionMs.toFloat() / durationMs,
                            currentPositionLabel = formatMs(positionMs),
                        )
                    }
                }
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun formatMs(ms: Long): String {
        val totalSeconds = (ms / 1000).toInt()
        return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
    }

    private companion object {
        const val PROGRESS_INTERVAL_MS = 500L
        const val RESTART_THRESHOLD_MS = 3_000L
    }
}
