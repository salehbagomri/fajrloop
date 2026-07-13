package com.bagomri.fajrloop.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity

/**
 * BaseActivity — النشاط الأساسي لجميع الشاشات لتهيئة خط SF Vision بحجم أكبر بـ 30% عالمياً
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        // تكبير الخط بنسبة 30% ليتلائم مع تصميم وحجم خط SF Vision المضغوط
        config.fontScale = 1.30f
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}
