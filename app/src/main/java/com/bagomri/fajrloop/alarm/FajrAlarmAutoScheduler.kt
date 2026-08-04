package com.bagomri.fajrloop.alarm

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bagomri.fajrloop.data.AlarmConfig
import com.bagomri.fajrloop.data.AlarmRepository
import com.bagomri.fajrloop.data.PrayerTimesRepository
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * FajrAlarmAutoScheduler — المسئول المركزي الذاتي لضمان جدولة منبه الفجر القادم 
 * يعمل تلقائياً سواء فتح المستخدم التطبيق أم لم يفتحه، وإعادة الجدولة المستمرة خلفيةً ومحلياً.
 */
object FajrAlarmAutoScheduler {

    private const val TAG = "FajrAlarmAutoScheduler"
    private const val PERIODIC_WORK_NAME = "fajr_alarm_auto_reschedule_work"

    /**
     * إعادة حساب الفجر القادم وجدولته فوراً وبشكل حتمي
     */
    fun scheduleNextFajrAlarm(context: Context): Long {
        val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val hasHalqa = !prefs.getString("current_halqa_id", null).isNullOrEmpty()
        val alarmEnabled = hasHalqa && prefs.getBoolean(AlarmPreferences.KEY_ALARM_ENABLED, true)

        if (!alarmEnabled) {
            Log.d(TAG, "⛔ Alarm disabled or no active halqa. Cancelling alarm.")
            AlarmScheduler.cancelAlarm(context)
            return -1L
        }

        if (TravelModeManager.isTravelModeActive(context)) {
            Log.d(TAG, "✈️ Travel mode active. Skipping alarm scheduling.")
            AlarmScheduler.cancelAlarm(context)
            return -1L
        }

        val prayerTimesRepository = PrayerTimesRepository(context)
        val alarmRepository = AlarmRepository(context)
        val now = System.currentTimeMillis()

        var prayerTimes = prayerTimesRepository.getPrayerTimesForDate(Date())
        val type = prefs.getString("alarm_timing_type", "with") ?: "with"
        val offset = prefs.getInt("alarm_timing_offset_minutes", 0)
        val offsetMillis = offset * 60 * 1000L

        val adjustedToday = when (type) {
            "before" -> prayerTimes.fajr - offsetMillis
            "after" -> prayerTimes.fajr + offsetMillis
            else -> prayerTimes.fajr
        }

        val targetAlarmTime = if (adjustedToday > now) {
            adjustedToday
        } else {
            val tomorrowTimes = prayerTimesRepository.getPrayerTimesForDate(Date(now + 86_400_000L))
            when (type) {
                "before" -> tomorrowTimes.fajr - offsetMillis
                "after" -> tomorrowTimes.fajr + offsetMillis
                else -> tomorrowTimes.fajr
            }
        }

        val currentConfig = alarmRepository.getAlarmConfig()
        alarmRepository.saveAlarmConfig(
            currentConfig.copy(
                triggerTimeMillis = targetAlarmTime,
                enabled = true
            )
        )

        Log.d(TAG, "⏰ Next Fajr Alarm successfully scheduled for: ${Date(targetAlarmTime)}")
        return targetAlarmTime
    }

    /**
     * تفعيل العامل الدوري من WorkManager الذي يضمن فحص وجدولة المنبه كل 6 ساعات حتى في غياب فتح التطبيق
     */
    fun startPeriodicRescheduler(context: Context) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<FajrAlarmWorker>(6, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.d(TAG, "🔄 Periodic Fajr Alarm WorkManager registered (every 6h)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start periodic rescheduler", e)
        }
    }
}

/**
 * FajrAlarmWorker — العامل الخلفي المنفذ لإعادة الجدولة الدورية عبر WorkManager
 */
class FajrAlarmWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("FajrAlarmWorker", "Executing periodic Fajr alarm check...")
        FajrAlarmAutoScheduler.scheduleNextFajrAlarm(context)
        return Result.success()
    }
}
