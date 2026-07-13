package com.bagomri.fajrloop

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.ui.alarm.AlarmRingingActivity

/**
 * FajrLoopApp — فئة التطبيق الرئيسية التي تقوم بمراقبة دورة حياة الأنشطة لمنع الالتفاف على شاشة الرنين
 */
class FajrLoopApp : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().log("FajrLoop App Initialized successfully")
        } catch (e: Exception) {
            Log.e("FajrLoopApp", "Failed to initialize Crashlytics logging", e)
        }
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                // إذا تم فتح أي شاشة أخرى غير شاشة الرنين بينما المنبه لا يزال يرن فعلياً، أعد شاشة الرنين فوراً للمقدمة
                if (activity !is AlarmRingingActivity) {
                    val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                    val isAlarmActive = prefs.getBoolean("alarm_active_ringing", false)
                    if (isAlarmActive) {
                        Log.d("FajrLoopApp", "Alarm is actively ringing. Relaunching AlarmRingingActivity...")
                        val relaunchIntent = Intent(activity, AlarmRingingActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                        activity.startActivity(relaunchIntent)
                    }
                }
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
