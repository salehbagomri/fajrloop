package com.bagomri.fajrloop.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler
import com.bagomri.fajrloop.data.PrayerTimesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {

    private val prayerTimesRepository = PrayerTimesRepository(application)

    private val _fajrTimeStrFlow = MutableStateFlow<String>("")
    val fajrTimeStrFlow: StateFlow<String> = _fajrTimeStrFlow.asStateFlow()

    private val _sunriseTimeStrFlow = MutableStateFlow<String>("")
    val sunriseTimeStrFlow: StateFlow<String> = _sunriseTimeStrFlow.asStateFlow()

    private val _countdownTextFlow = MutableStateFlow<String>("")
    val countdownTextFlow: StateFlow<String> = _countdownTextFlow.asStateFlow()

    private val _countdownColorFlow = MutableStateFlow<String>("#2ECC71")
    val countdownColorFlow: StateFlow<String> = _countdownColorFlow.asStateFlow()

    private val _countdownCardBorderModeFlow = MutableStateFlow<Int>(0)
    val countdownCardBorderModeFlow: StateFlow<Int> = _countdownCardBorderModeFlow.asStateFlow()

    private var countdownJob: Job? = null

    init {
        startFajrCountdown()
    }

    fun startFajrCountdown() {
        countdownJob?.cancel()

        val now = System.currentTimeMillis()
        var prayerTimes = prayerTimesRepository.getPrayerTimesForDate(Date())
        if (prayerTimes.fajr < now) {
            val tomorrow = Date(now + 86_400_000L)
            prayerTimes = prayerTimesRepository.getPrayerTimesForDate(tomorrow)
        }

        val arLocale = Locale.forLanguageTag("ar")
        val timeFormat = SimpleDateFormat("hh:mm a", arLocale)
        val fajrStr = timeFormat.format(Date(prayerTimes.fajr))
        val sunriseStr = timeFormat.format(Date(prayerTimes.sunrise))
        _fajrTimeStrFlow.value = fajrStr
        _sunriseTimeStrFlow.value = sunriseStr

        FajrAlarmAutoScheduler.scheduleNextFajrAlarm(getApplication())
        FajrAlarmAutoScheduler.startPeriodicRescheduler(getApplication())

        countdownJob = viewModelScope.launch {
            while (isActive) {
                val remaining = prayerTimes.fajr - System.currentTimeMillis()
                if (remaining > 0) {
                    val hours = TimeUnit.MILLISECONDS.toHours(remaining)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
                    _countdownTextFlow.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(remaining)
                    _countdownColorFlow.value = when {
                        totalMinutes >= 60 -> "#2ECC71"
                        totalMinutes in 15..59 -> "#FFD700"
                        else -> "#E74C3C"
                    }

                    _countdownCardBorderModeFlow.value = when {
                        totalMinutes < 5 -> 3
                        totalMinutes < 15 -> 2
                        totalMinutes < 60 -> 1
                        else -> 0
                    }
                } else {
                    _countdownTextFlow.value = "00:00:00"
                    _countdownColorFlow.value = "#E74C3C"
                    _countdownCardBorderModeFlow.value = 2
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
