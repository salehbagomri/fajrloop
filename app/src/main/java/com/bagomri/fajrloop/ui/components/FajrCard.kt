package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

/**
 * بطاقة موحدة — بديل GlassCard
 * خلفية Surface واضحة مع حد Border ناعم
 */
@Composable
fun FajrCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = FajrLoopColors.Surface,
    borderColor: Color = FajrLoopColors.Border,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = Radius.lg,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape),
        content = content
    )
}

@Preview
@Composable
private fun FajrCardPreview() {
    FajrLoopTheme {
        FajrCard(
            modifier = Modifier
                .padding(Spacing.lg)
                .fillMaxWidth()
                .height(100.dp)
        ) {}
    }
}
