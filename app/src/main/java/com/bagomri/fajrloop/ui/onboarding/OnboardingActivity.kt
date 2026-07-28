package com.bagomri.fajrloop.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.auth.LoginActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme

/**
 * OnboardingActivity — شاشات الترحيب والتعريف بالتطبيق عند أول تشغيل (Jetpack Compose)
 */
class OnboardingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FajrLoopTheme {
                OnboardingScreen(
                    onComplete = { completeOnboarding() }
                )
            }
        }
    }

    private fun completeOnboarding() {
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

