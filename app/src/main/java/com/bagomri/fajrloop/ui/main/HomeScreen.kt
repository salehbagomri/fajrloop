package com.bagomri.fajrloop.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTravelActive = remember(context) { com.bagomri.fajrloop.alarm.TravelModeManager.isTravelModeActive(context) }
    val travelStatusText = remember(context) { com.bagomri.fajrloop.alarm.TravelModeManager.getTravelModeStatusText(context) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            FajrBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar — Padded away from status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm),
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
                        size = 52.dp
                    )

                    Spacer(modifier = Modifier.width(Spacing.md))

                    Column {
                        Text(
                            text = "أهلاً بك 🖐️",
                            fontFamily = PpNmArabic,
                            fontSize = 13.sp,
                            color = FajrLoopColors.TextSecondary
                        )
                        Text(
                            text = userName.ifEmpty { "مستخدم" },
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = FajrLoopColors.Primary
                        )
                    }
                }

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = FajrIcons.Settings,
                        contentDescription = "الإعدادات",
                        tint = FajrLoopColors.TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Travel Mode Banner (If Active)
            if (isTravelActive) {
                Surface(
                    onClick = onSettingsClick,
                    shape = RoundedCornerShape(Radius.md),
                    color = FajrLoopColors.PrimaryContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, FajrLoopColors.Primary.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.md)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FlightTakeoff,
                            contentDescription = null,
                            tint = FajrLoopColors.Primary,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = Spacing.sm)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✈️ وضع السفر نشط - المنبه متوقف مؤقتاً",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FajrLoopColors.Primary
                            )
                            Text(
                                text = travelStatusText,
                                fontFamily = PpNmArabic,
                                fontSize = 11.sp,
                                color = FajrLoopColors.TextSecondary
                            )
                        }
                    }
                }
            }

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
                                .size(22.dp)
                                .padding(end = Spacing.sm)
                        )
                        Text(
                            text = "بعض الصلاحيات ناقصة. اضغط لإعدادها لضمان عمل المنبه.",
                            fontFamily = PpNmArabic,
                            fontSize = 13.sp,
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
