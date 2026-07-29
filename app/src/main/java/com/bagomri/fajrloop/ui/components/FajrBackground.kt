package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme

/**
 * خلفية ثابتة — بديل AnimatedGradientBackground
 * تدرج عمودي ناعم بدون حركة
 */
@Composable
fun FajrBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FajrLoopColors.Background,
                        FajrLoopColors.SurfaceVariant.copy(alpha = 0.3f),
                        FajrLoopColors.Background
                    )
                )
            )
    )
}

@Preview
@Composable
private fun FajrBackgroundPreview() {
    FajrLoopTheme {
        FajrBackground()
    }
}
