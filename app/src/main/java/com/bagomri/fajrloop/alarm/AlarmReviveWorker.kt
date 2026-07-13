package com.bagomri.fajrloop.alarm

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * AlarmReviveWorker — يقوم بإعادة تشغيل AlarmSoundService فوراً في حال تم إغلاق التطبيق من Recent Apps
 */
class AlarmReviveWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("AlarmReviveWorker", "Reviving AlarmSoundService...")

        val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_START_ALARM
            putExtra(AlarmSoundService.EXTRA_ALARM_LABEL, "صلاة الفجر")
            putExtra(AlarmSoundService.EXTRA_TRIGGER_TIME, System.currentTimeMillis())
        }

        return try {
            ContextCompat.startForegroundService(context, serviceIntent)
            Log.d("AlarmReviveWorker", "AlarmSoundService successfully revived as Foreground Service.")
            Result.success()
        } catch (e: Exception) {
            Log.e("AlarmReviveWorker", "Failed to revive AlarmSoundService", e)
            Result.failure()
        }
    }
}
