package com.bagomri.fajrloop.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class BackupCodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val halqaId = prefs.getString("current_halqa_id", null)
        val isAlarmEnabled = prefs.getBoolean("alarm_enabled", false)

        val formattedCode = if (!halqaId.isNullOrEmpty()) {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val seed = (dateStr + halqaId).hashCode().absoluteValue
            val code = (seed % 900000) + 100000
            "${code / 1000} ${code % 1000}"
        } else ""

        setContent {
            FajrLoopTheme {
                BackupCodeScreen(
                    halqaId = halqaId,
                    totpCode = formattedCode,
                    isAlarmEnabled = isAlarmEnabled,
                    onBackClick = { finish() }
                )
            }
        }
    }
}
