package com.bagomri.fajrloop.ui.main.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrDestructiveButton
import com.bagomri.fajrloop.ui.components.FajrSecondaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun LeaveHalqaDialog(
    title: String = "مغادرة الحلقة",
    description: String = "هل أنت متأكد من رغبتك في مغادرة هذه الحلقة؟",
    confirmText: String = "مغادرة",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        FajrCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = FajrLoopColors.Danger.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.Danger,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )

                Text(
                    text = description,
                    fontFamily = PpNmArabic,
                    fontSize = 14.sp,
                    color = FajrLoopColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = Spacing.xl)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    FajrSecondaryButton(
                        text = "إلغاء",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    FajrDestructiveButton(
                        text = confirmText,
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
