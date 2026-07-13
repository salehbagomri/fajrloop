package com.bagomri.fajrloop.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * AlarmDismissReceiver — استقبال أمر إيقاف المنبه من الإشعار
 */
class AlarmDismissReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmDismissReceiver"
        const val ACTION_DISMISS = "com.bagomri.fajrloop.ACTION_ALARM_DISMISS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DISMISS) return
        Log.d(TAG, "Dismiss action received — stopping alarm service")

        val stopIntent = Intent(context, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_STOP_ALARM
        }
        context.startService(stopIntent)
    }
}

