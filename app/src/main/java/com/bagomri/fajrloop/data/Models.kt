package com.bagomri.fajrloop.data

/**
 * HalqaMember — نموذج بيانات عضو الحلقة
 */
data class HalqaMember(
    val userId: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val role: String = "member", // admin, member
    val position: Int = 0,
    val responsibleForUserId: String = "",
    val status: String = "active",
    val joinedAt: String = ""
)

/**
 * DailyRecord — نموذج بيانات السجل اليومي للاستيقاظ
 */
data class DailyRecord(
    val uid: String = "",
    val halqaId: String = "",
    val date: String = "", // yyyy-MM-dd
    val status: String = "pending", // pending, ringing, challenge_done, awake, panic, missed, travel
    val alarmTime: String = "", // ISO8601
    val challengeDoneAt: String = "", // ISO8601
    val confirmedBy: String = ""
)

/**
 * AlarmConfig — نموذج إعدادات المنبه المحلي
 */
data class AlarmConfig(
    val triggerTimeMillis: Long = -1L,
    val label: String = "صلاة الفجر",
    val challengeType: String = "math", // math, shake, word
    val challengeDifficulty: String = "medium", // easy, medium, hard
    val enabled: Boolean = false
)

/**
 * PrayerTimes — نموذج مواقيت الصلاة المحسوبة
 */
data class PrayerTimes(
    val fajr: Long = 0L,
    val sunrise: Long = 0L,
    val dhuhr: Long = 0L,
    val asr: Long = 0L,
    val maghrib: Long = 0L,
    val isha: Long = 0L,
    val date: String = "",
    val method: String = "",
    val cityName: String = ""
)

/**
 * UserLocation — نموذج إحداثيات موقع المستخدم الجغرافي
 */
data class UserLocation(
    val latitude: Double = 14.5425,
    val longitude: Double = 49.1242,
    val cityName: String = "المكلا"
)

/**
 * UserSettings — نموذج إعدادات مستخدم حلقة الفجر
 */
data class UserSettings(
    val prayerCalcMethod: String = "umm_al_qura",
    val alarmMinutesBefore: Int = 0,
    val challengeType: String = "math",
    val challengeDifficulty: String = "medium",
    val alarmSound: String = "default",
    val vibration: Boolean = true,
    val travelMode: Boolean = false,
    val travelModeExpiry: String = "",
    val showMorningAdhkar: Boolean = true,
    val showDailyDua: Boolean = true
)

/**
 * UserProfile — نموذج ملف تعريف المستخدم الكامل
 */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val currentHalqaId: String = "",
    val timezone: String = "",
    val location: UserLocation = UserLocation(),
    val settings: UserSettings = UserSettings()
)
