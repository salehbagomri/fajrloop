package com.bagomri.fajrloop.ui.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme

class GuideActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FajrLoopTheme {
                GuideScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}
