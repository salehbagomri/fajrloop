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
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeSettingsDialog(
    initialType: String,
    initialDifficulty: String,
    onSaveChallenge: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedDifficulty by remember { mutableStateOf(initialDifficulty) }

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
                text = "تحدي الاستيقاظ 🧩",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Gold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "نوع التحدي",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = FajrLoopColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimingTypeOption(
                    title = "معادلة حسابية 🧮",
                    isSelected = selectedType == "math",
                    onClick = { selectedType = "math" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "كتابة كلمة ✍️",
                    isSelected = selectedType == "word",
                    onClick = { selectedType = "word" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "رج الهاتف 📱",
                    isSelected = selectedType == "shake",
                    onClick = { selectedType = "shake" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "مستوى الصعوبة",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = FajrLoopColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimingTypeOption(
                    title = "سهل 😊",
                    isSelected = selectedDifficulty == "easy",
                    onClick = { selectedDifficulty = "easy" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "متوسط 😐",
                    isSelected = selectedDifficulty == "medium",
                    onClick = { selectedDifficulty = "medium" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "صعب 🔥",
                    isSelected = selectedDifficulty == "hard",
                    onClick = { selectedDifficulty = "hard" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GoldButton(
                text = "حفظ التحدي",
                onClick = {
                    onSaveChallenge(selectedType, selectedDifficulty)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
