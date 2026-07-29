package com.bagomri.fajrloop.ui.main.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.components.FajrSecondaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun InviteCodeDialog(
    halqaName: String,
    inviteCode: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        FajrCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "كود الدعوة للحلقة",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.Primary,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )

                Text(
                    text = "حلقة: $halqaName",
                    fontFamily = PpNmArabic,
                    fontSize = 14.sp,
                    color = FajrLoopColors.TextSecondary,
                    modifier = Modifier.padding(bottom = Spacing.lg)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.md))
                        .background(FajrLoopColors.SurfaceVariant)
                        .border(1.dp, FajrLoopColors.Border, RoundedCornerShape(Radius.md))
                        .clickable { onCopy() }
                        .padding(vertical = Spacing.md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = inviteCode,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = FajrLoopColors.Primary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "اضغط على الكود للنسخ السريع",
                    fontFamily = PpNmArabic,
                    fontSize = 12.sp,
                    color = FajrLoopColors.TextSecondary,
                    modifier = Modifier.padding(bottom = Spacing.xl)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    FajrSecondaryButton(
                        text = "مشاركة",
                        onClick = onShare,
                        modifier = Modifier.weight(1f)
                    )

                    FajrPrimaryButton(
                        text = "إغلاق",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
