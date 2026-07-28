package com.bagomri.fajrloop.ui.settings.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTimingDialog(
    initialType: String,
    initialOffset: Int,
    onSaveTiming: (String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedOffset by remember { mutableFloatStateOf(initialOffset.toFloat().coerceAtLeast(1f)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FajrLoopColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "توقيت رنين المنبه ⏰",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Gold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Timing Types
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimingTypeOption(
                    title = "قبل الأذان",
                    isSelected = selectedType == "before",
                    onClick = { selectedType = "before" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "مع الأذان",
                    isSelected = selectedType == "with",
                    onClick = {
                        selectedType = "with"
                        selectedOffset = 0f
                    },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "بعد الأذان",
                    isSelected = selectedType == "after",
                    onClick = { selectedType = "after" },
                    modifier = Modifier.weight(1f)
                )
            }

            if (selectedType != "with") {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (selectedType == "before") "قبل الأذان بـ ${selectedOffset.toInt()} دقيقة ⏰"
                    else "بعد الأذان بـ ${selectedOffset.toInt()} دقيقة ⏱️",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = FajrLoopColors.TextPrimary
                )

                Slider(
                    value = selectedOffset,
                    onValueChange = { selectedOffset = it },
                    valueRange = 1f..60f,
                    steps = 59,
                    colors = SliderDefaults.colors(
                        thumbColor = FajrLoopColors.Gold,
                        activeTrackColor = FajrLoopColors.Gold,
                        inactiveTrackColor = FajrLoopColors.SurfaceBorder
                    ),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Quick buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(5, 10, 15, 30, 45).forEach { min ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(FajrLoopColors.Background.copy(alpha = 0.5f))
                                .clickable { selectedOffset = min.toFloat() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$min د",
                                fontFamily = PpNmArabic,
                                fontSize = 12.sp,
                                color = FajrLoopColors.Gold
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "سيقوم المنبه بالرنين مع دخول وقت أذان الفجر بالضبط 🕌",
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GoldButton(
                text = "حفظ التوقيت",
                onClick = {
                    val offset = selectedOffset.toInt()
                    val desc = when (selectedType) {
                        "before" -> "قبل الأذان بـ $offset دقيقة ⏰"
                        "after" -> "بعد الأذان بـ $offset دقيقة ⏱️"
                        else -> "مع أذان الفجر بالضبط 🕌"
                    }
                    onSaveTiming(selectedType, offset, desc)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TimingTypeOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) FajrLoopColors.Gold.copy(alpha = 0.2f) else FajrLoopColors.Background.copy(alpha = 0.4f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = PpNmArabic,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            color = if (isSelected) FajrLoopColors.Gold else FajrLoopColors.TextSecondary
        )
    }
}
