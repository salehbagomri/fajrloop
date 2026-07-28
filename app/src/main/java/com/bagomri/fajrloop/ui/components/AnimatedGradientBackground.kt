package com.bagomri.fajrloop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier
) {
    val gradientColors = listOf(
        FajrLoopColors.Background,
        FajrLoopColors.NightBlue,
        FajrLoopColors.NightPurple,
        FajrLoopColors.Background
    )

    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w == 0f || h == 0f) return@Canvas

        val progress4 = progress * 4f
        val phase = progress4.toInt().coerceIn(0, 3)
        val t = progress4 - phase

        fun lerp(a: Float, b: Float, fraction: Float): Float = a + (b - a) * fraction

        val startOffset = when (phase) {
            0 -> Offset(lerp(0f, w, t), 0f)
            1 -> Offset(w, lerp(0f, h, t))
            2 -> Offset(lerp(w, 0f, t), h)
            else -> Offset(0f, lerp(h, 0f, t))
        }

        val endOffset = when (phase) {
            0 -> Offset(lerp(w, 0f, t), h)
            1 -> Offset(0f, lerp(h, 0f, t))
            2 -> Offset(lerp(0f, w, t), 0f)
            else -> Offset(w, lerp(0f, h, t))
        }

        drawRect(
            brush = Brush.linearGradient(
                colors = gradientColors,
                start = startOffset,
                end = endOffset
            )
        )
    }
}

@Preview
@Composable
fun AnimatedGradientBackgroundPreview() {
    FajrLoopTheme {
        AnimatedGradientBackground()
    }
}
