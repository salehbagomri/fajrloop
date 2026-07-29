package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
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

private fun formatShortName(fullName: String): String {
    val parts = fullName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return if (parts.size > 2) {
        "${parts[0]} ${parts[1]}..."
    } else {
        fullName
    }
}

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

    val shortName = formatShortName(member.displayName)
    val shortTargetName = if (member.targetName.isNotEmpty()) formatShortName(member.targetName) else "العضو التالي"

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
            // Position badge in chain with admin crown indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(FajrLoopColors.PrimaryContainer)
                    .border(
                        width = if (member.role == "admin") 1.5.dp else 0.dp,
                        color = if (member.role == "admin") FajrLoopColors.Primary else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "#${member.position}",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = FajrLoopColors.Primary
                    )
                    if (member.role == "admin") {
                        Text(
                            text = "👑",
                            fontSize = 8.sp,
                            modifier = Modifier.padding(start = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            // User avatar
            UserAvatar(
                photoUrl = member.photoUrl,
                userName = member.displayName,
                size = 42.dp
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Details Column — Clean layout without text clutter
            Column(modifier = Modifier.weight(1f)) {
                // Member Name (Shortened to first 2 words if longer)
                Text(
                    text = shortName,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (member.isCurrentUser) FajrLoopColors.Primary else FajrLoopColors.TextPrimary
                )

                // Wake Responsibility Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = FajrLoopColors.Primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "مكلف بإيقاظ: $shortTargetName",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = FajrLoopColors.Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status Dot + Status Text
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            onClick = { onRemoveMember(member.uid, shortName) },
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
