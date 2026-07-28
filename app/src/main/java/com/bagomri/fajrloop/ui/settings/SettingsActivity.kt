package com.bagomri.fajrloop.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.alarm.AlarmScheduler
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.auth.FcmTokenManager
import com.bagomri.fajrloop.data.UserLocation
import com.bagomri.fajrloop.data.UserSettings
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.auth.LoginActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class SettingsActivity : BaseActivity() {

    private lateinit var prefs: SharedPreferences
    private val viewModel: SettingsViewModel by viewModels()

    private val userCityFlow = MutableStateFlow("مكة المكرمة")
    private val calcMethodFlow = MutableStateFlow("جامعة أم القرى (مكة المكرمة)")
    private val alarmTimingDescFlow = MutableStateFlow("مع أذان الفجر بالضبط 🕌")
    private val challengeTextFlow = MutableStateFlow("حل المعادلة - متوسط")
    private val alarmSoundTextFlow = MutableStateFlow("نغمة النظام الافتراضية")
    private val travelModeStatusFlow = MutableStateFlow("غير نشط حالياً")
    private val isVibrateFlow = MutableStateFlow(true)
    private val isAdhkarFlow = MutableStateFlow(true)
    private val isDuaFlow = MutableStateFlow(true)

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            getCurrentLocation()
        } else {
            showToast("⚠️ يجب منح صلاحية الموقع لتحديد مدينتك الحقيقية")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)

        loadSavedSettings()

        setContent {
            FajrLoopTheme {
                val userCity by userCityFlow.collectAsState()
                val calcMethod by calcMethodFlow.collectAsState()
                val alarmTimingDesc by alarmTimingDescFlow.collectAsState()
                val challengeText by challengeTextFlow.collectAsState()
                val alarmSoundText by alarmSoundTextFlow.collectAsState()
                val travelModeStatus by travelModeStatusFlow.collectAsState()
                val isVibrate by isVibrateFlow.collectAsState()
                val isAdhkar by isAdhkarFlow.collectAsState()
                val isDua by isDuaFlow.collectAsState()

                SettingsScreen(
                    userCity = userCity,
                    calcMethod = calcMethod,
                    alarmTimingDesc = alarmTimingDesc,
                    challengeText = challengeText,
                    alarmSoundText = alarmSoundText,
                    travelModeStatus = travelModeStatus,
                    isVibrateEnabled = isVibrate,
                    isAdhkarEnabled = isAdhkar,
                    isDuaEnabled = isDua,
                    onVibrateChange = { checked ->
                        isVibrateFlow.value = checked
                        prefs.edit().putBoolean("vibrate_on_alarm", checked).apply()
                        val currentSettings = viewModel.userProfile.value?.settings ?: UserSettings()
                        viewModel.updateUserSettings(currentSettings.copy(vibration = checked)) {}
                    },
                    onAdhkarChange = { checked ->
                        isAdhkarFlow.value = checked
                        prefs.edit().putBoolean("show_adhkar_after_alarm", checked).apply()
                        val currentSettings = viewModel.userProfile.value?.settings ?: UserSettings()
                        viewModel.updateUserSettings(currentSettings.copy(showMorningAdhkar = checked)) {}
                    },
                    onDuaChange = { checked ->
                        isDuaFlow.value = checked
                        prefs.edit().putBoolean("daily_dua_notification", checked).apply()
                        val currentSettings = viewModel.userProfile.value?.settings ?: UserSettings()
                        viewModel.updateUserSettings(currentSettings.copy(showDailyDua = checked)) {}
                    },
                    onLocationClick = { checkLocationPermissionsAndFetch() },
                    onTravelModeClick = { startActivity(Intent(this, TravelModeActivity::class.java)) },
                    onBackupCodeClick = { startActivity(Intent(this, BackupCodeActivity::class.java)) },
                    onManageHalqasClick = { showToast("إدارة وتعدد الحلقات — قريباً في الإصدار السحابي المحدث") },
                    onSaveCalcMethod = { selected ->
                        calcMethodFlow.value = formatCalcMethodToDisplay(selected)
                        prefs.edit().putString("prayer_calc_method", selected).apply()
                        val lat = prefs.getFloat("user_latitude", 14.5425f).toDouble()
                        val lng = prefs.getFloat("user_longitude", 49.1242f).toDouble()
                        val city = prefs.getString("user_city", "المكلا") ?: "المكلا"
                        viewModel.saveLocalLocationAndMethod(lat, lng, city, selected)
                        val currentSettings = viewModel.userProfile.value?.settings ?: UserSettings()
                        viewModel.updateUserSettings(currentSettings.copy(prayerCalcMethod = mapMethodNameToCode(selected))) {}
                        showToast("تم حفظ طريقة الحساب الجديدة")
                    },
                    onSaveAlarmTiming = { type, offset, desc ->
                        alarmTimingDescFlow.value = desc
                        prefs.edit()
                            .putString("alarm_timing_type", type)
                            .putInt("alarm_timing_offset_minutes", offset)
                            .putString("alarm_timing_desc", desc)
                            .apply()
                        val currentSettings = viewModel.userProfile.value?.settings ?: UserSettings()
                        viewModel.updateUserSettings(currentSettings.copy(alarmMinutesBefore = if (type == "before") -offset else offset)) {}
                        showToast("تم تحديث توقيت المنبه بنجاح")
                    },
                    onSaveChallenge = { type, diff ->
                        prefs.edit()
                            .putString("challenge_type", type)
                            .putString("challenge_difficulty", diff)
                            .apply()
                        val currentSettings = viewModel.userProfile.value?.settings ?: UserSettings()
                        viewModel.updateUserSettings(currentSettings.copy(challengeType = type, challengeDifficulty = diff)) {}
                        updateChallengeDescText(type, diff)
                        showToast("تم تحديث تحدي الاستيقاظ المفضل")
                    },
                    onSaveAlarmSound = { code, title ->
                        alarmSoundTextFlow.value = title
                        prefs.edit().putString("alarm_sound_choice", code).apply()
                        showToast("تم تحديث صوت المنبه بنجاح")
                    },
                    onAutoStartClick = { openAutoStartSettings() },
                    onBatteryClick = { openBatteryOptimizationSettings() },
                    onTestAlarmClick = {
                        AlarmScheduler.scheduleTestAlarm(this, 10)
                        showToast("🧪 تم جدولة منبه تجريبي بعد 10 ثوانٍ! الرجاء قفل الشاشة لاختبار الحماية.")
                    },
                    onGuideClick = { startActivity(Intent(this, GuideActivity::class.java)) },
                    onPrivacyClick = {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://salehbagomri.github.io/fajrloop-privacy/")))
                    },
                    onLogoutClick = { logout() },
                    onBackClick = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateTravelModeStatus()
    }

    private fun loadSavedSettings() {
        updateTravelModeStatus()

        val savedCity = prefs.getString("user_city", "مكة المكرمة") ?: "مكة المكرمة"
        userCityFlow.value = "المدينة: $savedCity 📍"

        calcMethodFlow.value = formatCalcMethodToDisplay(
            prefs.getString("prayer_calc_method", "جامعة أم القرى (مكة المكرمة)")
        )

        alarmTimingDescFlow.value = prefs.getString("alarm_timing_desc", "مع أذان الفجر بالضبط 🕌") ?: "مع أذان الفجر بالضبط 🕌"

        val challengeType = prefs.getString("challenge_type", "math")
        val challengeDiff = prefs.getString("challenge_difficulty", "medium")
        updateChallengeDescText(challengeType, challengeDiff)

        isVibrateFlow.value = prefs.getBoolean("vibrate_on_alarm", true)
        isAdhkarFlow.value = prefs.getBoolean("show_adhkar_after_alarm", true)
        isDuaFlow.value = prefs.getBoolean("daily_dua_notification", true)

        val alarmSoundChoice = prefs.getString("alarm_sound_choice", "default")
        alarmSoundTextFlow.value = mapSoundChoiceToText(alarmSoundChoice)
    }

    private fun mapSoundChoiceToText(choice: String?): String {
        return when (choice) {
            "afasy" -> "الأذان بصوت الشيخ مشاري العفاسي"
            "abdulbasit" -> "الأذان بصوت الشيخ عبدالباسط عبدالصمد"
            "islamic" -> "نغمة إسلامية هادئة"
            else -> "نغمة النظام الافتراضية"
        }
    }

    private fun updateTravelModeStatus() {
        val isTravelEnabled = prefs.getBoolean("travel_mode_enabled", false)
        if (isTravelEnabled) {
            val travelUntil = prefs.getString("travel_mode_until", "حتى الإلغاء اليدوي")
            travelModeStatusFlow.value = "نشط حالياً ✈️ (حتى: $travelUntil)"
        } else {
            travelModeStatusFlow.value = "غير نشط حالياً"
        }
    }

    private fun updateChallengeDescText(type: String?, diff: String?) {
        val typeStr = when (type) {
            "math" -> "حل المعادلة الحسابية"
            "word" -> "اكتب الكلمة التي تظهر"
            "shake" -> "رج الهاتف بقوة"
            else -> "حل المعادلة الحسابية"
        }
        val diffStr = when (diff) {
            "easy" -> "سهل"
            "medium" -> "متوسط"
            "hard" -> "صعب"
            else -> "متوسط"
        }
        challengeTextFlow.value = "$typeStr - $diffStr"
    }

    private fun formatCalcMethodToDisplay(method: String?): String {
        val m = method ?: "umm_al_qura"
        return when {
            m.contains("umm_al_qura") || m.contains("أم القرى") -> "جامعة أم القرى (مكة المكرمة)"
            m.contains("muslim_world_league") || m.contains("رابطة") -> "رابطة العالم الإسلامي"
            m.contains("egypt") || m.contains("المصرية") -> "الهيئة المصرية العامة للمساحة"
            m.contains("karachi") || m.contains("كراتشي") -> "جامعة العلوم الإسلامية بكراتشي"
            m.contains("isna") || m.contains("ISNA") -> "الجمعية الإسلامية لأمريكا الشمالية (ISNA)"
            else -> "جامعة أم القرى (مكة المكرمة)"
        }
    }

    private fun mapMethodNameToCode(name: String): String {
        return when {
            name.contains("أم القرى") || name.contains("umm_al_qura") -> "umm_al_qura"
            name.contains("رابطة") || name.contains("muslim_world_league") -> "muslim_world_league"
            name.contains("المصرية") || name.contains("egypt") -> "egypt"
            name.contains("كراتشي") || name.contains("karachi") -> "karachi"
            name.contains("ISNA") || name.contains("isna") -> "isna"
            else -> "umm_al_qura"
        }
    }

    private fun checkLocationPermissionsAndFetch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasFine = checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val hasCoarse = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasFine || hasCoarse) {
                getCurrentLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            showToast("📍 يرجى تفعيل خدمة الموقع (GPS)")
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        userCityFlow.value = "جارٍ تحديد موقعك الجغرافي الحقيقي... 📡"
        val provider = if (isNetworkEnabled) android.location.LocationManager.NETWORK_PROVIDER else android.location.LocationManager.GPS_PROVIDER

        try {
            val lastKnown = locationManager.getLastKnownLocation(provider)
            if (lastKnown != null) {
                resolveCityName(lastKnown.latitude, lastKnown.longitude)
            } else {
                locationManager.requestSingleUpdate(provider, object : android.location.LocationListener {
                    override fun onLocationChanged(location: android.location.Location) {
                        resolveCityName(location.latitude, location.longitude)
                    }
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }, android.os.Looper.getMainLooper())
            }
        } catch (e: SecurityException) {
            showToast("خطأ في صلاحيات الموقع الجغرافي")
        }
    }

    private fun resolveCityName(lat: Double, lng: Double) {
        Thread {
            try {
                val geocoder = android.location.Geocoder(this, Locale("ar"))
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val rawCity = addresses?.firstOrNull()?.let { addr ->
                    addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "مدينة غير معروفة"
                } ?: "مدينة غير معروفة"
                val city = translateCityToArabic(rawCity)

                runOnUiThread {
                    prefs.edit().putString("user_city", city)
                        .putFloat("user_latitude", lat.toFloat())
                        .putFloat("user_longitude", lng.toFloat())
                        .apply()
                    userCityFlow.value = "المدينة: $city 📍"
                    viewModel.updateUserLocation(UserLocation(latitude = lat, longitude = lng, cityName = city)) {}
                    showToast("تم تحديد موقعك الحقيقي بنجاح: $city 🎉")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val simplifiedLoc = String.format(Locale.US, "%.3f, %.3f", lat, lng)
                    prefs.edit().putString("user_city", simplifiedLoc)
                        .putFloat("user_latitude", lat.toFloat())
                        .putFloat("user_longitude", lng.toFloat())
                        .apply()
                    userCityFlow.value = "الموقع: $simplifiedLoc 📍"
                    viewModel.updateUserLocation(UserLocation(latitude = lat, longitude = lng, cityName = simplifiedLoc)) {}
                    showToast("تم تحديد الإحداثيات بنجاح! 📍")
                }
            }
        }.start()
    }

    private fun translateCityToArabic(englishName: String): String {
        val translations = mapOf(
            "makkah" to "مكة المكرمة",
            "mecca" to "مكة المكرمة",
            "riyadh" to "الرياض",
            "jeddah" to "جدة",
            "dammam" to "الدمام",
            "khobar" to "الخبر",
            "madinah" to "المدينة المنورة",
            "medina" to "المدينة المنورة",
            "sanaa" to "صنعاء",
            "aden" to "عدن",
            "taiz" to "تعز",
            "almukalla" to "المكلا",
            "mukalla" to "المكلا"
        )
        return translations[englishName.lowercase(Locale.ROOT)] ?: englishName
    }

    private fun openAutoStartSettings() {
        try {
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
            showToast("يرجى تفعيل التشغيل التلقائي (Auto-Start) في إعدادات التطبيق")
        } catch (e: Exception) {
            showToast("تعذر فتح الإعدادات تلقائياً")
        }
    }

    private fun openBatteryOptimizationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                showToast("تعذر فتح إعدادات البطارية تلقائياً")
            }
        }
    }

    private fun logout() {
        FcmTokenManager.unregisterToken()
        prefs.edit()
            .remove("current_halqa_id")
            .remove("current_halqa_name")
            .remove("cached_user_display_name")
            .remove("cached_user_photo_url")
            .remove("cached_awake_count_text")
            .remove("cached_today_summary_text")
            .apply()

        AuthManager.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
