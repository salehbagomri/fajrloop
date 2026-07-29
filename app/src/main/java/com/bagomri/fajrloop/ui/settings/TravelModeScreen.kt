package com.bagomri.fajrloop.ui.settings

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.bagomri.fajrloop.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TravelModeScreen(
    initialEnabled: Boolean,
    initialType: String,
    initialUntil: String,
    onSaveTravelMode: (Boolean, String, String) -> Unit,
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
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Switch card
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
                                text = if (isEnabled) "نشط حالياً" else "غير نشط حالياً",
                                fontFamily = PpNmArabic,
                                fontSize = 13.sp,
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
                                        .clickable { selectedType = type }
                                        .padding(vertical = Spacing.xs),
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
                                        color = FajrLoopColors.TextPrimary
                                    )
                                }
                            }

                            // Custom date option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker() }
                                    .padding(vertical = Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedType == "custom",
                                    onClick = { showDatePicker() },
                                    colors = RadioButtonDefaults.colors(selectedColor = FajrLoopColors.Primary)
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                                Column {
                                    Text(
                                        text = "تاريخ مخصص",
                                        fontFamily = PpNmArabic,
                                        fontSize = 14.sp,
                                        color = FajrLoopColors.TextPrimary
                                    )
                                    if (customDate.isNotEmpty()) {
                                        Text(
                                            text = "حتى: $customDate",
                                            fontFamily = PpNmArabic,
                                            fontSize = 12.sp,
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
                    text = "حفظ وضع السفر",
                    onClick = {
                        var untilText = "حتى الإلغاء اليدوي"
                        if (isEnabled) {
                            val cal = Calendar.getInstance()
                            untilText = when (selectedType) {
                                "1_day" -> {
                                    cal.add(Calendar.DAY_OF_MONTH, 1)
                                    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                                }
                                "3_days" -> {
                                    cal.add(Calendar.DAY_OF_MONTH, 3)
                                    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                                }
                                "7_days" -> {
                                    cal.add(Calendar.DAY_OF_MONTH, 7)
                                    SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(cal.time)
                                }
                                "custom" -> customDate.ifEmpty { "حتى الإلغاء اليدوي" }
                                else -> "حتى الإلغاء اليدوي"
                            }
                        }
                        onSaveTravelMode(isEnabled, selectedType, untilText)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
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
            onSaveTravelMode = { _, _, _ -> },
            onBackClick = {}
        )
    }
}
