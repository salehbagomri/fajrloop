package com.bagomri.fajrloop.ui.settings.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

val alarmSoundsList = listOf(
    Pair("afasy", "الأذان بصوت الشيخ مشاري العفاسي"),
    Pair("abdulbasit", "الأذان بصوت الشيخ عبدالباسط عبدالصمد"),
    Pair("islamic", "نغمة إسلامية هادئة"),
    Pair("default", "نغمة النظام الافتراضية")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSoundDialog(
    currentSound: String,
    onSoundSelect: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FajrLoopColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "صوت ونغمة المنبه 🔔",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Gold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                alarmSoundsList.forEach { (code, title) ->
                    val isSelected = currentSound == code

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSoundSelect(code, title)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = title,
                                fontFamily = PpNmArabic,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isSelected) FajrLoopColors.Gold else FajrLoopColors.TextPrimary
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = FajrLoopColors.Gold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
