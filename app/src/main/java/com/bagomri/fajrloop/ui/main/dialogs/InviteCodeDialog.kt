package com.bagomri.fajrloop.ui.main.dialogs

import androidx.compose.foundation.clickable
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
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun InviteCodeDialog(
    halqaName: String,
    inviteCode: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
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
                    text = "كود الدعوة للحلقة 🔗",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.Gold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "حلقة: $halqaName",
                    fontFamily = PpNmArabic,
                    fontSize = 14.sp,
                    color = FajrLoopColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopy() }
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = inviteCode,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = FajrLoopColors.Gold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                Text(
                    text = "اضغط على الكود للنسخ السريع 📋",
                    fontFamily = PpNmArabic,
                    fontSize = 12.sp,
                    color = FajrLoopColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("مشاركة 📲", fontFamily = PpNmArabic, color = FajrLoopColors.Gold)
                    }

                    GoldButton(
                        text = "إغلاق",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
