package com.turkcell.lyraapp.data.playback

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(UnstableApi::class)
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var playbackRepository: ExoPlayerPlaybackRepository

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        val forwardingPlayer = object : ForwardingPlayer(playbackRepository.player) {

            override fun seekToNext() {
                serviceScope.launch { playbackRepository.next() }
            }

            override fun seekToPrevious() {
                serviceScope.launch { playbackRepository.previous() }
            }

            override fun seekToNextMediaItem() {
                serviceScope.launch { playbackRepository.next() }
            }

            override fun seekToPreviousMediaItem() {
                serviceScope.launch { playbackRepository.previous() }
            }

            override fun hasNextMediaItem(): Boolean = true

            override fun hasPreviousMediaItem(): Boolean = true

            override fun isCommandAvailable(command: Int): Boolean {
                return when (command) {
                    Player.COMMAND_SEEK_TO_NEXT,
                    Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    Player.COMMAND_SEEK_TO_PREVIOUS,
                    Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> true
                    else -> super.isCommandAvailable(command)
                }
            }

            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            }
        }

        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setCallback(PlaybackSessionCallback())
            .setCustomLayout(buildCustomLayout(isLiked = false))
            .build()

        serviceScope.launch {
            playbackRepository.playbackState.collect { state ->
                mediaSession?.setCustomLayout(buildCustomLayout(state.isLiked))
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run { release() }
        mediaSession = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun buildCustomLayout(isLiked: Boolean): List<CommandButton> {
        val icon = if (isLiked) {
            CommandButton.ICON_HEART_FILLED
        } else {
            CommandButton.ICON_HEART_UNFILLED
        }
        val likeButton = CommandButton.Builder(icon)
            .setSessionCommand(COMMAND_TOGGLE_LIKE)
            .setDisplayName(if (isLiked) "Begeniyi Kaldir" else "Begen")
            .build()
        return listOf(likeButton)
    }

    private inner class PlaybackSessionCallback : MediaSession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult
                .DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
                .add(COMMAND_TOGGLE_LIKE)
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            if (customCommand == COMMAND_TOGGLE_LIKE) {
                serviceScope.launch { playbackRepository.toggleLike() }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    companion object {
        private val COMMAND_TOGGLE_LIKE = SessionCommand("TOGGLE_LIKE", Bundle.EMPTY)
    }
}
