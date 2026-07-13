package com.bagomri.fajrloop.data

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object PrayerTimesCalculator {

    fun calculate(
        latitude: Double,
        longitude: Double,
        date: Date,
        methodName: String,
        timeZoneId: String = TimeZone.getDefault().id
    ): com.bagomri.fajrloop.data.PrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val method = mapMethodName(methodName)
        val params = method.parameters

        val calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId)).apply {
            time = date
        }
        val dateComponents = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // 1-indexed month
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)
        val timezone = TimeZone.getTimeZone(timeZoneId)

        val fajrTime = prayerTimes.fajr?.time ?: 0L
        val sunriseTime = prayerTimes.sunrise?.time ?: 0L
        val dhuhrTime = prayerTimes.dhuhr?.time ?: 0L
        val asrTime = prayerTimes.asr?.time ?: 0L
        val maghribTime = prayerTimes.maghrib?.time ?: 0L
        val ishaTime = prayerTimes.isha?.time ?: 0L

        val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).apply {
            timeZone = timezone
        }.format(date)

        return com.bagomri.fajrloop.data.PrayerTimes(
            fajr = fajrTime,
            sunrise = sunriseTime,
            dhuhr = dhuhrTime,
            asr = asrTime,
            maghrib = maghribTime,
            isha = ishaTime,
            date = dateStr,
            method = methodName
        )
    }

    private fun mapMethodName(name: String): CalculationMethod {
        val lower = name.lowercase()
        return when {
            lower.contains("أم القرى") || lower.contains("umm_al_qura") || lower.contains("makkah") -> CalculationMethod.UMM_AL_QURA
            lower.contains("رابطة") || lower.contains("muslim_world_league") || lower.contains("mwl") -> CalculationMethod.MUSLIM_WORLD_LEAGUE
            lower.contains("الشمالية") || lower.contains("isna") || lower.contains("north_america") -> CalculationMethod.NORTH_AMERICA
            lower.contains("المصرية") || lower.contains("egypt") -> CalculationMethod.EGYPTIAN
            lower.contains("كراتشي") || lower.contains("karachi") -> CalculationMethod.KARACHI
            lower.contains("dubai") -> CalculationMethod.DUBAI
            lower.contains("kuwait") -> CalculationMethod.KUWAIT
            lower.contains("qatar") -> CalculationMethod.QATAR
            lower.contains("singapore") -> CalculationMethod.SINGAPORE
            else -> CalculationMethod.UMM_AL_QURA
        }
    }
}
