package com.bagomri.fajrloop.ui.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (emoji != null) {
                Text(
                    text = emoji,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FajrLoopColors.Gold,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 12.dp)
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
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        if (isChecked != null && onCheckedChange != null) {
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = FajrLoopColors.Gold,
                    checkedTrackColor = FajrLoopColors.Gold.copy(alpha = 0.3f),
                    uncheckedThumbColor = FajrLoopColors.TextSecondary,
                    uncheckedTrackColor = FajrLoopColors.Surface
                )
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!valueText.isNullOrEmpty()) {
                    Text(
                        text = valueText,
                        fontFamily = PpNmArabic,
                        fontSize = 13.sp,
                        color = FajrLoopColors.Gold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                if (onClick != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "الانتقال",
                        tint = FajrLoopColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
