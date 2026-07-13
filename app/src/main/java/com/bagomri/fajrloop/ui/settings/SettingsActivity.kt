package com.bagomri.fajrloop.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.alarm.AlarmScheduler
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.databinding.ActivitySettingsBinding
import com.bagomri.fajrloop.ui.auth.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences

    private val locationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
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
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(com.bagomri.fajrloop.alarm.AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)

        // تهيئة المظهر واللون الأحمر الزجاجي لزر تسجيل الخروج
        binding.btnLogout.setCustomBgAndBorder(
            Color.argb(26, 0xE7, 0x4C, 0x3C), // خلفية حمراء 10% شفافية
            Color.argb(77, 0xE7, 0x4C, 0x3C)  // حدود حمراء 30% شفافية
        )

        setupListeners()
        loadSavedSettings()
    }

    override fun onResume() {
        super.onResume()
        // تحديث حالة وضع السفر في حال عاد المستخدم من الشاشة الخاصة به
        updateTravelModeStatus()
    }

    private fun loadSavedSettings() {
        // 1. وضع السفر
        updateTravelModeStatus()

        // 2. الموقع
        val savedCity = prefs.getString("user_city", "مكة المكرمة")
        binding.textLocationStatus.text = "المدينة: $savedCity 📍 (اضغط للتحديث عبر الـ GPS)"

        // 3. طريقة الحساب
        binding.textCalcMethodValue.text = prefs.getString(
            "prayer_calc_method",
            "جامعة أم القرى (مكة المكرمة)"
        )

        // 4. توقيت المنبه
        binding.textAlarmTimingValue.text = prefs.getString(
            "alarm_timing_desc",
            "مع أذان الفجر بالضبط 🕌"
        )

        // 5. التحدي المفضل
        val challengeType = prefs.getString("challenge_type", "math")
        val challengeDiff = prefs.getString("challenge_difficulty", "medium")
        updateChallengeDescText(challengeType, challengeDiff)

        // 6. خيارات إضافية (Switches)
        binding.switchVibrate.isChecked = prefs.getBoolean("vibrate_on_alarm", true)
        binding.switchMorningAdhkar.isChecked = prefs.getBoolean("show_adhkar_after_alarm", true)
        binding.switchDailyDua.isChecked = prefs.getBoolean("daily_dua_notification", true)
    }

    private fun updateTravelModeStatus() {
        val isTravelEnabled = prefs.getBoolean("travel_mode_enabled", false)
        if (isTravelEnabled) {
            val travelUntil = prefs.getString("travel_mode_until", "حتى الإلغاء اليدوي")
            binding.textTravelModeStatus.text = "نشط حالياً ✈️ (حتى: $travelUntil)"
            binding.textTravelModeStatus.setTextColor(Color.parseColor("#3498DB")) // أزرق نشط
        } else {
            binding.textTravelModeStatus.text = "غير نشط حالياً"
            binding.textTravelModeStatus.setTextColor(Color.parseColor("#B0B0C5"))
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
        binding.textChallengeValue.text = "$typeStr - $diffStr"
    }

    private fun setupListeners() {
        // زر الرجوع
        binding.btnBack.setOnClickListener { finish() }

        // وضع السفر
        binding.rowTravelMode.setOnClickListener {
            startActivity(Intent(this, TravelModeActivity::class.java))
        }

        // كود الاستعادة الاحتياطي (TOTP)
        binding.rowBackupCode.setOnClickListener {
            startActivity(Intent(this, BackupCodeActivity::class.java))
        }

        // إدارة حلقاتي
        binding.rowManageHalqas.setOnClickListener {
            showToast("إدارة وتعدد الحلقات — قريباً في الإصدار السحابي المحدث")
        }

        // الموقع وتحديث الـ GPS
        binding.rowLocation.setOnClickListener {
            checkLocationPermissionsAndFetch()
        }

        // طريقة الحساب
        binding.rowCalcMethod.setOnClickListener {
            showCalcMethodDialog()
        }

        // توقيت المنبه
        binding.rowAlarmTiming.setOnClickListener {
            showAlarmTimingDialog()
        }

        // التحدي المفضل
        binding.rowChallenge.setOnClickListener {
            showChallengeSettingsDialog()
        }

        // التشغيل التلقائي (Auto-Start)
        binding.rowAutostart.setOnClickListener {
            openAutoStartSettings()
        }

        // إعدادات البطارية
        binding.rowBattery.setOnClickListener {
            openBatteryOptimizationSettings()
        }

        // Switches
        binding.switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibrate_on_alarm", isChecked).apply()
        }
        binding.switchMorningAdhkar.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_adhkar_after_alarm", isChecked).apply()
        }
        binding.switchDailyDua.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("daily_dua_notification", isChecked).apply()
        }

        // اختبار المنبه
        binding.rowTestAlarm.setOnClickListener {
            scheduleTestAlarm()
        }

        // دليل الاستخدام
        binding.rowGuide.setOnClickListener {
            startActivity(Intent(this, GuideActivity::class.java))
        }

        // تسجيل الخروج
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showCalcMethodDialog() {
        val methods = arrayOf(
            "جامعة أم القرى (مكة المكرمة)",
            "رابطة العالم الإسلامي",
            "الهيئة المصرية العامة للمساحة",
            "جامعة العلوم الإسلامية بكراتشي",
            "الجمعية الإسلامية لأمريكا الشمالية (ISNA)"
        )
        val currentMethod = prefs.getString("prayer_calc_method", methods[0])

        val dialog = BottomSheetDialog(this, R.style.DarkBottomSheetTheme)
        val view = layoutInflater.inflate(R.layout.dialog_settings_calc_method, null)
        dialog.setContentView(view)

        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)

        val btnUmm = view.findViewById<View>(R.id.btn_method_um_al_qura)
        val btnLeague = view.findViewById<View>(R.id.btn_method_muslim_league)
        val btnEgypt = view.findViewById<View>(R.id.btn_method_egypt)
        val btnKarachi = view.findViewById<View>(R.id.btn_method_karachi)
        val btnIsna = view.findViewById<View>(R.id.btn_method_isna)

        val checks = mapOf(
            methods[0] to view.findViewById<View>(R.id.check_um_al_qura),
            methods[1] to view.findViewById<View>(R.id.check_muslim_league),
            methods[2] to view.findViewById<View>(R.id.check_egypt),
            methods[3] to view.findViewById<View>(R.id.check_karachi),
            methods[4] to view.findViewById<View>(R.id.check_isna)
        )

        checks[currentMethod]?.visibility = View.VISIBLE
        when(currentMethod) {
            methods[0] -> btnUmm.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
            methods[1] -> btnLeague.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
            methods[2] -> btnEgypt.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
            methods[3] -> btnKarachi.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
            methods[4] -> btnIsna.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
        }

        val selectMethod = { selected: String ->
            prefs.edit().putString("prayer_calc_method", selected).apply()
            binding.textCalcMethodValue.text = selected
            showToast("تم حفظ طريقة الحساب الجديدة")
            dialog.dismiss()
        }

        btnUmm.setOnClickListener { selectMethod(methods[0]) }
        btnLeague.setOnClickListener { selectMethod(methods[1]) }
        btnEgypt.setOnClickListener { selectMethod(methods[2]) }
        btnKarachi.setOnClickListener { selectMethod(methods[3]) }
        btnIsna.setOnClickListener { selectMethod(methods[4]) }

        view.findViewById<View>(R.id.btn_close_calc).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showAlarmTimingDialog() {
        val currentType = prefs.getString("alarm_timing_type", "with")
        val currentOffset = prefs.getInt("alarm_timing_offset_minutes", 10)

        val dialog = BottomSheetDialog(this, R.style.DarkBottomSheetTheme)
        val view = layoutInflater.inflate(R.layout.dialog_settings_alarm_timing, null)
        dialog.setContentView(view)
        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)

        val btnBefore = view.findViewById<LinearLayout>(R.id.btn_timing_before)
        val btnWith = view.findViewById<LinearLayout>(R.id.btn_timing_with)
        val btnAfter = view.findViewById<LinearLayout>(R.id.btn_timing_after)

        val textBefore = view.findViewById<TextView>(R.id.text_timing_before)
        val textWith = view.findViewById<TextView>(R.id.text_timing_with)
        val textAfter = view.findViewById<TextView>(R.id.text_timing_after)

        val layoutMinutesPicker = view.findViewById<LinearLayout>(R.id.layout_minutes_picker)
        val textWithInfo = view.findViewById<TextView>(R.id.text_timing_with_info)
        val textOffsetSummary = view.findViewById<TextView>(R.id.text_offset_summary)
        val seekMinutes = view.findViewById<SeekBar>(R.id.seek_minutes)

        var selectedType = currentType
        var selectedOffset = currentOffset

        fun updateUI() {
            btnBefore.setBackgroundResource(R.drawable.bg_code_container)
            btnWith.setBackgroundResource(R.drawable.bg_code_container)
            btnAfter.setBackgroundResource(R.drawable.bg_code_container)

            textBefore.setTextColor(Color.parseColor("#B0B0C5"))
            textWith.setTextColor(Color.parseColor("#B0B0C5"))
            textAfter.setTextColor(Color.parseColor("#B0B0C5"))

            when (selectedType) {
                "before" -> {
                    btnBefore.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textBefore.setTextColor(Color.parseColor("#FFD700"))
                    layoutMinutesPicker.visibility = View.VISIBLE
                    textWithInfo.visibility = View.GONE
                    textOffsetSummary.text = "قبل الأذان بـ $selectedOffset دقيقة ⏰"
                }
                "with" -> {
                    btnWith.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textWith.setTextColor(Color.parseColor("#FFD700"))
                    layoutMinutesPicker.visibility = View.GONE
                    textWithInfo.visibility = View.VISIBLE
                }
                "after" -> {
                    btnAfter.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textAfter.setTextColor(Color.parseColor("#FFD700"))
                    layoutMinutesPicker.visibility = View.VISIBLE
                    textWithInfo.visibility = View.GONE
                    textOffsetSummary.text = "بعد الأذان بـ $selectedOffset دقيقة ⏱️"
                }
            }
        }

        seekMinutes.progress = selectedOffset - 1
        seekMinutes.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedOffset = progress + 1
                if (selectedType == "with") selectedType = "before"
                updateUI()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnBefore.setOnClickListener { selectedType = "before"; updateUI() }
        btnWith.setOnClickListener { selectedType = "with"; selectedOffset = 0; updateUI() }
        btnAfter.setOnClickListener { selectedType = "after"; updateUI() }

        val quickClicks = mapOf(
            R.id.btn_quick_5 to 5,
            R.id.btn_quick_10 to 10,
            R.id.btn_quick_15 to 15,
            R.id.btn_quick_30 to 30,
            R.id.btn_quick_45 to 45
        )
        quickClicks.forEach { (id, valMin) ->
            view.findViewById<View>(id).setOnClickListener {
                selectedOffset = valMin
                seekMinutes.progress = selectedOffset - 1
                if (selectedType == "with") selectedType = "before"
                updateUI()
            }
        }

        updateUI()

        view.findViewById<View>(R.id.btn_save_alarm_timing).setOnClickListener {
            val desc = when (selectedType) {
                "before" -> "قبل الأذان بـ $selectedOffset دقيقة ⏰"
                "after" -> "بعد الأذان بـ $selectedOffset دقيقة ⏱️"
                else -> "مع أذان الفجر بالضبط 🕌"
            }
            prefs.edit()
                .putString("alarm_timing_type", selectedType)
                .putInt("alarm_timing_offset_minutes", selectedOffset)
                .putString("alarm_timing_desc", desc)
                .apply()

            binding.textAlarmTimingValue.text = desc
            showToast("تم تحديث توقيت المنبه بنجاح")
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btn_close_timing).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showChallengeSettingsDialog() {
        val currentType = prefs.getString("challenge_type", "math")
        val currentDiff = prefs.getString("challenge_difficulty", "medium")

        val dialog = BottomSheetDialog(this, R.style.DarkBottomSheetTheme)
        val view = layoutInflater.inflate(R.layout.dialog_settings_challenge, null)
        dialog.setContentView(view)
        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundColor(Color.TRANSPARENT)

        val btnMath = view.findViewById<LinearLayout>(R.id.btn_type_math)
        val btnWord = view.findViewById<LinearLayout>(R.id.btn_type_word)
        val btnShake = view.findViewById<LinearLayout>(R.id.btn_type_shake)

        val textMath = view.findViewById<TextView>(R.id.text_type_math)
        val textWord = view.findViewById<TextView>(R.id.text_type_word)
        val textShake = view.findViewById<TextView>(R.id.text_type_shake)

        val btnEasy = view.findViewById<LinearLayout>(R.id.btn_diff_easy)
        val btnMedium = view.findViewById<LinearLayout>(R.id.btn_diff_medium)
        val btnHard = view.findViewById<LinearLayout>(R.id.btn_diff_hard)

        val textEasy = view.findViewById<TextView>(R.id.text_diff_easy)
        val textMedium = view.findViewById<TextView>(R.id.text_diff_medium)
        val textHard = view.findViewById<TextView>(R.id.text_diff_hard)

        var selectedType = currentType
        var selectedDiff = currentDiff

        fun updateUI() {
            btnMath.setBackgroundResource(R.drawable.bg_code_container)
            btnWord.setBackgroundResource(R.drawable.bg_code_container)
            btnShake.setBackgroundResource(R.drawable.bg_code_container)

            textMath.setTextColor(Color.parseColor("#B0B0C5"))
            textWord.setTextColor(Color.parseColor("#B0B0C5"))
            textShake.setTextColor(Color.parseColor("#B0B0C5"))

            when (selectedType) {
                "math" -> {
                    btnMath.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textMath.setTextColor(Color.parseColor("#FFD700"))
                }
                "word" -> {
                    btnWord.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textWord.setTextColor(Color.parseColor("#FFD700"))
                }
                "shake" -> {
                    btnShake.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textShake.setTextColor(Color.parseColor("#FFD700"))
                }
            }

            btnEasy.setBackgroundResource(R.drawable.bg_code_container)
            btnMedium.setBackgroundResource(R.drawable.bg_code_container)
            btnHard.setBackgroundResource(R.drawable.bg_code_container)

            textEasy.setTextColor(Color.parseColor("#B0B0C5"))
            textMedium.setTextColor(Color.parseColor("#B0B0C5"))
            textHard.setTextColor(Color.parseColor("#B0B0C5"))

            when (selectedDiff) {
                "easy" -> {
                    btnEasy.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textEasy.setTextColor(Color.parseColor("#FFD700"))
                }
                "medium" -> {
                    btnMedium.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textMedium.setTextColor(Color.parseColor("#FFD700"))
                }
                "hard" -> {
                    btnHard.setBackgroundResource(R.drawable.bg_chat_bubble_motivational)
                    textHard.setTextColor(Color.parseColor("#FFD700"))
                }
            }
        }

        btnMath.setOnClickListener { selectedType = "math"; updateUI() }
        btnWord.setOnClickListener { selectedType = "word"; updateUI() }
        btnShake.setOnClickListener { selectedType = "shake"; updateUI() }

        btnEasy.setOnClickListener { selectedDiff = "easy"; updateUI() }
        btnMedium.setOnClickListener { selectedDiff = "medium"; updateUI() }
        btnHard.setOnClickListener { selectedDiff = "hard"; updateUI() }

        updateUI()

        view.findViewById<View>(R.id.btn_save_challenge).setOnClickListener {
            prefs.edit()
                .putString("challenge_type", selectedType)
                .putString("challenge_difficulty", selectedDiff)
                .apply()

            updateChallengeDescText(selectedType, selectedDiff)
            showToast("تم تحديث تحدي الاستيقاظ المفضل")
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.btn_close_challenge).setOnClickListener { dialog.dismiss() }
        dialog.show()
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
                showToast("يرجى الانتقال لإعدادات البطارية يدوياً واستثناء حلقة الفجر")
            }
        } else {
            showToast("جهازك لا يتطلب إعدادات بطارية متقدمة")
        }
    }

    private fun scheduleTestAlarm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showToast("⚠️ يجب منح صلاحية المنبه الدقيق أولاً")
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }
        val triggerAt = System.currentTimeMillis() + 60000L // دقيقة واحدة
        val label = "اختبار المنبه الفوري"
        AlarmScheduler.scheduleAlarm(this, triggerAt, label)

        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(triggerAt))
        showToast("تمت جدولة منبه تجريبي بنجاح عند: $format")
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("تسجيل الخروج")
            .setMessage("هل أنت متأكد من رغبتك في تسجيل الخروج؟")
            .setPositiveButton("تسجيل خروج") { _, _ ->
                AuthManager.signOut()
                // العودة لشاشة الدخول وتصفير الـ stack
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun checkLocationPermissionsAndFetch() {
        val finePermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarsePermission = android.Manifest.permission.ACCESS_COARSE_LOCATION
        val fineGranted = androidx.core.content.ContextCompat.checkSelfPermission(this, finePermission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = androidx.core.content.ContextCompat.checkSelfPermission(this, coarsePermission) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(arrayOf(finePermission, coarsePermission))
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

        binding.textLocationStatus.text = "جارٍ تحديد موقعك الجغرافي الحقيقي... 📡"

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
                    prefs.edit().putString("user_city", city).apply()
                    binding.textLocationStatus.text = "المدينة: $city 📍 (تم التحديث عبر الـ GPS)"
                    showToast("تم تحديد موقعك الحقيقي بنجاح: $city 🎉")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val simplifiedLoc = String.format(Locale.US, "%.3f, %.3f", lat, lng)
                    prefs.edit().putString("user_city", simplifiedLoc).apply()
                    binding.textLocationStatus.text = "الموقع: $simplifiedLoc 📍 (تم التحديث عبر الـ GPS)"
                    showToast("تم تحديد الإحداثيات بنجاح! 📍")
                }
            }
        }.start()
    }

    private fun translateCityToArabic(englishName: String): String {
        val translations = mapOf(
            // السعودية
            "makkah" to "مكة المكرمة",
            "mecca" to "مكة المكرمة",
            "riyadh" to "الرياض",
            "jeddah" to "جدة",
            "dammam" to "الدمام",
            "khobar" to "الخبر",
            "madinah" to "المدينة المنورة",
            "medina" to "المدينة المنورة",
            "abha" to "أبها",
            "taif" to "الطائف",
            "tabuk" to "تبوك",
            "jizan" to "جيزان",
            "najran" to "نجران",
            "baha" to "الباحة",
            "hail" to "حائل",
            "qassim" to "القصيم",
            "buraidah" to "بريدة",
            "unaizah" to "عنيزة",
            "jubail" to "الجبيل",
            "yanbu" to "ينبع",
            "ahsa" to "الأحساء",
            "hofuf" to "الهفوف",
            "qatif" to "القطيف",

            // اليمن
            "sanaa" to "صنعاء",
            "sana" to "صنعاء",
            "aden" to "عدن",
            "taiz" to "تعز",
            "taizz" to "تعز",
            "alhudaydah" to "الحديدة",
            "hodeidah" to "الحديدة",
            "hodeida" to "الحديدة",
            "hudaydah" to "الحديدة",
            "almukalla" to "المكلا",
            "mukalla" to "المكلا",
            "ibb" to "إب",
            "dhamar" to "ذمار",
            "amran" to "عمران",
            "sayyan" to "سيان",
            "zabid" to "زبيد",
            "shibam" to "شبام",
            "marib" to "مأرب",
            "albayda" to "البيضاء",
            "bayda" to "البيضاء",
            "hajjah" to "حجة",
            "sadah" to "صعدة",
            "lahij" to "لحج",
            "lahj" to "لحج",
            "abyan" to "أبين",
            "shabwah" to "شبوة",
            "shabwa" to "شبوة",
            "almahrah" to "المهرة",
            "mahra" to "المهرة",
            "socotra" to "سقطرى",
            "hadramaut" to "حضرموت",
            "hadhramaut" to "حضرموت",
            "hadramout" to "حضرموت",
            "seiyun" to "سيئون",
            "sayun" to "سيئون",
            "tarim" to "تريم"
        )
        // تطبيع النص بالكامل بإزالة الكلمات التوضيحية (مثل Governorate, Province, City) والرموز والمسافات
        val normalized = englishName.lowercase()
            .replace("governorate", "")
            .replace("province", "")
            .replace("city", "")
            .replace("'", "")
            .replace("`", "")
            .replace(" ", "")
            .replace("-", "")
            .trim()

        return translations[normalized] ?: translations[englishName.lowercase().trim()] ?: englishName
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
