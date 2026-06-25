package com.turkcell.lyraapp.data.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
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
    private var player: ExoPlayer? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val notificationManager by lazy { NotificationManagerCompat.from(this) }

    override fun onCreate() {
        super.onCreate()

        val exoPlayer = ExoPlayer.Builder(this).build()
        player = exoPlayer
        playbackRepository.attachPlayer(exoPlayer)

        val forwardingPlayer = object : ForwardingPlayer(exoPlayer) {

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
                    COMMAND_SEEK_TO_NEXT,
                    COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    COMMAND_SEEK_TO_PREVIOUS,
                    COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM -> true
                    else -> super.isCommandAvailable(command)
                }
            }

            override fun getAvailableCommands(): Player.Commands {
                return super.getAvailableCommands().buildUpon()
                    .add(COMMAND_SEEK_TO_NEXT)
                    .add(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(COMMAND_SEEK_TO_PREVIOUS)
                    .add(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .build()
            }
        }

        val sessionActivityIntent = PendingIntent.getActivity(
            this,
            0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        mediaSession = MediaSession.Builder(this, forwardingPlayer)
            .setSessionActivity(sessionActivityIntent)
            .setCallback(PlaybackSessionCallback())
            .setMediaButtonPreferences(buildMediaButtonPreferences(isLiked = false))
            .build()

        createNotificationChannel()
        exoPlayer.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (
                    events.containsAny(
                        Player.EVENT_MEDIA_ITEM_TRANSITION,
                        Player.EVENT_MEDIA_METADATA_CHANGED,
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_IS_PLAYING_CHANGED,
                    )
                ) {
                    publishPlaybackNotification()
                }
            }
        })

        serviceScope.launch {
            playbackRepository.playbackState.collect { state ->
                mediaSession?.setMediaButtonPreferences(
                    buildMediaButtonPreferences(state.isLiked),
                )
                publishPlaybackNotification()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE_PLAYBACK -> serviceScope.launch {
                if (playbackRepository.playbackState.value.isPlaying) {
                    playbackRepository.pause()
                } else {
                    playbackRepository.resume()
                }
            }
            ACTION_PREVIOUS -> serviceScope.launch { playbackRepository.previous() }
            ACTION_NEXT -> serviceScope.launch { playbackRepository.next() }
            ACTION_TOGGLE_LIKE -> serviceScope.launch { playbackRepository.toggleLike() }
            ACTION_STOP -> stopPlaybackAndService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onUpdateNotification(
        session: MediaSession,
        startInForegroundRequired: Boolean,
    ) {
        publishPlaybackNotification()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopPlaybackAndService()
    }

    override fun onDestroy() {
        player?.let(playbackRepository::detachPlayer)
        mediaSession?.run { release() }
        mediaSession = null
        player?.release()
        player = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun stopPlaybackAndService() {
        player?.run {
            stop()
            clearMediaItems()
        }
        notificationManager.cancel(NOTIFICATION_ID)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun publishPlaybackNotification() {
        val session = mediaSession ?: return
        val activePlayer = player ?: return
        val mediaItem = activePlayer.currentMediaItem ?: return
        val metadata = mediaItem.mediaMetadata
        val isPlaying = activePlayer.isPlaying
        val notification = buildPlaybackNotification(
            title = metadata.title?.toString() ?: getString(com.turkcell.lyraapp.R.string.app_name),
            artist = metadata.artist?.toString().orEmpty(),
            artworkData = metadata.artworkData,
            isPlaying = isPlaying,
            isLiked = playbackRepository.playbackState.value.isLiked,
            session = session,
        )

        if (
            activePlayer.playWhenReady &&
            activePlayer.playbackState in listOf(Player.STATE_BUFFERING, Player.STATE_READY)
        ) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun buildPlaybackNotification(
        title: String,
        artist: String,
        artworkData: ByteArray?,
        isPlaying: Boolean,
        isLiked: Boolean,
        session: MediaSession,
    ): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        val artwork = artworkData?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }
        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseLabel = if (isPlaying) "Duraklat" else "Oynat"

        return builder
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(artist)
            .setLargeIcon(artwork)
            .setContentIntent(session.sessionActivity)
            .setDeleteIntent(servicePendingIntent(ACTION_STOP, REQUEST_STOP))
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_media_previous,
                    "Onceki",
                    servicePendingIntent(ACTION_PREVIOUS, REQUEST_PREVIOUS),
                ).build(),
            )
            .addAction(
                Notification.Action.Builder(
                    playPauseIcon,
                    playPauseLabel,
                    servicePendingIntent(ACTION_TOGGLE_PLAYBACK, REQUEST_TOGGLE_PLAYBACK),
                ).build(),
            )
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_media_next,
                    "Sonraki",
                    servicePendingIntent(ACTION_NEXT, REQUEST_NEXT),
                ).build(),
            )
            .addAction(
                Notification.Action.Builder(
                    if (isLiked) android.R.drawable.btn_star_big_on
                    else android.R.drawable.btn_star_big_off,
                    if (isLiked) "Begeniyi kaldir" else "Begen",
                    servicePendingIntent(ACTION_TOGGLE_LIKE, REQUEST_TOGGLE_LIKE),
                ).build(),
            )
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(session.platformToken)
                    .setShowActionsInCompactView(0, 1, 2),
            )
            .build()
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, PlaybackService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Muzik oynatma",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Calan sarki ve medya kontrolleri"
                setShowBadge(false)
            },
        )
    }

    private fun buildMediaButtonPreferences(isLiked: Boolean): List<CommandButton> {
        val icon = if (isLiked) {
            CommandButton.ICON_HEART_FILLED
        } else {
            CommandButton.ICON_HEART_UNFILLED
        }
        val likeButton = CommandButton.Builder(icon)
            .setSessionCommand(COMMAND_TOGGLE_LIKE)
            .setDisplayName(if (isLiked) "Begeniyi Kaldir" else "Begen")
            .setSlots(
                CommandButton.SLOT_BACK_SECONDARY,
                CommandButton.SLOT_OVERFLOW,
            )
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
                .setMediaButtonPreferences(
                    buildMediaButtonPreferences(
                        playbackRepository.playbackState.value.isLiked,
                    ),
                )
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
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "lyra_playback"
        private const val ACTION_TOGGLE_PLAYBACK = "com.turkcell.lyraapp.action.TOGGLE_PLAYBACK"
        private const val ACTION_PREVIOUS = "com.turkcell.lyraapp.action.PREVIOUS"
        private const val ACTION_NEXT = "com.turkcell.lyraapp.action.NEXT"
        private const val ACTION_TOGGLE_LIKE = "com.turkcell.lyraapp.action.TOGGLE_LIKE"
        private const val ACTION_STOP = "com.turkcell.lyraapp.action.STOP"
        private const val REQUEST_TOGGLE_PLAYBACK = 1
        private const val REQUEST_PREVIOUS = 2
        private const val REQUEST_NEXT = 3
        private const val REQUEST_TOGGLE_LIKE = 4
        private const val REQUEST_STOP = 5
        private val COMMAND_TOGGLE_LIKE = SessionCommand("TOGGLE_LIKE", Bundle.EMPTY)
    }
}
