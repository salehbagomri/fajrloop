package com.bagomri.fajrloop.alarm

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.data.AlarmConfig
import com.bagomri.fajrloop.data.AlarmRepository
import com.bagomri.fajrloop.data.PrayerTimesRepository
import com.google.firebase.database.FirebaseDatabase
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * FajrAlarmAutoScheduler — المسئول المركزي الذاتي لضمان جدولة منبه الفجر القادم (محلياً وسحابياً بحساب الفجر الأبكر الموحد للحلقة)
 */
object FajrAlarmAutoScheduler {

    private const val TAG = "FajrAlarmAutoScheduler"
    private const val PERIODIC_WORK_NAME = "fajr_alarm_auto_reschedule_work"

    /**
     * حساب وإرسال وقت الفجر المحلي للمستخدم في عقدة اعضاء الحلقة بـ Firebase
     */
    fun syncMemberFajrTime(context: Context, halqaId: String, fajrTimeMillis: Long) {
        val uid = AuthManager.getUserId() ?: return
        val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val lastSynced = prefs.getLong("last_synced_fajr_time_$uid", -1L)
        if (lastSynced == fajrTimeMillis) {
            return
        }

        try {
            prefs.edit().putLong("last_synced_fajr_time_$uid", fajrTimeMillis).apply()
            FirebaseDatabase.getInstance()
                .getReference("halqas")
                .child(halqaId)
                .child("members")
                .child(uid)
                .child("fajrTimeMillis")
                .setValue(fajrTimeMillis)
            Log.d(TAG, "Synced member fajrTimeMillis: $fajrTimeMillis to Halqa: $halqaId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync member fajr time", e)
        }
    }

    /**
     * إعادة حساب الفجر القادم وجدولته فوراً وبشكل حتمي (مع مراعاة توقيت الفجر الأبكر الموحد للحلقة)
     */
    fun scheduleNextFajrAlarm(context: Context): Long {
        val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
        val hasHalqa = !halqaId.isNullOrEmpty()
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
        val type = prefs.getString(AlarmPreferences.KEY_ALARM_TIMING_TYPE, "with") ?: "with"
        val offset = prefs.getInt(AlarmPreferences.KEY_ALARM_TIMING_OFFSET_MINUTES, 0)
        val offsetMillis = offset * 60 * 1000L

        val adjustedToday = when (type) {
            "before" -> prayerTimes.fajr - offsetMillis
            "after"  -> prayerTimes.fajr + offsetMillis
            else     -> prayerTimes.fajr
        }

        val localNextFajr = if (adjustedToday > now) {
            adjustedToday
        } else {
            val tomorrowTimes = prayerTimesRepository.getPrayerTimesForDate(Date(now + 86_400_000L))
            when (type) {
                "before" -> tomorrowTimes.fajr - offsetMillis
                "after"  -> tomorrowTimes.fajr + offsetMillis
                else     -> tomorrowTimes.fajr
            }
        }

        // ✅ الإصلاح 1: مزامنة وقت فجر العضو لـ Firebase لتفعيل ميزة "الفجر الموحد"
        if (!halqaId.isNullOrEmpty()) {
            syncMemberFajrTime(context, halqaId, localNextFajr)
            Log.d(TAG, "☀️ Synced own fajrTimeMillis to Firebase: ${Date(localNextFajr)}")
        }

        // قراءة توقيت الفجر الأبكر الموحد للحلقة المخزن
        val halqaEarliestFajr = prefs.getLong(AlarmPreferences.KEY_HALQA_EARLIEST_FAJR_MILLIS, -1L)

        val targetAlarmTime = if (halqaEarliestFajr > now && halqaEarliestFajr < localNextFajr) {
            Log.d(TAG, "🌟 Unified Halqa Timing ACTIVE: Using earliest Fajr (${Date(halqaEarliestFajr)}) instead of local (${Date(localNextFajr)})")
            halqaEarliestFajr
        } else {
            localNextFajr
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
     * تفعيل العامل الدوري من WorkManager.
     * ✅ الإصلاح 2: تم تقليل الدورية من 6 ساعات إلى 2 ساعة لتقليل احتمال فوت الجدولة قبل الفجر.
     * @param forceReplace إذا true يستبدل العامل الموجود (يستخدم بعد الإقلاع).
     */
    fun startPeriodicRescheduler(context: Context, forceReplace: Boolean = false) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<FajrAlarmWorker>(2, TimeUnit.HOURS)
                .build()

            // بعد الإقلاع نستبدل العامل القديم لضمان تفعيل الدورية الجديدة (2 ساعات)
            val policy = if (forceReplace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
                         else ExistingPeriodicWorkPolicy.KEEP

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                policy,
                workRequest
            )
            Log.d(TAG, "🔄 Periodic Fajr Alarm WorkManager registered (every 2h, forceReplace=$forceReplace)")
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
