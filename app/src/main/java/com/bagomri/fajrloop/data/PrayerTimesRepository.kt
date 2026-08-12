package com.bagomri.fajrloop.data

import android.content.Context
import android.content.SharedPreferences
import com.bagomri.fajrloop.alarm.AlarmPreferences
import java.util.Date
import java.util.TimeZone

class PrayerTimesRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        AlarmPreferences.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * حساب مواقيت الصلاة لتاريخ معين بناءً على إحداثيات موقع المستخدم المحفوظ وطريقة الحساب
     */
    fun getPrayerTimesForDate(date: Date): PrayerTimes {
        val latitude = prefs.getString("user_latitude_d", "14.5425")?.toDoubleOrNull() ?: 14.5425
        val longitude = prefs.getString("user_longitude_d", "49.1242")?.toDoubleOrNull() ?: 49.1242
        val cityName = prefs.getString("user_city", "المكلا") ?: "المكلا"
        val method = prefs.getString("prayer_calc_method", "umm_al_qura") ?: "umm_al_qura"

        val calculated = PrayerTimesCalculator.calculate(
            latitude = latitude,
            longitude = longitude,
            date = date,
            methodName = method,
            timeZoneId = TimeZone.getDefault().id
        )

        return calculated.copy(cityName = cityName)
    }

    /**
     * حفظ تفاصيل الموقع وطريقة الحساب يدوياً أو بعد الحصول عليها من نظام تحديد المواقع
     */
    fun saveLocationAndMethod(latitude: Double, longitude: Double, cityName: String, method: String) {
        prefs.edit().apply {
            putFloat("user_latitude", latitude.toFloat())
            putString("user_latitude_d", latitude.toString())
            putFloat("user_longitude", longitude.toFloat())
            putString("user_longitude_d", longitude.toString())
            putString("user_city", cityName)
            putString("prayer_calc_method", method)
            apply()
        }
    }
}
