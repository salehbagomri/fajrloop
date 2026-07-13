package com.bagomri.fajrloop.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BootReceiver — إعادة جدولة المنبه بعد إعادة تشغيل الجهاز
 *
 * Android يحذف جميع AlarmManager المجدولة عند إعادة التشغيل،
 * لذا يجب إعادة الجدولة فوراً بعد اكتمال التشغيل.
 *
 * يتلقى: BOOT_COMPLETED, LOCKED_BOOT_COMPLETED, QUICKBOOT_POWERON
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
     * إعادة جدولة المنبه من التفضيلات المحفوظة محلياً
     *
     * في المرحلة الأولى: يعيد جدولة المنبه المحفوظ في SharedPreferences.
     * في المراحل التالية: سيقرأ من بيانات الحلقة النشطة.
     */
    private fun rescheduleAlarms(context: Context) {
        val prefs = context.getSharedPreferences(
            AlarmPreferences.PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val savedAlarmTime = prefs.getLong(AlarmPreferences.KEY_ALARM_TIME_MILLIS, -1L)
        val alarmLabel = prefs.getString(AlarmPreferences.KEY_ALARM_LABEL, "صلاة الفجر") ?: "صلاة الفجر"
        val alarmEnabled = prefs.getBoolean(AlarmPreferences.KEY_ALARM_ENABLED, false)

        if (!alarmEnabled || savedAlarmTime == -1L) {
            Log.d(TAG, "No saved alarm to reschedule")
            return
        }

        // إذا كان الوقت قد مضى — جدوله للغد
        val now = System.currentTimeMillis()
        val targetTime = if (savedAlarmTime > now) {
            savedAlarmTime
        } else {
            // أضف يوماً كاملاً (86400000 ms) — في المرحلة التالية سيتم حساب الفجر الفعلي
            savedAlarmTime + 86_400_000L
        }

        AlarmScheduler.scheduleAlarm(context, targetTime, alarmLabel)
        Log.d(TAG, "✅ Alarm rescheduled after boot: $alarmLabel at $targetTime")
    }
}

