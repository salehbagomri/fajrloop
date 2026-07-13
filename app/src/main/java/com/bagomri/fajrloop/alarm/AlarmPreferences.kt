package com.bagomri.fajrloop.alarm

/**
 * AlarmPreferences — مفاتيح SharedPreferences لحفظ حالة المنبه
 *
 * يُستخدم لاستعادة المنبه بعد إعادة تشغيل الجهاز (BootReceiver).
 */
object AlarmPreferences {
    const val PREFS_NAME = "fajrloop_alarm_prefs"

    /** وقت رنين المنبه بالميلي ثانية (Unix timestamp) */
    const val KEY_ALARM_TIME_MILLIS = "alarm_time_millis"

    /** تسمية المنبه */
    const val KEY_ALARM_LABEL = "alarm_label"

    /** هل المنبه مُفعّل؟ */
    const val KEY_ALARM_ENABLED = "alarm_enabled"

    /** نوع التحدي (math, shake, word) */
    const val KEY_CHALLENGE_TYPE = "challenge_type"

    /** صعوبة التحدي (easy, medium, hard) */
    const val KEY_CHALLENGE_DIFFICULTY = "challenge_difficulty"
}

