package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius

/**
 * حوار موحد — خلفية Surface مع زوايا RadiusLg
 * لون العنوان TextPrimary (ليس Gold)
 */
@Composable
fun FajrDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "تأكيد",
    dismissText: String = "إلغاء",
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = title,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = FajrLoopColors.TextPrimary
            )
        },
        text = content,
        confirmButton = {
            if (onConfirm != null) {
                TextButton(onClick = onConfirm) {
                    Text(
                        text = confirmText,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        color = FajrLoopColors.Primary
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    fontFamily = PpNmArabic,
                    color = FajrLoopColors.TextSecondary
                )
            }
        },
        shape = RoundedCornerShape(Radius.lg),
        containerColor = FajrLoopColors.Surface,
        titleContentColor = FajrLoopColors.TextPrimary,
        textContentColor = FajrLoopColors.TextSecondary
    )
}

/**
 * حوار تأكيد حذف / خطر
 */
@Composable
fun FajrDestructiveDialog(
    title: String,
    message: String,
    confirmText: String = "تأكيد",
    dismissText: String = "إلغاء",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = title,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = FajrLoopColors.TextPrimary
            )
        },
        text = {
            Text(
                text = message,
                fontFamily = PpNmArabic,
                color = FajrLoopColors.TextPrimary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    color = FajrLoopColors.Danger
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    fontFamily = PpNmArabic,
                    color = FajrLoopColors.TextSecondary
                )
            }
        },
        shape = RoundedCornerShape(Radius.lg),
        containerColor = FajrLoopColors.Surface
    )
}

@Preview
@Composable
private fun FajrDialogPreview() {
    FajrLoopTheme {
        FajrDialog(
            title = "تأكيد العملية",
            onDismiss = {},
            onConfirm = {}
        ) {
            Text("هل تريد المتابعة؟", fontFamily = PpNmArabic, color = FajrLoopColors.TextPrimary)
        }
    }
}
