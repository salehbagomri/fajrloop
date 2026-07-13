package com.bagomri.fajrloop.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.bagomri.fajrloop.R

/**
 * BaseActivity — النشاط الأساسي لجميع الشاشات.
 *
 * 🎨 نظام التصميم المركزي — لتغيير حجم الخط عالمياً:
 *   افتح: res/values/design_tokens.xml
 *   عدّل: <item name="font_scale_factor" format="fraction" type="fraction">100%</item>
 *
 *   100% = الحجم الافتراضي | 110% = تكبير 10% | 130% = تكبير 30%
 *   لا حاجة لتعديل أي ملف آخر.
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // قراءة مضاعف حجم الخط من design_tokens.xml بطريقة آمنة
        val scale = try {
            // getFraction(base, pbase): 100% مع base=1 يُعيد 1.0f
            newBase.resources.getFraction(R.fraction.font_scale_factor, 1, 1)
        } catch (e: Exception) {
            1.0f  // fallback آمن عند أي خطأ
        }

        if (scale == 1.0f) {
            // لا حاجة لإعادة تهيئة السياق إذا كان المقياس افتراضياً
            super.attachBaseContext(newBase)
        } else {
            val config = Configuration(newBase.resources.configuration)
            config.fontScale = scale
            val context = newBase.createConfigurationContext(config)
            super.attachBaseContext(context)
        }
    }
}
