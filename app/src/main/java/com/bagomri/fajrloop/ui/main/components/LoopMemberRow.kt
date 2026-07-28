package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

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
    val statusEmoji = when (member.status) {
        "travel" -> "✈️ مسافر"
        "challenge_done" -> "⏳ حل التحدي — بانتظار صديقه"
        "awake" -> "✅ مستيقظ ومؤكد"
        "panic" -> "🚨 نداء استغاثة"
        "ringing" -> "🔔 يرن المنبه"
        "missed" -> "❌ فاته الفجر"
        else -> "💤 نائم"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(FajrLoopColors.Gold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${member.position}",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FajrLoopColors.Gold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // User avatar
            UserAvatar(
                photoUrl = member.photoUrl,
                size = 40.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Details (Name, Admin badge, Status & target)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.displayName,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (member.isCurrentUser) FajrLoopColors.Gold else FajrLoopColors.TextPrimary
                    )

                    if (member.role == "admin") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(FajrLoopColors.Gold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "مسؤول",
                                fontFamily = PpNmArabic,
                                fontSize = 9.sp,
                                color = FajrLoopColors.Gold
                            )
                        }
                    }
                }

                Text(
                    text = "يوقظ: ${member.targetName}\n$statusEmoji",
                    fontFamily = PpNmArabic,
                    fontSize = 11.sp,
                    color = FajrLoopColors.TextSecondary
                )
            }

            // Quick action buttons (Confirm wake, Call SOS)
            if (member.status == "challenge_done") {
                Button(
                    onClick = { onConfirmWake(member.uid) },
                    colors = ButtonDefaults.buttonColors(containerColor = FajrLoopColors.Gold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("تأكيد ✓", fontFamily = PpNmArabic, color = Color.Black, fontSize = 11.sp)
                }
            } else if (member.status == "panic") {
                Button(
                    onClick = { onCallClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = FajrLoopColors.DangerRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("اتصل 📞", fontFamily = PpNmArabic, color = Color.White, fontSize = 11.sp)
                }
            }

            // Admin reorder & remove controls
            if (isAdminView) {
                Row {
                    IconButton(onClick = { onMoveUp(member.uid) }) {
                        Text("▲", color = FajrLoopColors.Gold, fontSize = 12.sp)
                    }
                    IconButton(onClick = { onMoveDown(member.uid) }) {
                        Text("▼", color = FajrLoopColors.Gold, fontSize = 12.sp)
                    }
                    if (!member.isCurrentUser) {
                        IconButton(onClick = { onRemoveMember(member.uid, member.displayName) }) {
                            Text("🗑️", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
