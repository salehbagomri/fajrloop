package com.bagomri.fajrloop.ui.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupAdd
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
fun CreateHalqaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
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
                        imageVector = Icons.Outlined.GroupAdd,
                        contentDescription = null,
                        tint = FajrLoopColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Title
                Text(
                    text = "إنشاء حلقة فجر جديدة",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = FajrLoopColors.Primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Subtitle
                Text(
                    text = "أنشئ سلسلتك الخاصة وادعُ أصدقائك للاستيقاظ معاً لصلاة الفجر.",
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Input Field
                FajrTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        if (errorText != null) errorText = null
                    },
                    placeholder = "اسم الحلقة (مثال: فرسان الفجر)",
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
                            val trimmed = nameInput.trim()
                            if (trimmed.length >= 3) {
                                onConfirm(trimmed)
                            } else {
                                errorText = "يرجى إدخال اسم حلقة مكون من 3 أحرف على الأقل"
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp),
                        shape = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FajrLoopColors.Primary,
                            contentColor = Color(0xFF0D0B1A)
                        )
                    ) {
                        Text(
                            text = "إنشاء",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0D0B1A)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CreateHalqaDialogPreview() {
    FajrLoopTheme {
        CreateHalqaDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}
