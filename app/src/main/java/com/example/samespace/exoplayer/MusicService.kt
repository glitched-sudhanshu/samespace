package com.example.samespace.exoplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.samespace.MyApp
import com.example.samespace.exoplayer.Constants.NOTIFICATION_CHANNEL_ID

class MusicService : Service() {
    private lateinit var exoPlayer: ExoPlayer

    private val binder = MusicBinder()
    private val NOTIFICATION_ID = 121

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        exoPlayer = (application as MyApp).exoPlayer
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == Constants.ACTION.STARTFOREGROUND_ACTION) {
            startForeground(NOTIFICATION_ID, createNotification())
            exoPlayer.playWhenReady
        } else if (intent?.action == Constants.ACTION.STOPFOREGROUND_ACTION) {
            stopForeground(true)
            exoPlayer.release()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    fun resumeMusic() {
        if (!exoPlayer.isPlaying) {
            exoPlayer.play()
        }
    }

    fun pauseMusic() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    fun togglePlayer() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun mute() {
        if (exoPlayer.volume != 0f) {
            exoPlayer.volume = 0f
        }
    }

    fun unMute() {
        if (exoPlayer.volume == 0f) {
            exoPlayer.volume = 1f
        }
    }

    fun toggleMute() {
        if (exoPlayer.volume == 0f) {
            exoPlayer.volume = 1f
        } else {
            exoPlayer.volume = 0f
        }
    }

    fun playNextSong() {
        exoPlayer.seekToNextMediaItem()
    }

    fun playPreviousSong() {
        exoPlayer.seekToPreviousMediaItem()
    }

    @OptIn(UnstableApi::class)
    fun setSong(url: String) {
        val mediaItem = androidx.media3.common.MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun setSongs(
        urls: List<String>,
        resetPlaylist: Boolean = false,
    ) {
        val mediaItems =
            urls.map {
                androidx.media3.common.MediaItem.fromUri(it)
            }
        exoPlayer.setMediaItems(mediaItems, resetPlaylist)
        exoPlayer.prepare()
        exoPlayer.play()
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
                    .setSmallIcon(androidx.constraintlayout.widget.R.drawable.abc_btn_switch_to_on_mtrl_00012)
                    .setContentIntent(activityIntent)
            } else {
                Notification.Builder(baseContext)
                    .setContentTitle("Music Player")
                    .setContentText("Playing music")
                    .setOngoing(true)
                    .setSmallIcon(androidx.constraintlayout.widget.R.drawable.abc_btn_switch_to_on_mtrl_00012)
                    .setContentIntent(activityIntent)
            }
        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}

object Constants {
    object ACTION {
        const val STARTFOREGROUND_ACTION = "com.example.samespace.action.START_FOREGROUND"
        const val STOPFOREGROUND_ACTION = "com.example.samespace.action.STOP_FOREGROUND"
    }

    const val NOTIFICATION_CHANNEL_ID = "musicplayer_channel"
}
