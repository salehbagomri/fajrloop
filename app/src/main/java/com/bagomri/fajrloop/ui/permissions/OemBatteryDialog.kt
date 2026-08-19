package com.bagomri.fajrloop.ui.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import com.bagomri.fajrloop.util.OemBatteryHelper

/**
 * Dialog تعليمي يُرشد المستخدم خطوة بخطوة لتفعيل إعدادات التشغيل الخلفي
 * على الأجهزة التي تحتاج إعداداً يدوياً (Honor/Huawei/Samsung).
 * هذا هو النهج العالمي المتبع في التطبيقات الكبيرة كـ WhatsApp وAlarmy.
 */
@Composable
fun OemBatteryDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val isHuaweiOrHonor = OemBatteryHelper.isHuaweiOrHonor

    val title = if (isHuaweiOrHonor)
        "إعدادات التشغيل — Honor"
    else
        "إعدادات الخلفية — Samsung"

    val subtitle = if (isHuaweiOrHonor)
        "هاتفك يُقيّد التطبيقات في الخلفية تلقائياً. اتبع الخطوات لضمان رنين منبه الفجر:"
    else
        "قد يُوقف هاتفك التطبيقات النائمة. اتبع الخطوات لضمان رنين المنبه:"

    val steps = if (isHuaweiOrHonor) listOf(
        "اضغط \"فتح إعدادات التطبيق\" أدناه",
        "اضغط على \"تفاصيل استخدام الطاقة\"",
        "اضغط على \"إعدادات التشغيل\"",
        "غيّر من \"إدارة تلقائية\" إلى \"إدارة يدوية\"",
        "فعّل الخيارات الثلاثة: التشغيل التلقائي، الثانوي، والخلفي"
    ) else listOf(
        "اضغط \"فتح إعدادات التطبيق\" أدناه",
        "اضغط على \"البطارية\"",
        "اختر \"غير محدود\" (Unrestricted)",
        "ارجع وتأكد أن التطبيق ليس في قائمة التطبيقات النائمة"
    )

    Dialog(onDismissRequest = onDismiss) {
        FajrCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // أيقونة
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(FajrLoopColors.Warning.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhoneAndroid,
                        contentDescription = null,
                        tint = FajrLoopColors.Warning,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = title,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = subtitle,
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                // خطوات مرقّمة
                steps.forEachIndexed { index, step ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // رقم الخطوة
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(Radius.full))
                                .background(FajrLoopColors.Warning.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FajrLoopColors.Warning
                            )
                        }
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = step,
                            fontFamily = PpNmArabic,
                            fontSize = 14.sp,
                            color = FajrLoopColors.TextPrimary,
                            lineHeight = 21.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.lg))

                // أزرار الإجراء
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Radius.md),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FajrLoopColors.TextSecondary
                        )
                    ) {
                        Text(
                            text = "لاحقاً",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            onOpenSettings()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.6f),
                        shape = RoundedCornerShape(Radius.md),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FajrLoopColors.Warning,
                            contentColor = Color(0xFF1A1000)
                        )
                    ) {
                        Text(
                            text = "فتح الإعدادات",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
