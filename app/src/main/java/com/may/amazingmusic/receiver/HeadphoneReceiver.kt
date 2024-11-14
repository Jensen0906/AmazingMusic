package com.may.amazingmusic.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.may.amazingmusic.utils.player.PlayerManager

/**
 *
 * @author May
 * @date 2024/10/31 21:13
 * @description HeadphoneReceiver
 */
class HeadphoneReceiver : BroadcastReceiver() {
    private val TAG = this.javaClass.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "onReceive: action=${intent?.action}")
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent?.action) {
            PlayerManager.player?.pause()
        }
    }
}