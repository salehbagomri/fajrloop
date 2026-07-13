package com.bagomri.fajrloop.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bagomri.fajrloop.data.PrayerTimesRepository
import java.util.Date

/**
 * BootReceiver — إعادة جدولة منبه الفجر تلقائياً بعد إقلاع الهاتف
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Boot event received: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.LOCKED_BOOT_COMPLETED",
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                rescheduleAlarms(context)
            }
        }
    }

    /**
     * إعادة حساب الفجر وجدولة المنبه تلقائياً بناءً على طريقة الحساب والموقع الجغرافي الفعلي
     */
    private fun rescheduleAlarms(context: Context) {
        val prefs = context.getSharedPreferences(
            AlarmPreferences.PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val alarmEnabled = prefs.getBoolean(AlarmPreferences.KEY_ALARM_ENABLED, false)
        val alarmLabel = prefs.getString(AlarmPreferences.KEY_ALARM_LABEL, "صلاة الفجر") ?: "صلاة الفجر"

        if (!alarmEnabled) {
            Log.d(TAG, "Alarm not enabled, skipping reschedule")
            return
        }

        val prayerTimesRepository = PrayerTimesRepository(context)
        val now = System.currentTimeMillis()
        var prayerTimes = prayerTimesRepository.getPrayerTimesForDate(Date())
        
        if (prayerTimes.fajr < now) {
            val tomorrow = Date(now + 86_400_000L)
            prayerTimes = prayerTimesRepository.getPrayerTimesForDate(tomorrow)
        }

        // تطبيق خيارات إزاحة توقيت المنبه
        val type = prefs.getString("alarm_timing_type", "with") ?: "with"
        val offset = prefs.getInt("alarm_timing_offset_minutes", 0)
        val offsetMillis = offset * 60 * 1000L
        
        val adjustedFajr = when (type) {
            "before" -> prayerTimes.fajr - offsetMillis
            "after" -> prayerTimes.fajr + offsetMillis
            else -> prayerTimes.fajr
        }

        val targetAlarmTime = if (adjustedFajr > now) {
            adjustedFajr
        } else {
            val tomorrowTimes = prayerTimesRepository.getPrayerTimesForDate(Date(now + 86_400_000L))
            when (type) {
                "before" -> tomorrowTimes.fajr - offsetMillis
                "after" -> tomorrowTimes.fajr + offsetMillis
                else -> tomorrowTimes.fajr
            }
        }

        AlarmScheduler.scheduleAlarm(context, targetAlarmTime, alarmLabel)
        
        prefs.edit().putLong(AlarmPreferences.KEY_ALARM_TIME_MILLIS, targetAlarmTime).apply()
        
        Log.d(TAG, "✅ Alarm rescheduled after boot: $alarmLabel at $targetAlarmTime")
    }
}
