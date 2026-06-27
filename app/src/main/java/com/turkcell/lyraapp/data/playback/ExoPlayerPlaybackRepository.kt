package com.turkcell.lyraapp.data.playback

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.download.DownloadRepository
import com.turkcell.lyraapp.data.remote.PlaybackApiService
import com.turkcell.lyraapp.data.remote.dto.AdCompleteBodyDto
import com.turkcell.lyraapp.data.remote.dto.PlaybackNextBodyDto
import java.io.ByteArrayOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
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
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class ExoPlayerPlaybackRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playbackApiService: PlaybackApiService,
    private val downloadRepository: DownloadRepository,
) : PlaybackRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var player: ExoPlayer? = null

    @Volatile
    private var playerReady = CompletableDeferred<ExoPlayer>()

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val queue = mutableListOf<Song>()
    private var queueIndex = -1
    private var progressJob: Job? = null
    private var currentMediaItem: MediaItem? = null
    private var pendingAfterAd: PendingAfterAd? = null

    private data class PendingAfterAd(
        val song: Song,
        val streamUrl: String,
        val impressionId: String,
        val artworkBytes: ByteArray,
    )

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startProgressTicker() else stopProgressTicker()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                scope.launch { handlePlaybackEnded() }
            }
        }
    }

    internal fun attachPlayer(attachedPlayer: ExoPlayer) {
        player?.removeListener(playerListener)
        player = attachedPlayer
        attachedPlayer.addListener(playerListener)
        if (!playerReady.isCompleted) {
            playerReady.complete(attachedPlayer)
        }
    }

    internal fun detachPlayer(detachedPlayer: ExoPlayer) {
        if (player !== detachedPlayer) return
        stopProgressTicker()
        detachedPlayer.removeListener(playerListener)
        player = null
        playerReady = CompletableDeferred()
        _playbackState.update { it.copy(isPlaying = false) }
    }

    override suspend fun playSong(song: Song) {
        if (pendingAfterAd != null) return
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
        withContext(Dispatchers.Main) { player?.pause() }
    }

    override suspend fun resume() {
        if (_playbackState.value.currentSong == null) return
        val mediaItem = currentMediaItem ?: return
        val activePlayer = getOrStartPlayer()
        withContext(Dispatchers.Main) {
            if (activePlayer.mediaItemCount == 0) {
                activePlayer.setMediaItem(mediaItem)
                activePlayer.prepare()
            }
            activePlayer.play()
        }
    }

    override suspend fun next() {
        if (pendingAfterAd != null) return
        if (queue.isEmpty()) return
        queueIndex = (queueIndex + 1) % queue.size
        loadAndPlay(queue[queueIndex])
    }

    override suspend fun previous() {
        if (pendingAfterAd != null) return
        if (queue.isEmpty()) return
        val positionMs = withContext(Dispatchers.Main) { player?.currentPosition ?: 0L }
        if (positionMs > RESTART_THRESHOLD_MS) {
            withContext(Dispatchers.Main) { player?.seekTo(0L) }
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
        if (pendingAfterAd != null) return
        val activePlayer = player ?: return
        val durationMs = withContext(Dispatchers.Main) { activePlayer.duration }
        if (durationMs <= 0L) return
        val targetMs = (progress * durationMs).toLong()
        withContext(Dispatchers.Main) { activePlayer.seekTo(targetMs) }
        _playbackState.update {
            it.copy(progress = progress, currentPositionLabel = formatMs(targetMs))
        }
    }

    private suspend fun loadAndPlay(song: Song) {
        withContext(Dispatchers.Main) {
            player?.stop()
            player?.clearMediaItems()
        }
        _playbackState.update {
            it.copy(
                currentSong = song,
                isPlaying = false,
                isPlayingAd = false,
                adTitle = null,
                adAdvertiser = null,
                progress = 0f,
                currentPositionLabel = "0:00",
            )
        }
        try {
            val localPath = withContext(Dispatchers.IO) {
                downloadRepository.getLocalPath(song.id)
            }
            if (localPath != null) {
                playUri(
                    uri = android.net.Uri.fromFile(java.io.File(localPath)).toString(),
                    song = song,
                )
                return
            }

            val response = withContext(Dispatchers.IO) {
                playbackApiService.playbackNext(PlaybackNextBodyDto(song.id))
            }
            val data = response.data
            val artworkBytes = generateGradientArtwork(
                song.artworkStartColor.toInt(),
                song.artworkEndColor.toInt(),
            )

            if (data.type == "ad" && data.ad != null && data.adStream != null && data.impressionId != null) {
                pendingAfterAd = PendingAfterAd(
                    song = song,
                    streamUrl = data.stream.url,
                    impressionId = data.impressionId,
                    artworkBytes = artworkBytes,
                )
                _playbackState.update {
                    it.copy(
                        isPlayingAd = true,
                        adTitle = data.ad.title,
                        adAdvertiser = data.ad.advertiser,
                    )
                }
                playUri(
                    uri = data.adStream.url,
                    mediaId = data.ad.id,
                    title = data.ad.title,
                    artist = data.ad.advertiser,
                    artworkBytes = artworkBytes,
                )
            } else {
                playUri(
                    uri = data.stream.url,
                    song = song,
                    artworkBytes = artworkBytes,
                )
            }
        } catch (e: Exception) {
            _playbackState.update { it.copy(isPlaying = false, isPlayingAd = false) }
        }
    }

    private suspend fun playUri(
        uri: String,
        song: Song,
        artworkBytes: ByteArray = generateGradientArtwork(
            song.artworkStartColor.toInt(),
            song.artworkEndColor.toInt(),
        ),
    ) {
        playUri(uri, song.id, song.title, song.artist, artworkBytes)
    }

    private suspend fun playUri(
        uri: String,
        mediaId: String,
        title: String,
        artist: String,
        artworkBytes: ByteArray,
    ) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(mediaId)
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    .build(),
            )
            .build()
        currentMediaItem = mediaItem
        val activePlayer = getOrStartPlayer()
        withContext(Dispatchers.Main) {
            activePlayer.setMediaItem(mediaItem)
            activePlayer.prepare()
            activePlayer.play()
        }
    }

    private fun handlePlaybackEnded() {
        val pending = pendingAfterAd
        if (pending != null) {
            pendingAfterAd = null
            scope.launch {
                withContext(Dispatchers.IO) {
                    runCatching {
                        playbackApiService.adComplete(AdCompleteBodyDto(pending.impressionId))
                    }
                }
                _playbackState.update {
                    it.copy(
                        isPlayingAd = false,
                        adTitle = null,
                        adAdvertiser = null,
                        progress = 0f,
                        currentPositionLabel = "0:00",
                    )
                }
                playUri(
                    uri = pending.streamUrl,
                    song = pending.song,
                    artworkBytes = pending.artworkBytes,
                )
            }
        } else if (_playbackState.value.isRepeat) {
            scope.launch {
                withContext(Dispatchers.Main) {
                    player?.seekTo(0L)
                    player?.play()
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
                val activePlayer = player ?: break
                val durationMs = activePlayer.duration
                val positionMs = activePlayer.currentPosition
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

    private suspend fun getOrStartPlayer(): ExoPlayer {
        player?.let { return it }
        ContextCompat.startForegroundService(
            context,
            Intent(context, PlaybackService::class.java),
        )
        return withTimeout(PLAYER_START_TIMEOUT_MS) {
            playerReady.await()
        }
    }

    private fun formatMs(ms: Long): String {
        val totalSeconds = (ms / 1000).toInt()
        return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
    }

    private fun generateGradientArtwork(startColor: Int, endColor: Int): ByteArray {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                startColor, endColor,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        bitmap.recycle()
        return stream.toByteArray()
    }

    private companion object {
        const val PROGRESS_INTERVAL_MS = 500L
        const val RESTART_THRESHOLD_MS = 3_000L
        const val PLAYER_START_TIMEOUT_MS = 5_000L
    }
}
