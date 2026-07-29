package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme

/**
 * مفتاح تبديل موحد — ألوان متسقة مع نظام التصميم
 */
@Composable
fun FajrSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val trackColor = if (checked) {
        FajrLoopColors.Primary.copy(alpha = 0.3f)
    } else {
        FajrLoopColors.Border
    }

    val thumbColor = if (checked) {
        FajrLoopColors.Primary
    } else {
        FajrLoopColors.TextTertiary
    }

    val trackWidth = 44.dp
    val trackHeight = 24.dp
    val thumbSize = 18.dp
    val thumbPadding = 3.dp

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackColor, CircleShape)
            .border(1.dp, if (checked) FajrLoopColors.Primary.copy(alpha = 0.5f) else Color.Transparent, CircleShape)
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .padding(thumbPadding)
                .size(thumbSize)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Preview
@Composable
private fun FajrSwitchPreview() {
    FajrLoopTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            FajrSwitch(checked = true, onCheckedChange = {})
            FajrSwitch(checked = false, onCheckedChange = {})
        }
    }
}
