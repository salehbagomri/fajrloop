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
        val latitude = prefs.getString(AlarmPreferences.KEY_USER_LATITUDE, "14.5425")?.toDoubleOrNull() ?: 14.5425
        val longitude = prefs.getString(AlarmPreferences.KEY_USER_LONGITUDE, "49.1242")?.toDoubleOrNull() ?: 49.1242
        val cityName = prefs.getString(AlarmPreferences.KEY_USER_CITY, "المكلا") ?: "المكلا"
        val method = prefs.getString(AlarmPreferences.KEY_PRAYER_CALC_METHOD, "umm_al_qura") ?: "umm_al_qura"

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
            putFloat(AlarmPreferences.KEY_USER_LATITUDE_FLOAT, latitude.toFloat())
            putString(AlarmPreferences.KEY_USER_LATITUDE, latitude.toString())
            putFloat(AlarmPreferences.KEY_USER_LONGITUDE_FLOAT, longitude.toFloat())
            putString(AlarmPreferences.KEY_USER_LONGITUDE, longitude.toString())
            putString(AlarmPreferences.KEY_USER_CITY, cityName)
            putString(AlarmPreferences.KEY_PRAYER_CALC_METHOD, method)
            apply()
        }
    }
}
