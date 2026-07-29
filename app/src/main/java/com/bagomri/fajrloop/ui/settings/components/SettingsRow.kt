package com.bagomri.fajrloop.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrSwitch
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    emoji: String? = null,
    valueText: String? = null,
    isChecked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FajrLoopColors.TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = Spacing.md)
                )
            } else if (emoji != null) {
                Text(
                    text = emoji,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = Spacing.md)
                )
            }

            Column {
                Text(
                    text = title,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = FajrLoopColors.TextPrimary
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        fontFamily = PpNmArabic,
                        fontSize = 12.sp,
                        color = FajrLoopColors.TextSecondary,
                        modifier = Modifier.padding(top = Spacing.xxs)
                    )
                }
            }
        }

        if (isChecked != null && onCheckedChange != null) {
            FajrSwitch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!valueText.isNullOrEmpty()) {
                    Text(
                        text = valueText,
                        fontFamily = PpNmArabic,
                        fontSize = 13.sp,
                        color = FajrLoopColors.Primary,
                        modifier = Modifier.padding(end = Spacing.xs)
                    )
                }

                if (onClick != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = "الانتقال",
                        tint = FajrLoopColors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
