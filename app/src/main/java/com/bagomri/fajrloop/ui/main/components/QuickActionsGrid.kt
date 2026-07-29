package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

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
        if (isInHalqa) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                ActionCard(
                    icon = FajrIcons.Group,
                    title = "تفاصيل الحلقة",
                    onClick = onHalqaDetailsClick,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = FajrIcons.Chat,
                    title = "محادثة الحلقة",
                    onClick = onChatClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                ActionCard(
                    icon = FajrIcons.Stats,
                    title = "سجل التزامي",
                    onClick = onStatsClick,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = FajrIcons.InviteFriend,
                    title = "دعوة أصدقاء",
                    onClick = onInviteClick,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                ActionCard(
                    icon = FajrIcons.CreateHalqa,
                    title = "إنشاء حلقة",
                    onClick = onCreateHalqaClick,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = FajrIcons.JoinWithCode,
                    title = "الانضمام بكود",
                    onClick = onJoinHalqaClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            ActionCard(
                icon = FajrIcons.Stats,
                title = "سجل التزامي",
                onClick = onStatsClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FajrCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = FajrLoopColors.Primary,
                modifier = Modifier
                    .size(22.dp)
                    .padding(end = Spacing.sm)
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
