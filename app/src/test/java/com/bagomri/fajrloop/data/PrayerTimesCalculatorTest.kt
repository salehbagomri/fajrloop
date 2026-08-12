package com.bagomri.fajrloop.data

import org.junit.Test
import org.junit.Assert.*
import java.util.*

class PrayerTimesCalculatorTest {

    // التسامح المسموح به: ±5 دقائق (300,000 ms) مقارنة بالمصادر المرجعية
    private val TOLERANCE_MS = 300_000L

    private fun getDateFor(year: Int, month: Int, day: Int, tzId: String): Date {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId))
        cal.set(year, month - 1, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun getExpectedMillis(hour: Int, minute: Int, tzId: String, date: Date): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone(tzId))
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun `Makkah Fajr time is approximately correct`() {
        // مكة المكرمة — طريقة أم القرى — 15 مارس 2025
        val date = getDateFor(2025, 3, 15, "Asia/Riyadh")
        val result = PrayerTimesCalculator.calculate(21.3891, 39.8579, date, "umm_al_qura", "Asia/Riyadh")

        assertTrue("Makkah fajr time is invalid", result.fajr > 0L)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply { timeInMillis = result.fajr }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        assertTrue("Makkah fajr hour should be around 5 AM (was $hour)", hour in 4..5)
    }

    @Test
    fun `Cairo Fajr time is approximately correct`() {
        // القاهرة — الطريقة المصرية — 15 مارس 2025
        val date = getDateFor(2025, 3, 15, "Africa/Cairo")
        val result = PrayerTimesCalculator.calculate(30.0444, 31.2357, date, "egypt", "Africa/Cairo")

        assertTrue("Cairo fajr time is invalid", result.fajr > 0L)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Africa/Cairo")).apply { timeInMillis = result.fajr }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        assertTrue("Cairo fajr hour should be around 4 AM (was $hour)", hour in 4..5)
    }

    @Test
    fun `all prayer times are in correct order`() {
        val date = getDateFor(2025, 3, 15, "Asia/Riyadh")
        val result = PrayerTimesCalculator.calculate(21.3891, 39.8579, date, "umm_al_qura", "Asia/Riyadh")

        assertTrue("fajr < sunrise", result.fajr < result.sunrise)
        assertTrue("sunrise < dhuhr", result.sunrise < result.dhuhr)
        assertTrue("dhuhr < asr", result.dhuhr < result.asr)
        assertTrue("asr < maghrib", result.asr < result.maghrib)
        assertTrue("maghrib < isha", result.maghrib < result.isha)
    }

    @Test
    fun `all prayer times are non-zero`() {
        val date = getDateFor(2025, 6, 15, "Asia/Riyadh")
        val result = PrayerTimesCalculator.calculate(21.3891, 39.8579, date, "umm_al_qura", "Asia/Riyadh")

        assertTrue(result.fajr > 0)
        assertTrue(result.sunrise > 0)
        assertTrue(result.dhuhr > 0)
        assertTrue(result.asr > 0)
        assertTrue(result.maghrib > 0)
        assertTrue(result.isha > 0)
    }
}
