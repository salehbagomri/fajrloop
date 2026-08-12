package com.bagomri.fajrloop.alarm

import android.content.Context
import android.util.Log
import com.bagomri.fajrloop.auth.AuthManager
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * TravelModeManager - إدارة وضع السفر الحقيقي (إيقاف وتفعيل المنبه، فحص انتهاء الصلاحية، والمزامنة السحابية)
 */
object TravelModeManager {

    private const val TAG = "TravelModeManager"

    /**
     * التحقق مما إذا كان وضع السفر مفعلاً ونشطاً بالوقت الحالي
     */
    fun isTravelModeActive(context: Context): Boolean {
        val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("travel_mode_enabled", false)
        if (!isEnabled) return false

        val untilTimestamp = prefs.getLong("travel_mode_until_timestamp", Long.MAX_VALUE)
        if (untilTimestamp != Long.MAX_VALUE && System.currentTimeMillis() > untilTimestamp) {
            // انقضت مدة السفر المحجوزة! تعطيل وضع السفر تلقائياً واستعادة المنبه
            Log.d(TAG, "✈️ Travel mode duration expired. Auto-disabling.")
            setTravelMode(
                context = context,
                enabled = false,
                type = "indefinite",
                untilText = "غير نشط حالياً",
                untilTimestamp = 0L
            )
            return false
        }
        return true
    }

    /**
     * الحصول على نص حالة وضع السفر للعرض في شاشة الإعدادات والرئيسية
     */
    fun getTravelModeStatusText(context: Context): String {
        if (!isTravelModeActive(context)) return "غير نشط حالياً"
        val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val untilText = prefs.getString("travel_mode_until", "حتى الإلغاء اليدوي") ?: "حتى الإلغاء اليدوي"
        return "نشط - $untilText"
    }

    /**
     * ضبط وحفظ وضع السفر (تفعيل أو تعطيل، إلغاء/إعادة جدولة المنبه، والمزامنة السحابية اللحظية)
     */
    fun setTravelMode(
        context: Context,
        enabled: Boolean,
        type: String,
        untilText: String,
        untilTimestamp: Long
    ) {
        val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("travel_mode_enabled", enabled)
            .putString("travel_mode_type", type)
            .putString("travel_mode_until", untilText)
            .putLong("travel_mode_until_timestamp", untilTimestamp)
            .apply()

        if (enabled) {
            // 1. إلغاء المنبه المحلي مؤقتاً طوال فترة السفر
            AlarmScheduler.cancelAlarm(context)
            Log.d(TAG, "✈️ Travel mode ENABLED. Local alarm cancelled.")
        } else {
            // 2. إعادة جدولة المنبه المحلي
            FajrAlarmAutoScheduler.scheduleNextFajrAlarm(context)
            Log.d(TAG, "🔔 Travel mode DISABLED. Rescheduled local alarm.")
        }

        // 3. مزامنة حالة السفر اللحظية سحابياً في Firebase (في ملف المستخدم وفي عقدة الحلقة النشطة)
        val uid = AuthManager.getUserId()
        if (uid != null) {
            try {
                val db = FirebaseDatabase.getInstance()
                val userRef = db.getReference("users").child(uid)
                
                userRef.child("settings").child("travelMode").setValue(enabled)
                userRef.child("settings").child("travelModeExpiry").setValue(if (enabled) untilText else "")
                userRef.child("status").setValue(if (enabled) "travel" else "active")

                // المزامنة الحية داخل عقدة اعضاء الحلقة لتظهر فوراً لدى الأصدقاء
                val halqaId = prefs.getString("current_halqa_id", null)
                if (!halqaId.isNullOrEmpty()) {
                    db.getReference("halqas")
                        .child(halqaId)
                        .child("members")
                        .child(uid)
                        .child("status")
                        .setValue(if (enabled) "travel" else "active")
                    Log.d(TAG, "✅ Realtime synced travel status to Halqa: $halqaId")
                } else {
                    userRef.child("currentHalqaId").get().addOnSuccessListener { snap ->
                        val hId = snap.value as? String
                        if (!hId.isNullOrEmpty()) {
                            db.getReference("halqas")
                                .child(hId)
                                .child("members")
                                .child(uid)
                                .child("status")
                                .setValue(if (enabled) "travel" else "active")
                            prefs.edit().putString("current_halqa_id", hId).apply()
                            Log.d(TAG, "✅ Realtime synced travel status to Halqa via user node: $hId")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update Firebase travel mode", e)
            }
        }
    }

    /**
     * حساب وقت الانتهاء بصيغة النص وطابع الوقت الملي ثانية (Timestamp)
     */
    fun calculateUntilTimestamp(type: String, customDateStr: String): Pair<String, Long> {
        val cal = Calendar.getInstance()
        return when (type) {
            "1_day" -> {
                cal.add(Calendar.DAY_OF_MONTH, 1)
                val formatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                Pair("حتى غداً ($formatted)", cal.timeInMillis)
            }
            "3_days" -> {
                cal.add(Calendar.DAY_OF_MONTH, 3)
                val formatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                Pair("حتى $formatted (3 أيام)", cal.timeInMillis)
            }
            "7_days" -> {
                cal.add(Calendar.DAY_OF_MONTH, 7)
                val formatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                Pair("حتى $formatted (7 أيام)", cal.timeInMillis)
            }
            "custom" -> {
                if (customDateStr.isNotEmpty()) {
                    try {
                        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                        val parsedDate = sdf.parse(customDateStr)
                        if (parsedDate != null) {
                            val endCal = Calendar.getInstance().apply {
                                time = parsedDate
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                            }
                            Pair("حتى $customDateStr", endCal.timeInMillis)
                        } else {
                            Pair("حتى الإلغاء اليدوي", Long.MAX_VALUE)
                        }
                    } catch (e: Exception) {
                        Pair("حتى الإلغاء اليدوي", Long.MAX_VALUE)
                    }
                } else {
                    Pair("حتى الإلغاء اليدوي", Long.MAX_VALUE)
                }
            }
            else -> Pair("حتى الإلغاء اليدوي", Long.MAX_VALUE)
        }
    }
}
