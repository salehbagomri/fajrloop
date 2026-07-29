package com.bagomri.fajrloop.ui.main.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
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
    val displayCode = inviteCode.ifEmpty { "FJR-E3ZL" }

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
                // Top Icon Badge — Envelope with red heart
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262033))
                        .border(1.dp, Color(0xFF3B334D), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Mail,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "❤️",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Title — "رمز دعوة الصديق"
                Text(
                    text = "رمز دعوة الصديق",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = FajrLoopColors.Primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                // Halqa Name Chip — "حلقة: الفجر2"
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF161528))
                        .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = Spacing.xl, vertical = 6.dp)
                ) {
                    Text(
                        text = "حلقة: ${halqaName.ifEmpty { "الفجر" }}",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Description Text
                Text(
                    text = "شارك هذا الرمز مع صديقك للانضمام إلى سلسلتك والاستيقاظ معاً لصلاة الفجر.",
                    fontFamily = PpNmArabic,
                    fontSize = 14.sp,
                    color = FajrLoopColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = Spacing.sm)
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                // Code Container Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(Color(0xFF131224))
                        .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(Radius.lg))
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Copy Button (On Left in RTL)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(Radius.full))
                                .background(Color(0xFF1E1D33))
                                .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.5f), RoundedCornerShape(Radius.full))
                                .clickable { onCopy() }
                                .padding(horizontal = Spacing.lg, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "نسخ",
                                    tint = FajrLoopColors.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "نسخ",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FajrLoopColors.Primary
                                )
                            }
                        }

                        // Code Text (On Right in RTL)
                        Text(
                            text = displayCode,
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = FajrLoopColors.Primary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xxl))

                // Bottom Actions: Share (Gold Primary) & Close (Dark Secondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    // Close Button
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
                            text = "إغلاق",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    // Share Button (Solid Gold)
                    Button(
                        onClick = onShare,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(50.dp),
                        shape = RoundedCornerShape(Radius.full),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FajrLoopColors.Primary,
                            contentColor = FajrLoopColors.Background
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = FajrLoopColors.Background,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = "مشاركة الرمز",
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
}

@Preview
@Composable
private fun InviteCodeDialogPreview() {
    FajrLoopTheme {
        InviteCodeDialog(
            halqaName = "الفجر2",
            inviteCode = "FJR-E3ZL",
            onCopy = {},
            onShare = {},
            onDismiss = {}
        )
    }
}
