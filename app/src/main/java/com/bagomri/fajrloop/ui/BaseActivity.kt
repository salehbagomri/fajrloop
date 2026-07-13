package com.bagomri.fajrloop.ui

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.bagomri.fajrloop.R

/**
 * BaseActivity — النشاط الأساسي لجميع الشاشات.
 *
 * 🎨 نظام التصميم المركزي:
 * لتغيير حجم الخط عالمياً، افتح:
 *   res/values/design_tokens.xml
 * وعدّل قيمة:
 *   <item name="font_scale_factor" format="float" type="dimen">1.0</item>
 *
 * القيمة 1.0 = الحجم الافتراضي، 1.3 = تكبير 30%، إلخ.
 * لا حاجة لتعديل أي ملف آخر.
 */
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        // قراءة مضاعف حجم الخط من ملف design_tokens.xml مباشرة
        val scale = newBase.resources.getDimension(R.dimen.font_scale_factor) /
                    newBase.resources.displayMetrics.density

        val config = Configuration(newBase.resources.configuration)
        config.fontScale = scale
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
}

