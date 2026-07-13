package com.bagomri.fajrloop.ui.permissions

import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import com.bagomri.fajrloop.ui.BaseActivity
import androidx.core.app.NotificationManagerCompat
import com.bagomri.fajrloop.databinding.ActivityPermissionSetupBinding

/**
 * PermissionSetupActivity — شاشة فحص وطلب الصلاحيات الإلزامية
 *
 * تفحص الصلاحيات التالية وتوجّه المستخدم لمنحها:
 * 1. POST_NOTIFICATIONS (Android 13+)
 * 2. SCHEDULE_EXACT_ALARM (Android 12+)
 * 3. REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
 * 4. USE_FULL_SCREEN_INTENT (Android 14+)
 */
class PermissionSetupActivity : BaseActivity() {

    private lateinit var binding: ActivityPermissionSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissionRows()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun setupPermissionRows() {
        binding.apply {
            rowNotifications.setTitle("إشعارات التطبيق")
            rowNotifications.setDescription("لعرض إشعار المنبه على شاشة القفل (Android 13+)")

            rowExactAlarm.setTitle("المنبه الدقيق")
            rowExactAlarm.setDescription("لضمان رنين المنبه في الوقت المحدد بدقة الثانية")

            rowBattery.setTitle("تجاهل تحسين البطارية")
            rowBattery.setDescription("لحماية خدمة الرنين من القتل في الخلفية")

            rowFullScreen.setTitle("الظهور فوق قفل الشاشة")
            rowFullScreen.setDescription("لفتح شاشة المنبه تلقائياً حتى لو كان الهاتف مقفلاً")
        }
    }

    private fun updatePermissionStatus() {
        binding.apply {
            // 1. إشعارات (Android 13+)
            val notifGranted = NotificationManagerCompat.from(this@PermissionSetupActivity)
                .areNotificationsEnabled()
            rowNotifications.setStatus(notifGranted)

            // 2. منبه دقيق (Android 12+)
            val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
            } else true
            rowExactAlarm.setStatus(exactAlarmGranted)

            // 3. تجاهل تحسين البطارية
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            val batteryGranted = pm.isIgnoringBatteryOptimizations(packageName)
            rowBattery.setStatus(batteryGranted)

            // 4. Full Screen Intent (Android 14+)
            val fullScreenGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val nm = getSystemService(android.app.NotificationManager::class.java)
                nm.canUseFullScreenIntent()
            } else true
            rowFullScreen.setStatus(fullScreenGranted)

            // عرض زر "الانتهاء" فقط إذا كل الصلاحيات ممنوحة
            btnDone.visibility = if (notifGranted && exactAlarmGranted && batteryGranted && fullScreenGranted) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            rowNotifications.setOnActionClick {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    })
                }
            }

            rowExactAlarm.setOnActionClick {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    })
                }
            }

            rowBattery.setOnActionClick {
                startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                })
            }

            rowFullScreen.setOnActionClick {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.parse("package:$packageName")
                    })
                }
            }

            btnDone.setOnClickListener {
                startActivity(Intent(this@PermissionSetupActivity, com.bagomri.fajrloop.ui.main.MainActivity::class.java))
                finish()
            }
        }
    }
}

