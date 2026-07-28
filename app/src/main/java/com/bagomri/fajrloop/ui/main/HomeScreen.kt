package com.bagomri.fajrloop.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.main.FriendWakeAlert
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.main.components.CountdownCard
import com.bagomri.fajrloop.ui.main.components.FriendWakeAlertCard
import com.bagomri.fajrloop.ui.main.components.QuickActionsGrid
import com.bagomri.fajrloop.ui.main.components.SpiritualContentCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun HomeScreen(
    userName: String,
    userPhotoUrl: String,
    isInHalqa: Boolean,
    fajrTimeStr: String,
    sunriseTimeStr: String,
    countdownText: String,
    countdownColorHex: String,
    countdownBorderMode: Int,
    friendWakeAlert: FriendWakeAlert?,
    hasPermissionWarning: Boolean,
    onSettingsClick: () -> Unit,
    onHalqaDetailsClick: () -> Unit,
    onChatClick: () -> Unit,
    onStatsClick: () -> Unit,
    onInviteClick: () -> Unit,
    onCreateHalqaClick: () -> Unit,
    onJoinHalqaClick: () -> Unit,
    onConfirmFriendWake: (String) -> Unit,
    onFixPermissionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar (Greeting, User Avatar, Settings Icon)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    UserAvatar(
                        photoUrl = userPhotoUrl,
                        size = 44.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "أهلاً بك 👋",
                            fontFamily = PpNmArabic,
                            fontSize = 12.sp,
                            color = FajrLoopColors.TextSecondary
                        )
                        Text(
                            text = userName.ifEmpty { "مستخدم جديد" },
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FajrLoopColors.Gold
                        )
                    }
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .clip(CircleShape)
                ) {
                    Text("⚙️", fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permissions Warning Card (if needed)
            if (hasPermissionWarning) {
                Surface(
                    onClick = onFixPermissionsClick,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                    color = FajrLoopColors.DangerRed.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FajrLoopColors.DangerRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                        Text(
                            text = "تنبه! بعض الصلاحيات لا تزال ناقصة، قد لا يرن المنبه بدقة. اضغط للضبط الآن.",
                            fontFamily = PpNmArabic,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Friend Wake Alert Card (if active)
            if (friendWakeAlert != null) {
                FriendWakeAlertCard(
                    alert = friendWakeAlert,
                    onConfirmClick = onConfirmFriendWake,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }

            // Fajr Countdown Card
            CountdownCard(
                fajrTimeStr = fajrTimeStr,
                sunriseTimeStr = sunriseTimeStr,
                countdownText = countdownText,
                countdownColorHex = countdownColorHex,
                borderMode = countdownBorderMode,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Spiritual Content (Ayah / Hadith)
            SpiritualContentCard(
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Quick Actions Grid
            QuickActionsGrid(
                isInHalqa = isInHalqa,
                onHalqaDetailsClick = onHalqaDetailsClick,
                onChatClick = onChatClick,
                onStatsClick = onStatsClick,
                onInviteClick = onInviteClick,
                onCreateHalqaClick = onCreateHalqaClick,
                onJoinHalqaClick = onJoinHalqaClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    FajrLoopTheme {
        HomeScreen(
            userName = "صالح باقومري",
            userPhotoUrl = "",
            isInHalqa = true,
            fajrTimeStr = "04:30",
            sunriseTimeStr = "05:50",
            countdownText = "03:45:12",
            countdownColorHex = "#FFD700",
            countdownBorderMode = 1,
            friendWakeAlert = null,
            hasPermissionWarning = false,
            onSettingsClick = {},
            onHalqaDetailsClick = {},
            onChatClick = {},
            onStatsClick = {},
            onInviteClick = {},
            onCreateHalqaClick = {},
            onJoinHalqaClick = {},
            onConfirmFriendWake = {},
            onFixPermissionsClick = {}
        )
    }
}
