package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.main.FriendWakeAlert
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun FriendWakeAlertCard(
    alert: FriendWakeAlert,
    onConfirmClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FajrLoopColors.Gold, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔔 تنبيه استيقاظ صديق!",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = FajrLoopColors.Gold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = alert.message,
                fontFamily = PpNmArabic,
                fontSize = 13.sp,
                color = FajrLoopColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            GoldButton(
                text = "تأكيد استيقاظ ${alert.displayName} وإيقاف منبهه ✓",
                onClick = { onConfirmClick(alert.uid) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
