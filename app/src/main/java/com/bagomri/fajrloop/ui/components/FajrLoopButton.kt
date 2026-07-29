package com.bagomri.fajrloop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

/**
 * زر رئيسي ذهبي — CTA الأساسي
 */
@Composable
fun FajrPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        colors = ButtonDefaults.buttonColors(
            containerColor = FajrLoopColors.Primary,
            contentColor = Color(0xFF0D0B1A),
            disabledContainerColor = FajrLoopColors.Primary.copy(alpha = 0.4f),
            disabledContentColor = Color(0xFF0D0B1A).copy(alpha = 0.5f)
        )
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = Spacing.xs)
            )
        }
        Text(
            text = text,
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

/**
 * زر ثانوي شفاف — إجراء ثانوي
 */
@Composable
fun FajrSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(1.dp, FajrLoopColors.Primary.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = FajrLoopColors.Primary
        )
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = Spacing.xs)
            )
        }
        Text(
            text = text,
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

/**
 * زر خطر — حذف / خروج / SOS
 */
@Composable
fun FajrDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(Radius.md),
        border = BorderStroke(1.dp, FajrLoopColors.Danger.copy(alpha = 0.3f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = FajrLoopColors.Danger.copy(alpha = 0.1f),
            contentColor = FajrLoopColors.Danger
        )
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = Spacing.xs)
            )
        }
        Text(
            text = text,
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

// ── Deprecated aliases ─────────────────────────────────────

@Deprecated("استخدم FajrPrimaryButton بدلاً منه", replaceWith = ReplaceWith("FajrPrimaryButton(text, onClick, modifier, enabled)"))
@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = FajrPrimaryButton(text, onClick, modifier, enabled)

@Deprecated("استخدم FajrSecondaryButton بدلاً منه", replaceWith = ReplaceWith("FajrSecondaryButton(text, onClick, modifier, enabled)"))
@Composable
fun TransparentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = FajrSecondaryButton(text, onClick, modifier, enabled)

@Deprecated("استخدم FajrDestructiveButton بدلاً منه", replaceWith = ReplaceWith("FajrDestructiveButton(text, onClick, modifier, enabled)"))
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = FajrDestructiveButton(text, onClick, modifier, enabled)

@Preview
@Composable
private fun FajrButtonsPreview() {
    FajrLoopTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            FajrPrimaryButton(
                text = "زر رئيسي",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = FajrIcons.Settings
            )
            FajrSecondaryButton(
                text = "زر ثانوي",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            FajrDestructiveButton(
                text = "تسجيل الخروج",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = FajrIcons.Logout
            )
        }
    }
}
