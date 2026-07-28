package com.bagomri.fajrloop.ui.stats

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.google.firebase.auth.FirebaseAuth

/**
 * StatsActivity — شاشة إحصائيات التزام الفجر ولوحة الصدارة والأوسمة (Jetpack Compose)
 */
class StatsActivity : BaseActivity() {

    private val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        setContent {
            FajrLoopTheme {
                val state by viewModel.uiState.collectAsState()

                StatsScreen(
                    state = state,
                    currentUid = currentUid,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
