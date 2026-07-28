package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun QuickActionsGrid(
    isInHalqa: Boolean,
    onHalqaDetailsClick: () -> Unit,
    onChatClick: () -> Unit,
    onStatsClick: () -> Unit,
    onInviteClick: () -> Unit,
    onCreateHalqaClick: () -> Unit,
    onJoinHalqaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "الإجراءات السريعة ⚡",
            fontFamily = PpNmArabic,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = FajrLoopColors.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isInHalqa) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    icon = "👥",
                    title = "تفاصيل الحلقة",
                    onClick = onHalqaDetailsClick,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = "💬",
                    title = "محادثة الحلقة",
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    icon = "📊",
                    title = "سجل التزامي",
                    onClick = onStatsClick,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = "🔗",
                    title = "دعوة أصدقاء",
                    onClick = onInviteClick,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(
                    icon = "➕",
                    title = "إنشاء حلقة جديدة",
                    onClick = onCreateHalqaClick,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = "🔑",
                    title = "الانضمام بكود",
                    onClick = onJoinHalqaClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ActionCard(
                icon = "📊",
                title = "سجل التزامي الفردي",
                onClick = onStatsClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActionCard(
    icon: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = title,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = FajrLoopColors.TextPrimary
            )
        }
    }
}
