package com.bagomri.fajrloop.ui.main.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrDestructiveButton
import com.bagomri.fajrloop.ui.main.components.HalqaMemberItem
import com.bagomri.fajrloop.ui.main.components.LoopMemberRow
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
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
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
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
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "تفاصيل حلقة «$halqaName»",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = FajrLoopColors.Primary,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            // Explanatory Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md)
                    .background(FajrLoopColors.PrimaryContainer.copy(alpha = 0.4f), RoundedCornerShape(Radius.md))
                    .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(Radius.md))
                    .padding(Spacing.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = FajrLoopColors.Primary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = Spacing.xs)
                    )
                    Text(
                        text = if (isAdmin)
                            "سلسلة الاستيقاظ دائرية: كل عضو مُكلف بإيقاظ العضو التالي. يمكنك إعادة الترتيب بواسطة أسهم التوجيه."
                        else
                            "سلسلة الاستيقاظ دائرية: كل عضو في الحلقة مُكلف بإيقاظ العضو الذي يليه في القائمة.",
                        fontFamily = PpNmArabic,
                        fontSize = 12.sp,
                        color = FajrLoopColors.TextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                itemsIndexed(members) { index, member ->
                    LoopMemberRow(
                        member = member,
                        isAdminView = isAdmin,
                        onConfirmWake = onConfirmWake,
                        onCallClick = onCallClick,
                        onMoveUp = { onMoveUp(index) },
                        onMoveDown = { onMoveDown(index) },
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
