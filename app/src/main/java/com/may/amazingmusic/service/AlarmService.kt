package com.may.amazingmusic.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.may.amazingmusic.constant.IntentConst.ALARM_TIME_ACTION
import com.may.amazingmusic.constant.IntentConst.ALARM_TIME_MINUTE
import com.may.amazingmusic.constant.IntentConst.ALARM_TIME_REQUEST_CODE

/**
 * @Author Jensen
 * @Date 2024/11/24 11:33
 */
class AlarmService : Service() {
    private val TAG = this.javaClass.simpleName

    override fun onBind(intent: Intent?): IBinder? {
        val timeInMinutes = intent?.getIntExtra(ALARM_TIME_MINUTE, 5)
        Log.e(TAG, "onBind: time=$timeInMinutes min")
        setOrCancelTimer(timeInMinutes)
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "onUnbind: ")
        setOrCancelTimer(0, isCancel = true)
        return super.onUnbind(intent)
    }

    private fun setOrCancelTimer(timeInMinutes: Int?, isCancel: Boolean = false) {
        if (timeInMinutes == null) return
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ALARM_TIME_ACTION)
        intent.component = ComponentName(packageName, "com.may.amazingmusic.receiver.AlarmReceiver")
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_TIME_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (isCancel) {
            alarmManager.cancel(pendingIntent)
        } else {
            val triggerTime = System.currentTimeMillis() + timeInMinutes * 60 * 1000L
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}