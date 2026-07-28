package com.bagomri.fajrloop.ui.main.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.DangerButton
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.main.components.HalqaMemberItem
import com.bagomri.fajrloop.ui.main.components.LoopMemberRow
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalqaDetailsSheet(
    halqaName: String,
    members: List<HalqaMemberItem>,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onLeaveClick: () -> Unit,
    onConfirmWake: (String) -> Unit,
    onCallClick: () -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onRemoveMember: (String, String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F0F29),
        contentColor = FajrLoopColors.TextPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "تفاصيل حلقة \"$halqaName\" 👥",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Gold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(members) { member ->
                    LoopMemberRow(
                        member = member,
                        isAdminView = isAdmin,
                        onConfirmWake = onConfirmWake,
                        onCallClick = onCallClick,
                        onMoveUp = onMoveUp,
                        onMoveDown = onMoveDown,
                        onRemoveMember = onRemoveMember
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DangerButton(
                text = "مغادرة الحلقة 🚪",
                onClick = onLeaveClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
