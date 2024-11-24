package com.may.amazingmusic.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.may.amazingmusic.constant.IntentConst.ALARM_TIME_ACTION
import com.may.amazingmusic.utils.DataStoreManager
import com.may.amazingmusic.utils.isTrue
import com.may.amazingmusic.utils.player.PlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * @Author Jensen
 * @Date 2024/11/24 22:10
 */
class AlarmReceiver : BroadcastReceiver() {
    private val TAG = this.javaClass.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "onReceive: context=$context, intent=$intent, action=${intent?.action}")
        if (intent?.action == ALARM_TIME_ACTION) {
            CoroutineScope(Dispatchers.Main).launch {
                if (DataStoreManager.stopUntilPlayCompleted.first().isTrue()) {
                    PlayerManager.stopUntilThisOver = true
                } else {
                    PlayerManager.release()
                    DataStoreManager.updateTimerOpened(false)
                }
            }
        }
    }
}