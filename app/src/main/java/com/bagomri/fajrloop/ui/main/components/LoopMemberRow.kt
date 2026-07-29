package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.MemberStatus
import com.bagomri.fajrloop.ui.components.StatusDot
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

data class HalqaMemberItem(
    val uid: String,
    val displayName: String,
    val photoUrl: String,
    val role: String,
    val responsibleForUserId: String,
    val targetName: String,
    val status: String,
    val isCurrentUser: Boolean,
    val position: Int
)

@Composable
fun LoopMemberRow(
    member: HalqaMemberItem,
    isAdminView: Boolean,
    onConfirmWake: (String) -> Unit,
    onCallClick: () -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onRemoveMember: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val memberStatus = when (member.status) {
        "travel" -> MemberStatus.Travel
        "challenge_done" -> MemberStatus.ChallengeDone
        "awake" -> MemberStatus.Awake
        "panic" -> MemberStatus.Panic
        else -> MemberStatus.Pending
    }

    val statusText = when (member.status) {
        "travel" -> "مسافر"
        "challenge_done" -> "حل التحدي — بانتظار التأكيد"
        "awake" -> "مستيقظ"
        "panic" -> "نداء استغاثة"
        "ringing" -> "يرن المنبه"
        "missed" -> "فاته الفجر"
        else -> "نائم"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs),
        shape = RoundedCornerShape(Radius.md),
        color = FajrLoopColors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position badge in chain
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(FajrLoopColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${member.position}",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FajrLoopColors.Primary
                )
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            // User avatar
            UserAvatar(
                photoUrl = member.photoUrl,
                userName = member.displayName,
                size = 42.dp
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                // Row 1: Name + Role Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.displayName,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (member.isCurrentUser) FajrLoopColors.Primary else FajrLoopColors.TextPrimary
                    )

                    if (member.role == "admin") {
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Box(
                            modifier = Modifier
                                .background(FajrLoopColors.PrimaryContainer, RoundedCornerShape(Spacing.xs))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "مسؤول الحلقة",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = FajrLoopColors.Primary
                            )
                        }
                    }
                }

                // Row 2: Wake Responsibility
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = FajrLoopColors.Primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مكلف بإيقاظ: ${member.targetName.ifEmpty { "العضو التالي" }}",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = FajrLoopColors.Primary
                    )
                }

                // Row 3: Status Dot + Status Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    StatusDot(status = memberStatus, size = 6.dp)
                    Text(
                        text = statusText,
                        fontFamily = PpNmArabic,
                        fontSize = 11.sp,
                        color = FajrLoopColors.TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Quick Action: Confirm Wake
            if (member.status == "challenge_done") {
                Button(
                    onClick = { onConfirmWake(member.uid) },
                    colors = ButtonDefaults.buttonColors(containerColor = FajrLoopColors.Primary),
                    shape = RoundedCornerShape(Radius.sm),
                    contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = Spacing.xs)
                ) {
                    Text("تأكيد", fontFamily = PpNmArabic, color = FajrLoopColors.Background, fontSize = 11.sp)
                }
            } else if (member.status == "panic") {
                Button(
                    onClick = { onCallClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = FajrLoopColors.Danger),
                    shape = RoundedCornerShape(Radius.sm),
                    contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = Spacing.xs)
                ) {
                    Icon(
                        Icons.Outlined.Phone,
                        contentDescription = "اتصل",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text("اتصل", fontFamily = PpNmArabic, color = Color.White, fontSize = 11.sp)
                }
            }

            // Admin Controls: Reorder Up / Down / Delete
            if (isAdminView) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onMoveUp(member.uid) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ArrowUpward,
                            contentDescription = "نقل لأعلى",
                            tint = FajrLoopColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { onMoveDown(member.uid) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ArrowDownward,
                            contentDescription = "نقل لأسفل",
                            tint = FajrLoopColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (!member.isCurrentUser) {
                        IconButton(
                            onClick = { onRemoveMember(member.uid, member.displayName) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "حذف العضو",
                                tint = FajrLoopColors.Danger,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
