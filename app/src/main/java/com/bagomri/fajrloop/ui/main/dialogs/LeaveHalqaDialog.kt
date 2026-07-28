package com.bagomri.fajrloop.ui.main.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bagomri.fajrloop.ui.components.DangerButton
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun LeaveHalqaDialog(
    title: String = "مغادرة الحلقة 🚪",
    description: String = "هل أنت متأكد من رغبتك في مغادرة هذه الحلقة؟",
    confirmText: String = "مغادرة الحلقة",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.DangerRed,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Text(
                    text = description,
                    fontFamily = PpNmArabic,
                    fontSize = 14.sp,
                    color = FajrLoopColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء", fontFamily = PpNmArabic, color = FajrLoopColors.TextSecondary)
                    }

                    DangerButton(
                        text = confirmText,
                        onClick = {
                            onConfirm()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
