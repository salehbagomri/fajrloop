package com.bagomri.fajrloop.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.main.components.CountdownCard
import com.bagomri.fajrloop.ui.main.components.FriendWakeAlertCard
import com.bagomri.fajrloop.ui.main.components.QuickActionsGrid
import com.bagomri.fajrloop.ui.main.components.SpiritualContentCard
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

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
        FajrBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))

            // Header Bar
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
                        userName = userName,
                        size = 44.dp
                    )

                    Spacer(modifier = Modifier.width(Spacing.md))

                    Column {
                        Text(
                            text = "أهلاً بك",
                            fontFamily = PpNmArabic,
                            fontSize = 12.sp,
                            color = FajrLoopColors.TextSecondary
                        )
                        Text(
                            text = userName.ifEmpty { "مستخدم" },
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FajrLoopColors.Primary
                        )
                    }
                }

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = FajrIcons.Settings,
                        contentDescription = "الإعدادات",
                        tint = FajrLoopColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Permissions Warning Card
            if (hasPermissionWarning) {
                Surface(
                    onClick = onFixPermissionsClick,
                    shape = RoundedCornerShape(Radius.md),
                    color = FajrLoopColors.Warning.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, FajrLoopColors.Warning.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.lg)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FajrIcons.Warning,
                            contentDescription = null,
                            tint = FajrLoopColors.Warning,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = Spacing.sm)
                        )
                        Text(
                            text = "بعض الصلاحيات ناقصة. اضغط لإعدادها لضمان عمل المنبه.",
                            fontFamily = PpNmArabic,
                            fontSize = 12.sp,
                            color = FajrLoopColors.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Friend Wake Alert Card
            if (friendWakeAlert != null) {
                FriendWakeAlertCard(
                    alert = friendWakeAlert,
                    onConfirmClick = onConfirmFriendWake,
                    modifier = Modifier.padding(bottom = Spacing.xl)
                )
            }

            // Fajr Countdown Card
            CountdownCard(
                fajrTimeStr = fajrTimeStr,
                sunriseTimeStr = sunriseTimeStr,
                countdownText = countdownText,
                countdownColorHex = countdownColorHex,
                borderMode = countdownBorderMode,
                modifier = Modifier.padding(bottom = Spacing.xl)
            )

            // Spiritual Content
            SpiritualContentCard(
                modifier = Modifier.padding(bottom = Spacing.xl)
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

            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    FajrLoopTheme {
        HomeScreen(
            userName = "صالح باقومري",
            userPhotoUrl = "",
            isInHalqa = true,
            fajrTimeStr = "04:30",
            sunriseTimeStr = "05:50",
            countdownText = "03:45:12",
            countdownColorHex = "#D4A54A",
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
