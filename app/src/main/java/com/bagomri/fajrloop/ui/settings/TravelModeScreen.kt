package com.bagomri.fajrloop.ui.settings

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.components.FajrSwitch
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TravelModeScreen(
    initialEnabled: Boolean,
    initialType: String,
    initialUntil: String,
    onSaveTravelMode: (Boolean, String, String, Long) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(initialEnabled) }
    var selectedType by remember { mutableStateOf(initialType) }
    var customDate by remember { mutableStateOf(if (initialType == "custom") initialUntil else "") }

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                val formatted = String.format("%04d/%02d/%02d", year, month + 1, day)
                customDate = formatted
                selectedType = "custom"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.datePicker.minDate = cal.timeInMillis
        datePicker.show()
    }

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            FajrLoopTopBar(
                title = "وضع السفر",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Explanatory Info Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(Color(0xFF1A1830))
                        .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.25f), RoundedCornerShape(Radius.md))
                        .padding(Spacing.lg)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = FajrLoopColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Column {
                            Text(
                                text = "ما هو وضع السفر؟",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = FajrLoopColors.Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "عند تفعيل وضع السفر، سيتم إيقاف المنبه والإشعارات مؤقتاً طوال فترة سفرك. لن يتم إخطار أعضاء الحلقة بعدم التزامك خلال هذه الفترة.",
                                fontFamily = PpNmArabic,
                                fontSize = 12.sp,
                                color = FajrLoopColors.TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Header Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEnabled) FajrLoopColors.PrimaryContainer
                            else Color(0xFF1E1C30)
                        )
                        .border(
                            1.dp,
                            if (isEnabled) FajrLoopColors.Primary.copy(alpha = 0.5f)
                            else Color(0xFF2D2A45),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FlightTakeoff,
                        contentDescription = null,
                        tint = if (isEnabled) FajrLoopColors.Primary else FajrLoopColors.TextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Switch Card
                FajrCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تفعيل وضع السفر",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FajrLoopColors.TextPrimary
                            )
                            Text(
                                text = if (isEnabled) "نشط حالياً — المنبه متوقف مؤقتاً" else "غير نشط — المنبه يعمل بشكل طبيعي",
                                fontFamily = PpNmArabic,
                                fontSize = 12.sp,
                                color = if (isEnabled) FajrLoopColors.Primary else FajrLoopColors.TextSecondary,
                                modifier = Modifier.padding(top = Spacing.xxs)
                            )
                        }

                        FajrSwitch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it }
                        )
                    }
                }

                if (isEnabled) {
                    FajrCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Spacing.lg)) {
                            Text(
                                text = "مدة وضع السفر",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = FajrLoopColors.Primary,
                                modifier = Modifier.padding(bottom = Spacing.sm)
                            )
                            Text(
                                text = "اختر المدة التي تريد إيقاف المنبه خلالها:",
                                fontFamily = PpNmArabic,
                                fontSize = 12.sp,
                                color = FajrLoopColors.TextSecondary,
                                modifier = Modifier.padding(bottom = Spacing.md)
                            )

                            val options = listOf(
                                "indefinite" to "حتى الإلغاء اليدوي",
                                "1_day" to "يوم واحد (24 ساعة)",
                                "3_days" to "3 أيام",
                                "7_days" to "أسبوع كامل (7 أيام)"
                            )

                            options.forEach { (type, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(Radius.sm))
                                        .background(
                                            if (selectedType == type) FajrLoopColors.PrimaryContainer
                                            else Color.Transparent
                                        )
                                        .clickable { selectedType = type }
                                        .padding(vertical = Spacing.xs, horizontal = Spacing.xs),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedType == type,
                                        onClick = { selectedType = type },
                                        colors = RadioButtonDefaults.colors(selectedColor = FajrLoopColors.Primary)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.sm))
                                    Text(
                                        text = label,
                                        fontFamily = PpNmArabic,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedType == type) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selectedType == type) FajrLoopColors.TextPrimary else FajrLoopColors.TextSecondary
                                    )
                                }
                            }

                            // Custom date option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Radius.sm))
                                    .background(
                                        if (selectedType == "custom") FajrLoopColors.PrimaryContainer
                                        else Color.Transparent
                                    )
                                    .clickable { showDatePicker() }
                                    .padding(vertical = Spacing.xs, horizontal = Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedType == "custom",
                                    onClick = { showDatePicker() },
                                    colors = RadioButtonDefaults.colors(selectedColor = FajrLoopColors.Primary)
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = if (selectedType == "custom") FajrLoopColors.Primary else FajrLoopColors.TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Column {
                                    Text(
                                        text = "تاريخ مخصص",
                                        fontFamily = PpNmArabic,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedType == "custom") FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (selectedType == "custom") FajrLoopColors.TextPrimary else FajrLoopColors.TextSecondary
                                    )
                                    if (customDate.isNotEmpty()) {
                                        Text(
                                            text = "حتى: $customDate",
                                            fontFamily = PpNmArabic,
                                            fontSize = 11.sp,
                                            color = FajrLoopColors.Primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                FajrPrimaryButton(
                    text = if (isEnabled) "حفظ وتفعيل وضع السفر" else "حفظ الإعدادات وإلغاء وضع السفر",
                    onClick = {
                        val (untilText, untilTimestamp) = if (isEnabled) {
                            com.bagomri.fajrloop.alarm.TravelModeManager.calculateUntilTimestamp(selectedType, customDate)
                        } else {
                            Pair("غير نشط حالياً", 0L)
                        }
                        onSaveTravelMode(isEnabled, selectedType, untilText, untilTimestamp)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.lg))
            }
        }
    }
}

@Preview
@Composable
private fun TravelModeScreenPreview() {
    FajrLoopTheme {
        TravelModeScreen(
            initialEnabled = true,
            initialType = "3_days",
            initialUntil = "2026/08/01",
            onSaveTravelMode = { _, _, _, _ -> },
            onBackClick = {}
        )
    }
}
