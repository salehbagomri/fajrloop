package com.bagomri.fajrloop.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrDestructiveButton
import com.bagomri.fajrloop.ui.components.FajrDestructiveDialog
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.settings.components.SettingsRow
import com.bagomri.fajrloop.ui.settings.components.SettingsSection
import com.bagomri.fajrloop.ui.settings.dialogs.*
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.Spacing
import com.bagomri.fajrloop.utils.AppInfoUtils

@Composable
fun SettingsScreen(
    userCity: String,
    calcMethod: String,
    alarmTimingDesc: String,
    alarmTimingType: String = "with",
    alarmTimingOffset: Int = 0,
    challengeText: String,
    challengeType: String = "math",
    challengeDifficulty: String = "easy",
    alarmSoundText: String,
    alarmSoundCode: String = "default",
    travelModeStatus: String,
    isVibrateEnabled: Boolean,
    isAdhkarEnabled: Boolean,
    isDuaEnabled: Boolean,
    isAdmin: Boolean = false,
    isInHalqa: Boolean = false,
    onVibrateChange: (Boolean) -> Unit,
    onAdhkarChange: (Boolean) -> Unit,
    onDuaChange: (Boolean) -> Unit,
    onLocationClick: () -> Unit,
    onTravelModeClick: () -> Unit,
    onSaveCalcMethod: (String) -> Unit,
    onSaveAlarmTiming: (String, Int, String) -> Unit,
    onSaveChallenge: (String, String) -> Unit,
    onSaveAlarmSound: (String, String) -> Unit,
    onPermissionsManageClick: () -> Unit,
    onTestAlarmClick: () -> Unit,
    onGuideClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCalcDialog by remember { mutableStateOf(false) }
    var showTimingDialog by remember { mutableStateOf(false) }
    var showChallengeDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showNonAdminNotice by remember { mutableStateOf(false) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeveloperDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var isAboutExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            FajrLoopTopBar(
                title = "الإعدادات",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Section 1: Travel
                SettingsSection(title = "السفر والمرونة") {
                    SettingsRow(
                        title = "وضع السفر",
                        subtitle = travelModeStatus,
                        icon = FajrIcons.TravelMode,
                        onClick = onTravelModeClick
                    )
                }

                // Section 2: Location & Calculation
                SettingsSection(title = "الموقع والمواقيت") {
                    SettingsRow(
                        title = "المدينة الحالية (GPS)",
                        subtitle = userCity,
                        icon = FajrIcons.Location,
                        onClick = onLocationClick
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "طريقة حساب مواقيت الصلاة",
                        subtitle = calcMethod,
                        icon = FajrIcons.PrayerCalc,
                        onClick = { showCalcDialog = true }
                    )
                }

                // Section 3: Alarm Customization
                SettingsSection(title = "المنبه والتحدي") {
                    val timingSubtitle = if (isInHalqa && !isAdmin) "$alarmTimingDesc (موحّد للحلقة 🔒)" else alarmTimingDesc
                    SettingsRow(
                        title = "توقيت رنين المنبه",
                        subtitle = timingSubtitle,
                        icon = FajrIcons.AlarmTiming,
                        onClick = {
                            if (isInHalqa && !isAdmin) {
                                showNonAdminNotice = true
                            } else {
                                showTimingDialog = true
                            }
                        }
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "تحدي الاستيقاظ المفضّل",
                        subtitle = challengeText,
                        icon = FajrIcons.Challenge,
                        onClick = { showChallengeDialog = true }
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "صوت ونغمة المنبه",
                        subtitle = alarmSoundText,
                        icon = FajrIcons.AlarmSound,
                        onClick = { showSoundDialog = true }
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "اهتزاز الهاتف أثناء الرنين",
                        isChecked = isVibrateEnabled,
                        onCheckedChange = onVibrateChange,
                        icon = FajrIcons.Vibration
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "عرض أذكار الصباح بعد الإغلاق",
                        isChecked = isAdhkarEnabled,
                        onCheckedChange = onAdhkarChange,
                        icon = FajrIcons.MorningAdhkar
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "إشعار دعاء الفجر اليومي",
                        isChecked = isDuaEnabled,
                        onCheckedChange = onDuaChange,
                        icon = FajrIcons.DuaNotification
                    )
                }

                // Section 4: Permissions & System
                SettingsSection(title = "الصلاحيات والنظام") {
                    SettingsRow(
                        title = "فحص وإدارة جميع الصلاحيات",
                        subtitle = "مراجعة وتفعيل كافة صلاحيات المنبه والبطارية",
                        icon = FajrIcons.Battery,
                        onClick = onPermissionsManageClick
                    )
                }

                // Section 5: Expandable About Section
                SettingsSection(title = "عن التطبيق") {
                    SettingsRow(
                        title = "عن التطبيق والمعلومات",
                        subtitle = "المعلومات، الدليل، السياسة، والمطور",
                        icon = FajrIcons.AboutApp,
                        trailingIcon = if (isAboutExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        onClick = { isAboutExpanded = !isAboutExpanded }
                    )

                    AnimatedVisibility(visible = isAboutExpanded) {
                        Column {
                            HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                            SettingsRow(
                                title = "دليل الاستخدام الشامل",
                                icon = FajrIcons.Guide,
                                onClick = onGuideClick
                            )
                            HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                            SettingsRow(
                                title = "سياسة الخصوصية الأمان",
                                icon = FajrIcons.Privacy,
                                onClick = onPrivacyClick
                            )
                            HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                            SettingsRow(
                                title = "المطور والدعم الفني",
                                subtitle = "Saleh Bagomri",
                                icon = FajrIcons.Developer,
                                onClick = { showDeveloperDialog = true }
                            )
                            HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                            val versionName = AppInfoUtils.getAppVersionName(context)
                            val versionCode = AppInfoUtils.getAppVersionCode(context)
                            SettingsRow(
                                title = "حول التطبيق ورقم الإصدار",
                                subtitle = "v$versionName (البناء $versionCode)",
                                icon = FajrIcons.AppVersion,
                                onClick = { showAboutDialog = true }
                            )
                        }
                    }
                }

                // Section 6: Logout
                FajrDestructiveButton(
                    text = "تسجيل الخروج",
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = FajrIcons.Logout
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }

        // Dialogs
        if (showCalcDialog) {
            CalcMethodDialog(
                currentMethod = calcMethod,
                onMethodSelect = onSaveCalcMethod,
                onDismiss = { showCalcDialog = false }
            )
        }

        if (showTimingDialog) {
            AlarmTimingDialog(
                initialType = alarmTimingType,
                initialOffset = alarmTimingOffset,
                onSaveTiming = onSaveAlarmTiming,
                onDismiss = { showTimingDialog = false }
            )
        }

        if (showNonAdminNotice) {
            AlertDialog(
                onDismissRequest = { showNonAdminNotice = false },
                title = { Text("توقيت المنبه موحّد للحلقة 🔒", color = FajrLoopColors.TextPrimary) },
                text = { Text("توقيت رنين المنبه موحّد لجميع أعضاء الحلقة ويتم تحديده بواسطة مسؤول الحلقة لضمان استيقاظ الجميع في نفس اللحظة.", color = FajrLoopColors.TextSecondary) },
                confirmButton = {
                    TextButton(onClick = { showNonAdminNotice = false }) {
                        Text("حسناً", color = FajrLoopColors.Primary)
                    }
                },
                containerColor = FajrLoopColors.Surface
            )
        }

        if (showPrivacyDialog) {
            PrivacyPolicyDialog(
                onDismiss = { showPrivacyDialog = false }
            )
        }

        if (showDeveloperDialog) {
            DeveloperInfoDialog(
                onDismiss = { showDeveloperDialog = false }
            )
        }

        if (showAboutDialog) {
            AboutAppDialog(
                onDismiss = { showAboutDialog = false }
            )
        }

        if (showChallengeDialog) {
            ChallengeSettingsDialog(
                initialType = challengeType,
                initialDifficulty = challengeDifficulty,
                onSaveChallenge = onSaveChallenge,
                onDismiss = { showChallengeDialog = false }
            )
        }

        if (showSoundDialog) {
            AlarmSoundDialog(
                currentSound = alarmSoundCode,
                onSoundSelect = onSaveAlarmSound,
                onDismiss = { showSoundDialog = false }
            )
        }

        if (showLogoutConfirm) {
            FajrDestructiveDialog(
                title = "تسجيل الخروج",
                message = "هل تريد تسجيل الخروج؟ سيتوقف المنبه حتى تسجّل الدخول مجدداً.",
                confirmText = "تسجيل الخروج",
                dismissText = "إلغاء",
                onConfirm = {
                    showLogoutConfirm = false
                    onLogoutClick()
                },
                onDismiss = { showLogoutConfirm = false }
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    FajrLoopTheme {
        SettingsScreen(
            userCity = "مكة المكرمة",
            calcMethod = "جامعة أم القرى (مكة المكرمة)",
            alarmTimingDesc = "مع أذان الفجر بالضبط",
            challengeText = "معادلة حسابية - متوسط",
            alarmSoundText = "افتراضي",
            travelModeStatus = "غير نشط",
            isVibrateEnabled = true,
            isAdhkarEnabled = true,
            isDuaEnabled = true,
            onVibrateChange = {},
            onAdhkarChange = {},
            onDuaChange = {},
            onLocationClick = {},
            onTravelModeClick = {},
            onSaveCalcMethod = {},
            onSaveAlarmTiming = { _, _, _ -> },
            onSaveChallenge = { _, _ -> },
            onSaveAlarmSound = { _, _ -> },
            onPermissionsManageClick = {},
            onTestAlarmClick = {},
            onGuideClick = {},
            onPrivacyClick = {},
            onLogoutClick = {},
            onBackClick = {}
        )
    }
}
