package com.bagomri.fajrloop.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log

/**
 * AlarmReceiver — مستقبل بث إطلاق المنبه من AlarmManager
 *
 * يستيقظ الجهاز عبر WakeLock مؤقت، ثم يُشغّل AlarmSoundService
 * كـ ForegroundService لحماية الرنين من قتل النظام.
 *
 * يحتوي على Watchdog لمنع قتل التطبيق وإيقافه من الخلفية.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "AlarmReceiver"
        const val ACTION_ALARM_FIRE = "com.bagomri.fajrloop.ACTION_ALARM_FIRE"
        const val ACTION_ALARM_WATCHDOG = "com.bagomri.fajrloop.ACTION_ALARM_WATCHDOG"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_TRIGGER_TIME = "extra_trigger_time"

        // WakeLock ثابت لنقل التحكم للخدمة الأمامية بأمان
        @Volatile
        private var wakeLock: PowerManager.WakeLock? = null

        fun acquireWakeLock(context: Context) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock?.let { if (it.isHeld) it.release() }
            wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "FajrLoop::AlarmReceiverWakeLock"
            ).also {
                it.setReferenceCounted(false)
                it.acquire(30_000L) // أقصى مدة 30 ثانية
            }
        }

        fun releaseWakeLock() {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            wakeLock = null
        }

        fun scheduleWatchdog(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_ALARM_WATCHDOG
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerTime = System.currentTimeMillis() + 15_000L // 15 ثانية كحد أقصى للتحقق
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d(TAG, "Watchdog scheduled in 15 seconds")
        }

        fun cancelWatchdog(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = ACTION_ALARM_WATCHDOG
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                999,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Watchdog cancelled")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "🔔 AlarmReceiver.onReceive: action=${intent.action}")

        val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)

        if (intent.action == ACTION_ALARM_FIRE) {
            // تفعيل حالة رنين المنبه النشط في التفضيلات لمنع الإغلاق
            prefs.edit().putBoolean("alarm_active_ringing", true).apply()

            val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "صلاة الفجر"
            val triggerTime = intent.getLongExtra(EXTRA_TRIGGER_TIME, System.currentTimeMillis())

            acquireWakeLock(context)

            // تشغيل الخدمة الأمامية
            val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
                action = AlarmSoundService.ACTION_START_ALARM
                putExtra(AlarmSoundService.EXTRA_ALARM_LABEL, label)
                putExtra(AlarmSoundService.EXTRA_TRIGGER_TIME, triggerTime)
            }
            context.startForegroundService(serviceIntent)
            Log.d(TAG, "▶️ AlarmSoundService started from alarm fire")

            // جدولة Watchdog للاستمرار بمراقبة بقاء الخدمة والشاشة
            scheduleWatchdog(context)

        } else if (intent.action == ACTION_ALARM_WATCHDOG) {
            val isAlarmActive = prefs.getBoolean("alarm_active_ringing", false)
            Log.d(TAG, "Watchdog tick. isAlarmActive=$isAlarmActive")

            if (isAlarmActive) {
                // المنبه يجب أن يكون رنان، ولكنه أغلق! نعيد تشغيله فوراً
                acquireWakeLock(context)

                val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
                    action = AlarmSoundService.ACTION_START_ALARM
                    putExtra(AlarmSoundService.EXTRA_ALARM_LABEL, "صلاة الفجر")
                    putExtra(AlarmSoundService.EXTRA_TRIGGER_TIME, System.currentTimeMillis())
                }
                context.startForegroundService(serviceIntent)
                Log.d(TAG, "▶️ AlarmSoundService restarted by Watchdog")

                // إعادة جدولة Watchdog التالي
                scheduleWatchdog(context)
            }
        }
    }
}
