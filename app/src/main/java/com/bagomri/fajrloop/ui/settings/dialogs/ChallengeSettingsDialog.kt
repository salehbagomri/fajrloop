package com.bagomri.fajrloop.ui.settings.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

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
                .padding(Spacing.xl)
        ) {
            Text(
                text = "تحدي الاستيقاظ",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Primary,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            Text(
                text = "نوع التحدي",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = FajrLoopColors.TextSecondary,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                TimingTypeOption(
                    title = "معادلة حسابية",
                    isSelected = selectedType == "math",
                    onClick = { selectedType = "math" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "ترتيب كلمة",
                    isSelected = selectedType == "word",
                    onClick = { selectedType = "word" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "رج الهاتف",
                    isSelected = selectedType == "shake",
                    onClick = { selectedType = "shake" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = "مستوى الصعوبة",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = FajrLoopColors.TextSecondary,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                TimingTypeOption(
                    title = "سهل",
                    isSelected = selectedDifficulty == "easy",
                    onClick = { selectedDifficulty = "easy" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "متوسط",
                    isSelected = selectedDifficulty == "medium",
                    onClick = { selectedDifficulty = "medium" },
                    modifier = Modifier.weight(1f)
                )
                TimingTypeOption(
                    title = "صعب",
                    isSelected = selectedDifficulty == "hard",
                    onClick = { selectedDifficulty = "hard" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            FajrPrimaryButton(
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
