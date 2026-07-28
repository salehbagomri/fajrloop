package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun CountdownCard(
    fajrTimeStr: String,
    sunriseTimeStr: String,
    countdownText: String,
    countdownColorHex: String,
    borderMode: Int,
    modifier: Modifier = Modifier
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(countdownColorHex))
    } catch (e: Exception) {
        FajrLoopColors.Gold
    }

    val borderColor = when (borderMode) {
        3, 2 -> FajrLoopColors.DangerRed.copy(alpha = 0.6f)
        1 -> FajrLoopColors.Gold.copy(alpha = 0.5f)
        else -> FajrLoopColors.SurfaceBorder
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "المتبقي على أذان الفجر",
                fontFamily = PpNmArabic,
                fontSize = 13.sp,
                color = FajrLoopColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = countdownText,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = parsedColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "أذان الفجر 🕌",
                        fontFamily = PpNmArabic,
                        fontSize = 12.sp,
                        color = FajrLoopColors.TextSecondary
                    )
                    Text(
                        text = fajrTimeStr,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FajrLoopColors.Gold
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .border(0.5.dp, FajrLoopColors.SurfaceBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الشروق 🌅",
                        fontFamily = PpNmArabic,
                        fontSize = 12.sp,
                        color = FajrLoopColors.TextSecondary
                    )
                    Text(
                        text = sunriseTimeStr,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FajrLoopColors.TextPrimary
                    )
                }
            }
        }
    }
}
