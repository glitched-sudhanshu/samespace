package com.example.samespace.exoplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.service.media.MediaBrowserService
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import com.example.samespace.R
import com.example.samespace.exoplayer.Constants.NOTIFICATION_CHANNEL_ID

class MusicService : MediaBrowserService() {
    private var mMediaSession: MediaSession? = null
    private lateinit var mStateBuilder: PlaybackState.Builder
    private var exoPlayer: ExoPlayer? = null
    private val mMediaSessionCallback =
        object : MediaSession.Callback() {
            override fun onPlayFromUri(
                uri: Uri?,
                extras: Bundle?,
            ) {
                super.onPlayFromUri(uri, extras)
            }

            override fun onPause() {
                super.onPause()
                pause()
            }

            override fun onStop() {
                super.onStop()
                stop()
            }
        }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        createNotification()
        mMediaSession =
            MediaSession(applicationContext, "tag for debugging").apply {
                setFlags(
                    MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS,
                )
                mStateBuilder =
                    PlaybackState.Builder()
                        .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PLAY_PAUSE)
                setPlaybackState(mStateBuilder.build())
                setCallback(mMediaSessionCallback)
                setSessionToken(sessionToken)
                isActive = true
            }
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(applicationContext).build()
    }

    private fun pause() {
        exoPlayer?.apply {
            playWhenReady = false
            if (playbackState == PlaybackState.STATE_PLAYING) {
                updatePlaybackState(PlaybackState.STATE_PAUSED)
            }
        }
    }

    private fun stop() {
        exoPlayer?.playWhenReady = false
        exoPlayer?.release()
        exoPlayer = null
        updatePlaybackState(PlaybackState.STATE_NONE)
        mMediaSession?.isActive = false
        mMediaSession?.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun updatePlaybackState(state: Int) {
        mMediaSession?.setPlaybackState(
            PlaybackState.Builder().setState(
                state,
                0L,
                1.0f,
            ).build(),
        )
    }

    override fun onGetRoot(
        p0: String,
        p1: Int,
        p2: Bundle?,
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        p0: String,
        p1: Result<MutableList<MediaBrowser.MediaItem>>,
    ) {
        TODO("Not yet implemented")
    }

    private fun createNotification(): Notification {
        val notificationManager =
            ContextCompat.getSystemService(baseContext, NotificationManager::class.java)
        val activityIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let {
                PendingIntent.getActivity(baseContext, 0, it, PendingIntent.FLAG_IMMUTABLE)
            }
        val builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel =
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "Music Service Channel",
                        NotificationManager.IMPORTANCE_HIGH,
                    )
                notificationChannel.enableLights(true)
                notificationChannel.enableVibration(false)
                notificationManager?.createNotificationChannel(notificationChannel)
                Notification.Builder(baseContext, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("Music Player")
                    .setContentText("Playing music")
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_app_icon)
                    .setContentIntent(activityIntent)
            } else {
                Notification.Builder(baseContext)
                    .setContentTitle("Music Player")
                    .setContentText("Playing music")
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_app_icon)
                    .setContentIntent(activityIntent)
            }
        val notificationItem = builder.build()
        notificationManager?.notify(1234, notificationItem)
        return notificationItem
    }
}

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "musicplayer_channel"
}
