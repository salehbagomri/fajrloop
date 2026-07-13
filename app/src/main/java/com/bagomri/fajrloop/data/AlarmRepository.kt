package com.bagomri.fajrloop.data

import android.content.Context
import android.content.SharedPreferences
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.alarm.AlarmScheduler

class AlarmRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        AlarmPreferences.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * استرجاع إعدادات المنبه الحالي
     */
    fun getAlarmConfig(): AlarmConfig {
        val triggerTime = prefs.getLong(AlarmPreferences.KEY_ALARM_TIME_MILLIS, -1L)
        val label = prefs.getString(AlarmPreferences.KEY_ALARM_LABEL, "صلاة الفجر") ?: "صلاة الفجر"
        val type = prefs.getString(AlarmPreferences.KEY_CHALLENGE_TYPE, "math") ?: "math"
        val difficulty = prefs.getString(AlarmPreferences.KEY_CHALLENGE_DIFFICULTY, "medium") ?: "medium"
        val enabled = prefs.getBoolean(AlarmPreferences.KEY_ALARM_ENABLED, false)
        return AlarmConfig(triggerTime, label, type, difficulty, enabled)
    }

    /**
     * حفظ وجدولة إعدادات المنبه
     */
    fun saveAlarmConfig(config: AlarmConfig) {
        prefs.edit().apply {
            putLong(AlarmPreferences.KEY_ALARM_TIME_MILLIS, config.triggerTimeMillis)
            putString(AlarmPreferences.KEY_ALARM_LABEL, config.label)
            putString(AlarmPreferences.KEY_CHALLENGE_TYPE, config.challengeType)
            putString(AlarmPreferences.KEY_CHALLENGE_DIFFICULTY, config.challengeDifficulty)
            putBoolean(AlarmPreferences.KEY_ALARM_ENABLED, config.enabled)
            apply()
        }

        if (config.enabled && config.triggerTimeMillis > 0) {
            AlarmScheduler.scheduleAlarm(context, config.triggerTimeMillis, config.label)
        } else {
            AlarmScheduler.cancelAlarm(context)
        }
    }

    /**
     * التحقق مما إذا كان هناك منبه مجدول نشط في النظام
     */
    fun isAlarmScheduled(): Boolean {
        return AlarmScheduler.isAlarmScheduled(context)
    }

    /**
     * إلغاء المنبه النشط
     */
    fun cancelAlarm() {
        prefs.edit().putBoolean(AlarmPreferences.KEY_ALARM_ENABLED, false).apply()
        AlarmScheduler.cancelAlarm(context)
    }
}
