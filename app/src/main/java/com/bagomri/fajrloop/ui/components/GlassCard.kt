package com.bagomri.fajrloop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f),
    backgroundColor: Color = FajrLoopColors.Surface.copy(alpha = 0.04f),
    cornerRadius: Dp = 16.dp,
    isPulsing: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val scale by if (isPulsing) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        rememberUpdatedState(1f)
    }

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor)
            .border(1.5.dp, borderColor, shape)
            .drawWithContent {
                drawContent()
                // Top glass shine overlay
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.086f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.35f
                    )
                )
            },
        content = content
    )
}

@Preview
@Composable
fun GlassCardPreview() {
    FajrLoopTheme {
        GlassCard(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(100.dp)
        ) {
            // Preview content
        }
    }
}
