package com.bagomri.fajrloop.ui.main.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrDestructiveButton
import com.bagomri.fajrloop.ui.main.components.HalqaMemberItem
import com.bagomri.fajrloop.ui.main.components.LoopMemberRow
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

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
        containerColor = FajrLoopColors.Surface,
        contentColor = FajrLoopColors.TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "تفاصيل حلقة «$halqaName»",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Primary,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
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

            Spacer(modifier = Modifier.height(Spacing.lg))

            FajrDestructiveButton(
                text = "مغادرة الحلقة",
                onClick = onLeaveClick,
                leadingIcon = FajrIcons.Logout,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}
