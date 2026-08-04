package com.bagomri.fajrloop.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrDestructiveButton
import com.bagomri.fajrloop.ui.components.FajrDestructiveDialog
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.settings.components.SettingsRow
import com.bagomri.fajrloop.ui.settings.components.SettingsSection
import com.bagomri.fajrloop.ui.settings.dialogs.AlarmSoundDialog
import com.bagomri.fajrloop.ui.settings.dialogs.AlarmTimingDialog
import com.bagomri.fajrloop.ui.settings.dialogs.CalcMethodDialog
import com.bagomri.fajrloop.ui.settings.dialogs.ChallengeSettingsDialog
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun SettingsScreen(
    userCity: String,
    calcMethod: String,
    alarmTimingDesc: String,
    alarmTimingType: String = "with",
    alarmTimingOffset: Int = 0,
    challengeText: String,
    challengeType: String = "math",
    challengeDifficulty: String = "medium",
    alarmSoundText: String,
    alarmSoundCode: String = "default",
    travelModeStatus: String,
    isVibrateEnabled: Boolean,
    isAdhkarEnabled: Boolean,
    isDuaEnabled: Boolean,
    onVibrateChange: (Boolean) -> Unit,
    onAdhkarChange: (Boolean) -> Unit,
    onDuaChange: (Boolean) -> Unit,
    onLocationClick: () -> Unit,
    onTravelModeClick: () -> Unit,
    onBackupCodeClick: () -> Unit,
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
    var showCalcDialog by remember { mutableStateOf(false) }
    var showTimingDialog by remember { mutableStateOf(false) }
    var showChallengeDialog by remember { mutableStateOf(false) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            FajrLoopTopBar(
                title = "الإعدادات",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                // Section 1: Travel & Emergency
                SettingsSection(title = "السفر والطوارئ") {
                    SettingsRow(
                        title = "وضع السفر",
                        subtitle = travelModeStatus,
                        icon = FajrIcons.TravelMode,
                        onClick = onTravelModeClick
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "كود الطوارئ (TOTP)",
                        subtitle = "رمز مؤقت لإيقاف المنبه عند الطوارئ",
                        icon = FajrIcons.EmergencyCode,
                        onClick = onBackupCodeClick
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
                    SettingsRow(
                        title = "توقيت رنين المنبه",
                        subtitle = alarmTimingDesc,
                        icon = FajrIcons.AlarmTiming,
                        onClick = { showTimingDialog = true }
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

                // Section 5: Guide & About
                SettingsSection(title = "عن التطبيق") {
                    SettingsRow(
                        title = "دليل الاستخدام",
                        icon = FajrIcons.Guide,
                        onClick = onGuideClick
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "سياسة الخصوصية",
                        icon = FajrIcons.Privacy,
                        onClick = onPrivacyClick
                    )
                    HorizontalDivider(color = FajrLoopColors.BorderSubtle, thickness = 0.5.dp)
                    SettingsRow(
                        title = "إصدار التطبيق",
                        valueText = "v1.0.0 (Compose)",
                        icon = FajrIcons.AppVersion
                    )
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
            onBackupCodeClick = {},
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
