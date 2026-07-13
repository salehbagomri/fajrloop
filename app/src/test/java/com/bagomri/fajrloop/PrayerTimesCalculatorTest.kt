package com.bagomri.fajrloop

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class PrayerTimesCalculatorTest {

    @Test
    fun testFajrTimeForMakkah() {
        // coordinates of Makkah
        val coordinates = Coordinates(21.4225, 39.8262)
        val dateComponents = DateComponents(2026, 7, 13)
        val params = CalculationMethod.UMM_AL_QURA.parameters
        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        val fajrTime = prayerTimes.fajr
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh")).apply {
            time = fajrTime
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Verify that Fajr in Makkah is between 3:30 AM and 5:30 AM
        assertTrue("Fajr hour is $hour which is out of range", hour in 3..5)
    }

    @Test
    fun testFajrTimeForRiyadh() {
        val dateComponents = DateComponents(2026, 7, 13)
        val params = CalculationMethod.UMM_AL_QURA.parameters

        // Makkah
        val makkahTimes = PrayerTimes(Coordinates(21.4225, 39.8262), dateComponents, params)
        // Riyadh
        val riyadhTimes = PrayerTimes(Coordinates(24.7136, 46.6753), dateComponents, params)

        // Riyadh is east of Makkah, so its Fajr time should be earlier than Makkah
        assertTrue(riyadhTimes.fajr.before(makkahTimes.fajr))
    }

    @Test
    fun testDifferentCalcMethods() {
        val coordinates = Coordinates(21.4225, 39.8262)
        val dateComponents = DateComponents(2026, 7, 13)

        val ummAlQuraTimes = PrayerTimes(coordinates, dateComponents, CalculationMethod.UMM_AL_QURA.parameters)
        val mwlTimes = PrayerTimes(coordinates, dateComponents, CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters)

        // Different calculation parameters should result in slightly different times
        assertNotEquals(ummAlQuraTimes.fajr.time, mwlTimes.fajr.time)
    }
}
