package com.bagomri.fajrloop.ui.permissions

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.core.app.NotificationManagerCompat
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.main.MainActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * PermissionSetupActivity — شاشة فحص وطلب الصلاحيات الإلزامية (Jetpack Compose)
 */
class PermissionSetupActivity : BaseActivity() {

    private val permissionsStateFlow = MutableStateFlow<List<PermissionItemData>>(emptyList())
    private val allGrantedStateFlow = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FajrLoopTheme {
                val permissions = permissionsStateFlow.value
                val allGranted = allGrantedStateFlow.value

                PermissionScreen(
                    permissions = permissions,
                    allGranted = allGranted,
                    onDoneClick = {
                        startActivity(Intent(this@PermissionSetupActivity, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        // 1. إشعارات (Android 13+)
        val notifGranted = NotificationManagerCompat.from(this).areNotificationsEnabled()

        // 2. منبه دقيق (Android 12+)
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else true

        // 3. تجاهل تحسين البطارية
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val batteryGranted = pm.isIgnoringBatteryOptimizations(packageName)

        // 4. Full Screen Intent (Android 14+)
        val fullScreenGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(android.app.NotificationManager::class.java)
            nm.canUseFullScreenIntent()
        } else true

        // 5. الظهور فوق التطبيقات الأخرى (System Alert Window)
        val overlaysGranted = Settings.canDrawOverlays(this)

        val allGranted = notifGranted && exactAlarmGranted && batteryGranted && fullScreenGranted && overlaysGranted
        allGrantedStateFlow.value = allGranted

        permissionsStateFlow.value = listOf(
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
    }
}
