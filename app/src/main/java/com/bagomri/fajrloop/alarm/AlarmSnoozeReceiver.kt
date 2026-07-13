package com.bagomri.fajrloop.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * AlarmSnoozeReceiver — تأجيل المنبه (مرحبة للمستقبل)
 *
 * في المرحلة الأولى: غير مُفعّل — موجود فقط كـ placeholder في Manifest.
 */
class AlarmSnoozeReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmSnoozeReceiver"
        const val ACTION_SNOOZE = "com.bagomri.fajrloop.ACTION_ALARM_SNOOZE"
        const val SNOOZE_MINUTES = 5
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SNOOZE) return
        Log.d(TAG, "Snooze not supported in Phase 1 — ignoring")
        // TODO: Phase 2+ — تأجيل 5 دقائق وتحديث الحالة في قاعدة البيانات
    }
}

