package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.main.FriendWakeAlert
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun FriendWakeAlertCard(
    alert: FriendWakeAlert,
    onConfirmClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FajrCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = FajrLoopColors.Warning
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = "تنبيه استيقاظ",
                tint = FajrLoopColors.Warning,
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = Spacing.xs)
            )

            Text(
                text = "تنبيه استيقاظ صديق",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = FajrLoopColors.Warning,
                modifier = Modifier.padding(bottom = Spacing.xs)
            )

            Text(
                text = alert.message,
                fontFamily = PpNmArabic,
                fontSize = 13.sp,
                color = FajrLoopColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            FajrPrimaryButton(
                text = "تأكيد استيقاظ ${alert.displayName}",
                onClick = { onConfirmClick(alert.uid) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
