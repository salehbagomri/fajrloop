package com.bagomri.fajrloop.alarm

/**
 * AlarmPreferences — المركز الوحيد لجميع مفاتيح SharedPreferences في التطبيق
 */
object AlarmPreferences {
    const val PREFS_NAME = "fajrloop_prefs"

    // مفاتيح المنبه الأساسية
    const val KEY_ALARM_ACTIVE_RINGING = "alarm_active_ringing"
    const val KEY_ALARM_LABEL = "alarm_label"
    const val KEY_ALARM_TRIGGER_TIME = "alarm_trigger_time"
    const val KEY_ALARM_TIME_MILLIS = "alarm_time_millis"
    const val KEY_ALARM_ENABLED = "alarm_enabled"
    const val KEY_CHALLENGE_TYPE = "challenge_type"
    const val KEY_CHALLENGE_DIFFICULTY = "challenge_difficulty"
    const val KEY_ALARM_SOUND_CHOICE = "alarm_sound_choice"
    const val KEY_VIBRATE_ON_ALARM = "vibrate_on_alarm"
    const val KEY_SHOW_ADHKAR_AFTER_ALARM = "show_adhkar_after_alarm"
    const val KEY_DAILY_DUA_NOTIFICATION = "daily_dua_notification"

    // مفاتيح التوقيت والتأخير
    const val KEY_ALARM_TIMING_TYPE = "alarm_timing_type"
    const val KEY_ALARM_TIMING_OFFSET_MINUTES = "alarm_timing_offset_minutes"
    const val KEY_ALARM_TIMING_DESC = "alarm_timing_desc"

    // مفاتيح الحلقة
    const val KEY_CURRENT_HALQA_ID = "current_halqa_id"
    const val KEY_CURRENT_HALQA_NAME = "current_halqa_name"
    const val KEY_HALQA_EARLIEST_FAJR_MILLIS = "halqa_earliest_fajr_millis"
    const val KEY_LAST_SYNCED_FAJR_TIME = "last_synced_fajr_time"

    // مفاتيح الموقع والصلاة
    const val KEY_USER_LATITUDE = "user_latitude_d"
    const val KEY_USER_LONGITUDE = "user_longitude_d"
    const val KEY_USER_LATITUDE_FLOAT = "user_latitude"
    const val KEY_USER_LONGITUDE_FLOAT = "user_longitude"
    const val KEY_USER_CITY = "user_city"
    const val KEY_PRAYER_CALC_METHOD = "prayer_calc_method"

    // مفاتيح وضع السفر
    const val KEY_TRAVEL_MODE_ENABLED = "travel_mode_enabled"
    const val KEY_TRAVEL_MODE_TYPE = "travel_mode_type"
    const val KEY_TRAVEL_MODE_UNTIL = "travel_mode_until"
    const val KEY_TRAVEL_MODE_UNTIL_TIMESTAMP = "travel_mode_until_timestamp"

    // مفاتيح الكاش للمستخدم
    const val KEY_CACHED_USER_DISPLAY_NAME = "cached_user_display_name"
    const val KEY_CACHED_USER_PHOTO_URL = "cached_user_photo_url"
    const val KEY_CACHED_AWAKE_COUNT_TEXT = "cached_awake_count_text"
    const val KEY_CACHED_TODAY_SUMMARY_TEXT = "cached_today_summary_text"

    // مفاتيح إضافية
    const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    const val KEY_LAST_FAJR_SYNC_MILLIS = "last_fajr_sync_millis"
}
