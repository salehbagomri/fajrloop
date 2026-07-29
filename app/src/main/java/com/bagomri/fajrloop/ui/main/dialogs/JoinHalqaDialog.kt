package com.bagomri.fajrloop.ui.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrTextField
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun JoinHalqaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        FajrCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = FajrLoopColors.Primary.copy(alpha = 0.35f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xxl, vertical = Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Icon Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262033))
                        .border(1.dp, Color(0xFF3B334D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Key,
                        contentDescription = null,
                        tint = FajrLoopColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Title
                Text(
                    text = "الانضمام إلى حلقة فجر",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = FajrLoopColors.Primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Subtitle
                Text(
                    text = "أدخل كود الدعوة المشارك معك للانضمام مباشرة لسلسلة الاستيقاظ.",
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Input Field
                FajrTextField(
                    value = codeInput,
                    onValueChange = {
                        codeInput = it.uppercase()
                        if (errorText != null) errorText = null
                    },
                    placeholder = "كود الدعوة (مثال: FJR-E3ZL)",
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        fontFamily = PpNmArabic,
                        fontSize = 12.sp,
                        color = FajrLoopColors.Danger,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.xs)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B192C),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color(0xFF332D48))
                    ) {
                        Text(
                            text = "إلغاء",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    // Confirm Button (Solid Gold)
                    Button(
                        onClick = {
                            val trimmed = codeInput.trim()
                            if (trimmed.isNotEmpty()) {
                                onConfirm(trimmed)
                            } else {
                                errorText = "يرجى إدخال كود الدعوة"
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp),
                        shape = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FajrLoopColors.Primary,
                            contentColor = FajrLoopColors.Background
                        )
                    ) {
                        Text(
                            text = "الانضمام للحلقة",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FajrLoopColors.Background
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun JoinHalqaDialogPreview() {
    FajrLoopTheme {
        JoinHalqaDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}
