package com.bagomri.fajrloop.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bagomri.fajrloop.auth.AuthManager

/**
 * BootReceiver — إعادة جدولة منبه الفجر تلقائياً وتنشيط العامل الدوري بعد إقلاع الهاتف.
 *
 * ✅ الإصلاح 3:
 * - نتحقق من تسجيل الدخول قبل الجدولة (لا فائدة إذا المستخدم غير مسجّل)
 * - نستخدم forceReplace=true لضمان أن دورية 2 ساعات تحل محل أي دورية قديمة (6 ساعات)
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

                // لا نجدول المنبه إلا إذا كان المستخدم مسجّل الدخول
                if (!AuthManager.isUserSignedIn()) {
                    Log.d(TAG, "User not signed in. Skipping alarm scheduling on boot.")
                    return
                }

                Log.d(TAG, "Scheduling Fajr alarm and starting periodic rescheduler on boot...")
                FajrAlarmAutoScheduler.scheduleNextFajrAlarm(context)
                // forceReplace=true لضمان تحديث الدورية من 6h إلى 2h على الأجهزة القديمة
                FajrAlarmAutoScheduler.startPeriodicRescheduler(context, forceReplace = true)
            }
        }
    }
}
