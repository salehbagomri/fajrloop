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
import androidx.compose.material.icons.outlined.Timer
import com.bagomri.fajrloop.ui.components.FajrSecondaryButton
import com.bagomri.fajrloop.ui.main.components.HalqaMemberItem
import com.bagomri.fajrloop.ui.main.components.LoopMemberRow
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

import androidx.compose.material.icons.filled.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HalqaDetailsSheet(
    halqaName: String,
    members: List<HalqaMemberItem>,
    isAdmin: Boolean,
    isHalqaEffective: Boolean = true,
    onDismiss: () -> Unit,
    onLeaveClick: () -> Unit,
    onTestAlarmClick: () -> Unit,
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

            if (!isHalqaEffective) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.md),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الحلقة تحتاج عضوين على الأقل لتفعيل نظام الإيقاظ المتبادل. ادعُ أصدقاءك!",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

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

            FajrSecondaryButton(
                text = "اختبار منبه الحلقة (رنين بعد دقيقة) 🧪",
                onClick = onTestAlarmClick,
                leadingIcon = Icons.Outlined.Timer,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(Spacing.sm))

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
