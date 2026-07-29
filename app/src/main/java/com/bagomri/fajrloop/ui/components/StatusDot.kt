package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

/**
 * حالات العضو
 */
enum class MemberStatus(val color: Color, val label: String) {
    Awake(FajrLoopColors.Success, "استيقظ"),
    ChallengeDone(FajrLoopColors.Warning, "حل التحدي"),
    Pending(FajrLoopColors.TextTertiary, "بانتظار"),
    Travel(FajrLoopColors.Info, "مسافر"),
    Panic(FajrLoopColors.Danger, "استغاثة")
}

/**
 * نقطة حالة صغيرة (مع أو بدون نص)
 */
@Composable
fun StatusDot(
    status: MemberStatus,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    showLabel: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(status.color)
        )
        if (showLabel) {
            Text(
                text = status.label,
                fontFamily = PpNmArabic,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = status.color
            )
        }
    }
}

@Preview
@Composable
private fun StatusDotPreview() {
    FajrLoopTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MemberStatus.entries.forEach { status ->
                StatusDot(status = status, showLabel = true)
            }
        }
    }
}
