package com.bagomri.fajrloop.ui.main.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.components.FajrSecondaryButton
import com.bagomri.fajrloop.ui.components.FajrTextField
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun JoinHalqaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var codeInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        FajrCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "الانضمام لحلقة فجر",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.Primary,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )

                FajrTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    placeholder = "أدخل كود الدعوة المكون من 6 أرقام",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.xl)
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

                    FajrPrimaryButton(
                        text = "انضمام",
                        onClick = {
                            if (codeInput.trim().isNotEmpty()) {
                                onConfirm(codeInput.trim())
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
