package com.bagomri.fajrloop.ui.main

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.auth.FcmTokenManager
import com.bagomri.fajrloop.ui.auth.LoginViewModel
import com.bagomri.fajrloop.ui.chat.ChatViewModel
import com.bagomri.fajrloop.ui.navigation.FajrLoopNavGraph
import com.bagomri.fajrloop.ui.navigation.Screen
import com.bagomri.fajrloop.ui.permissions.PermissionItemData
import com.bagomri.fajrloop.ui.settings.SettingsViewModel
import com.bagomri.fajrloop.ui.stats.StatsViewModel
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.bagomri.fajrloop.util.OemBatteryHelper
import kotlinx.coroutines.flow.MutableStateFlow

import androidx.activity.enableEdgeToEdge

/**
 * MainActivity — النشاط الرئيسي الوحيد للتطبيق (Single Activity Host with Jetpack Compose & Navigation)
 */
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val prayerTimesViewModel: PrayerTimesViewModel by viewModels()
    private val halqaViewModel: HalqaViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val statsViewModel: StatsViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private val permissionsListState = MutableStateFlow<List<PermissionItemData>>(emptyList())
    private val allPermissionsGrantedState = MutableStateFlow(false)

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                loginViewModel.handleLegacySignInResult(idToken, null)
            } catch (e: ApiException) {
                loginViewModel.handleLegacySignInResult(null, e.statusCode)
            }
        } else {
            loginViewModel.onLegacySignInCancelled()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (AuthManager.isUserSignedIn()) {
            FcmTokenManager.registerToken()
            com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler.scheduleNextFajrAlarm(this)
            com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler.startPeriodicRescheduler(this)
        }

        val startDestination = determineStartDestination()

        setContent {
            FajrLoopTheme {
                val navController = rememberNavController()
                val permissionsList by permissionsListState.collectAsState()
                val allPermissionsGranted by allPermissionsGrantedState.collectAsState()

                FajrLoopNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    onFallbackLegacyLogin = { intent ->
                        googleSignInLauncher.launch(intent)
                    },
                    permissionsList = permissionsList,
                    allPermissionsGranted = allPermissionsGranted,
                    onRefreshPermissions = { updatePermissionStatus() },
                    mainViewModel = mainViewModel,
                    prayerTimesViewModel = prayerTimesViewModel,
                    halqaViewModel = halqaViewModel,
                    settingsViewModel = settingsViewModel,
                    statsViewModel = statsViewModel,
                    loginViewModel = loginViewModel,
                    chatViewModel = chatViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        if (AuthManager.isUserSignedIn()) {
            prayerTimesViewModel.startFajrCountdown()
            com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler.scheduleNextFajrAlarm(this)
            com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler.startPeriodicRescheduler(this)
        }
    }

    private fun determineStartDestination(): String {
        val navigateToExtra = intent?.getStringExtra("navigate_to")
        if (navigateToExtra == "morning_adhkar") {
            return Screen.MorningAdhkar.route
        }

        val onboardingPrefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val onboardingCompleted = onboardingPrefs.getBoolean(AlarmPreferences.KEY_ONBOARDING_COMPLETED, false)

        if (!onboardingCompleted) {
            return Screen.Onboarding.route
        }
        if (!AuthManager.isUserSignedIn()) {
            return Screen.Login.route
        }
        if (!hasAllCriticalPermissions()) {
            return Screen.PermissionSetup.route
        }
        return Screen.Home.route
    }

    private fun updatePermissionStatus() {
        val notifGranted = NotificationManagerCompat.from(this).areNotificationsEnabled()
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else true
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val batteryGranted = pm.isIgnoringBatteryOptimizations(packageName)
        val fullScreenGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.canUseFullScreenIntent()
        } else true
        val overlaysGranted = Settings.canDrawOverlays(this)

        // OEM: نقرأ من SharedPreferences إذا زار المستخدم صفحة إعدادات الماركة
        val oemPrefs = getSharedPreferences("oem_settings", Context.MODE_PRIVATE)
        val oemVisited = oemPrefs.getBoolean("oem_settings_visited", false)

        val allGranted = notifGranted && exactAlarmGranted && batteryGranted && fullScreenGranted && overlaysGranted
        allPermissionsGrantedState.value = allGranted

        val items = mutableListOf(
            PermissionItemData(
                id = "notifications",
                title = "إشعارات التطبيق",
                description = "لعرض إشعار المنبه على شاشة القفل (Android 13+)",
                isGranted = notifGranted,
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        })
                    }
                }
            ),
            PermissionItemData(
                id = "exact_alarm",
                title = "المنبه الدقيق",
                description = "لضمان رنين المنبه في الوقت المحدد بدقة الثانية",
                isGranted = exactAlarmGranted,
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:$packageName")
                        })
                    }
                }
            ),
            PermissionItemData(
                id = "battery",
                title = "تجاهل تحسين البطارية",
                description = "لحماية خدمة الرنين من القتل في الخلفية",
                isGranted = batteryGranted,
                onRequest = {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    })
                }
            ),
            PermissionItemData(
                id = "full_screen",
                title = "الظهور فوق قفل الشاشة",
                description = "لفتح شاشة المنبه تلقائياً حتى لو كان الهاتف مقفلاً",
                isGranted = fullScreenGranted,
                onRequest = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                            data = Uri.parse("package:$packageName")
                        })
                    }
                }
            ),
            PermissionItemData(
                id = "draw_overlays",
                title = "الظهور فوق التطبيقات الأخرى",
                description = "لمنع تجاوز المنبه وإظهار شاشة التحدي أثناء استخدام الهاتف",
                isGranted = overlaysGranted,
                onRequest = {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    })
                }
            )
        )

        // إضافة عنصر OEM فقط للأجهزة التي تحتاجه (Honor/Huawei/Samsung)
        if (OemBatteryHelper.requiresOemSettings()) {
            items.add(
                PermissionItemData(
                    id = "oem_battery",
                    title = OemBatteryHelper.getOemLabel() ?: "إعدادات التشغيل الخلفي",
                    description = OemBatteryHelper.getOemDescription()
                        ?: "فعّل خيارات التشغيل في الخلفية لضمان رنين المنبه",
                    isGranted = oemVisited,
                    onRequest = {
                        // نحفظ أن المستخدم زار الصفحة
                        oemPrefs.edit().putBoolean("oem_settings_visited", true).apply()
                        try {
                            startActivity(OemBatteryHelper.getOemIntent(this))
                        } catch (e: Exception) {
                            startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:$packageName"))
                            )
                        }
                    }
                )
            )
        }

        permissionsListState.value = items
    }

    private fun hasAllCriticalPermissions(): Boolean {
        val notifGranted = NotificationManagerCompat.from(this).areNotificationsEnabled()
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else true
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val batteryGranted = pm.isIgnoringBatteryOptimizations(packageName)
        val fullScreenGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.canUseFullScreenIntent()
        } else true
        val overlaysGranted = Settings.canDrawOverlays(this)

        return notifGranted && exactAlarmGranted && batteryGranted && fullScreenGranted && overlaysGranted
    }
}
