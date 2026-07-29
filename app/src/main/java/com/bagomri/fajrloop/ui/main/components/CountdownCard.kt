package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

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
        FajrLoopColors.Primary
    }

    val borderColor = when (borderMode) {
        3, 2 -> FajrLoopColors.Danger.copy(alpha = 0.6f)
        1 -> FajrLoopColors.Primary.copy(alpha = 0.5f)
        else -> FajrLoopColors.Border
    }

    FajrCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = borderColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "المتبقي لأذان الفجر",
                fontFamily = PpNmArabic,
                fontSize = 13.sp,
                color = FajrLoopColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            Text(
                text = countdownText,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 48.sp,
                color = parsedColor
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            HorizontalDivider(
                color = FajrLoopColors.BorderSubtle,
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // أذان الفجر
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = FajrLoopColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "أذان الفجر",
                            fontFamily = PpNmArabic,
                            fontSize = 12.sp,
                            color = FajrLoopColors.TextSecondary
                        )
                    }
                    Text(
                        text = fajrTimeStr,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FajrLoopColors.Primary
                    )
                }

                // Divider عمودي
                Box(
                    modifier = Modifier
                        .height(Spacing.xxl)
                        .width(0.5.dp)
                        .padding()
                ) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(0.5.dp),
                        color = FajrLoopColors.BorderSubtle
                    )
                }

                // الشروق
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الشروق",
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
