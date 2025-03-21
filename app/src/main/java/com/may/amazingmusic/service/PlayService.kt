package com.may.amazingmusic.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.DefaultMediaDescriptionAdapter
import androidx.media3.ui.PlayerNotificationManager
import com.may.amazingmusic.constant.BaseWorkConst.REPEAT_MODE_SHUFFLE
import com.may.amazingmusic.constant.BaseWorkConst.REPEAT_MODE_SINGLE
import com.may.amazingmusic.constant.IntentConst.PENDING_INTENT_ACTION
import com.may.amazingmusic.receiver.HeadphoneReceiver
import com.may.amazingmusic.ui.activity.MainActivity
import com.may.amazingmusic.utils.player.PlayerManager


/**
 * @author May
 * @date 2024/9/16 0:23
 * @description PlayService
 */
@SuppressLint("RestrictedApi")
@UnstableApi
class PlayService : Service() {
    private val TAG = this.javaClass.simpleName

    private lateinit var player: ExoPlayer
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private val channelId = "audio_player_channel"
    private val notificationId = 1
    private val headphoneReceiver = HeadphoneReceiver()
    private lateinit var mediaSession: MediaSession

    private var bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)

    override fun onBind(intent: Intent?): IBinder? {
        player = PlayerManager.player ?: ExoPlayer.Builder(this).build().apply {
            repeatMode = if (PlayerManager.repeatModeLiveData.value == REPEAT_MODE_SINGLE) ExoPlayer.REPEAT_MODE_ONE
            else ExoPlayer.REPEAT_MODE_ALL
            shuffleModeEnabled = PlayerManager.repeatModeLiveData.value == REPEAT_MODE_SHUFFLE
        }

        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager

        PlayerManager.player?.let {
            mediaSession = MediaSession.Builder(this, it).setCallback(object : MediaSession.Callback {
                override fun onMediaButtonEvent(
                    session: MediaSession,
                    controllerInfo: MediaSession.ControllerInfo,
                    intent: Intent
                ): Boolean {
                    val keyEvent = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                    } else @Suppress("DEPRECATION") intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                    if (keyEvent?.keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                        PlayerManager.playPreviousSong()
                        return true
                    }
                    return super.onMediaButtonEvent(session, controllerInfo, intent)
                }
            }).build()

        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "音乐播放", NotificationManager.IMPORTANCE_HIGH)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        setPlayerNotification()
        startForeground(notificationId, NotificationCompat.Builder(this, channelId).build())
        registerHeadphone()
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        PlayerManager.release()
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
        unRegisterHeadphone()
        mediaSession.release()
        return super.onUnbind(intent)
    }

    private fun registerHeadphone() {
        val intentFilter = IntentFilter().apply {
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }
        registerReceiver(headphoneReceiver, intentFilter)
    }

    private fun unRegisterHeadphone() {
        unregisterReceiver(headphoneReceiver)
    }

    private fun setPlayerNotification() {
        val intent = Intent(this, MainActivity::class.java)
        intent.action = PENDING_INTENT_ACTION
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        playerNotificationManager = PlayerNotificationManager.Builder(
            this, notificationId, channelId
        ).setMediaDescriptionAdapter(DefaultMediaDescriptionAdapter(pendingIntent)).build().apply {
            setPlayer(player)
            setUseRewindAction(false)
            setUseFastForwardAction(false)
            setMediaSessionToken(mediaSession.platformToken)
        }
    }
}