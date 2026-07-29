package com.bagomri.fajrloop.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = FajrLoopColors.Primary,
            modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.sm)
        )

        FajrCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}
