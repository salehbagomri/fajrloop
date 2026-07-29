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
fun CreateHalqaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        FajrCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "إنشاء حلقة فجر جديدة",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.Primary,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )

                FajrTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = "اسم الحلقة (مثال: حلقة الفجر)",
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
                        text = "إنشاء الحلقة",
                        onClick = {
                            if (nameInput.trim().length >= 3) {
                                onConfirm(nameInput.trim())
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
