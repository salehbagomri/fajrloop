package com.bagomri.fajrloop.ui.adhkar

import android.os.Bundle
import androidx.activity.compose.setContent
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme

/**
 * MorningAdhkarActivity — شاشة أذكار الصباح التفاعلية بعد تأكيد الاستيقاظ (Jetpack Compose)
 */
class MorningAdhkarActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FajrLoopTheme {
                AdhkarScreen(
                    onFinish = { finish() }
                )
            }
        }
    }
}
