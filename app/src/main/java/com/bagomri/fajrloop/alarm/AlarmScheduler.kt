package com.bagomri.fajrloop.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

/**
 * AlarmScheduler — المسؤول عن جدولة وإلغاء منبه الفجر المحلي
 *
 * يستخدم [AlarmManager.setAlarmClock] على Android 12+
 * لضمان دقة الإطلاق بالثانية وإظهار أيقونة الساعة في شريط الحالة.
 *
 * هذا المكوّن معزول تماماً عن منطق الحلقة والسيرفر (المرحلة 1).
 */
object AlarmScheduler {

    private const val TAG = "AlarmScheduler"
    private const val ALARM_REQUEST_CODE = 1001

    /**
     * جدولة منبه بدقة ثانية على وقت محدد
     *
     * @param context السياق
     * @param triggerAtMillis وقت الرنين بصيغة Unix timestamp بالميلي ثانية
     * @param label تسمية المنبه (لعرضها في الإشعار)
     */
    fun scheduleAlarm(context: Context, triggerAtMillis: Long, label: String = "صلاة الفجر") {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // التحقق من صلاحية SCHEDULE_EXACT_ALARM على Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "SCHEDULE_EXACT_ALARM permission not granted. Cannot schedule exact alarm.")
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_FIRE
            putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmReceiver.EXTRA_TRIGGER_TIME, triggerAtMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // إنشاء AlarmClockInfo لعرض أيقونة الساعة في شريط الحالة
        val showIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_FIRE
        }
        val showPendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE + 1,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)

        // setAlarmClock: يضمن الدقة حتى في Doze Mode ولا يحتاج إذناً إضافياً
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

        val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        Log.d(
            TAG,
            "✅ Alarm scheduled for: ${calendar.time} | label=$label"
        )
    }

    /**
     * إلغاء المنبه المجدوَل
     */
    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_FIRE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "❌ Alarm cancelled")
        } ?: Log.d(TAG, "No alarm to cancel")
    }

    /**
     * التحقق من وجود منبه مجدوَل
     */
    fun isAlarmScheduled(context: Context): Boolean {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_FIRE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    /**
     * جدولة منبه تجريبي بعد N ثانية من الآن (للاختبار السريع)
     */
    fun scheduleTestAlarm(context: Context, secondsFromNow: Int = 10) {
        val triggerAt = System.currentTimeMillis() + (secondsFromNow * 1000L)
        scheduleAlarm(context, triggerAt, "منبه تجريبي")
        Log.d(TAG, "🧪 Test alarm set for $secondsFromNow seconds from now")
    }
}

