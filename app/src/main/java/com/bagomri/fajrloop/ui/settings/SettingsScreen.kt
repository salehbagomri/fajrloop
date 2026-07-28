package com.bagomri.fajrloop.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.DangerButton
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.settings.components.SettingsRow
import com.bagomri.fajrloop.ui.settings.components.SettingsSection
import com.bagomri.fajrloop.ui.settings.dialogs.AlarmSoundDialog
import com.bagomri.fajrloop.ui.settings.dialogs.AlarmTimingDialog
import com.bagomri.fajrloop.ui.settings.dialogs.CalcMethodDialog
import com.bagomri.fajrloop.ui.settings.dialogs.ChallengeSettingsDialog
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun SettingsScreen(
    userCity: String,
    calcMethod: String,
    alarmTimingDesc: String,
    challengeText: String,
    alarmSoundText: String,
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
    onManageHalqasClick: () -> Unit,
    onSaveCalcMethod: (String) -> Unit,
    onSaveAlarmTiming: (String, Int, String) -> Unit,
    onSaveChallenge: (String, String) -> Unit,
    onSaveAlarmSound: (String, String) -> Unit,
    onAutoStartClick: () -> Unit,
    onBatteryClick: () -> Unit,
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
        AnimatedGradientBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            FajrLoopTopBar(
                title = "الإعدادات ⚙️",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Halqa & Travel Mode
                SettingsSection(title = "الحلقة ووضع السفر ✈️") {
                    SettingsRow(
                        title = "وضع السفر",
                        subtitle = travelModeStatus,
                        emoji = "✈️",
                        onClick = onTravelModeClick
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "كود الطوارئ لليوم (TOTP)",
                        subtitle = "رمز مؤقت لإلغاء المنبه في الحالات الطارئة",
                        emoji = "🔑",
                        onClick = onBackupCodeClick
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "إدارة ودائرة الحلقات",
                        subtitle = "تعدد الحلقات والتنقل بينها",
                        emoji = "👥",
                        onClick = onManageHalqasClick
                    )
                }

                // Section 2: Location & Calculation
                SettingsSection(title = "الموقع الجغرافي وحساب المواقيت 🗺️") {
                    SettingsRow(
                        title = "المدينة الحالية (GPS)",
                        subtitle = userCity,
                        emoji = "📍",
                        onClick = onLocationClick
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "طريقة حساب مواقيت الصلاة",
                        valueText = calcMethod,
                        emoji = "🕌",
                        onClick = { showCalcDialog = true }
                    )
                }

                // Section 3: Alarm Customization
                SettingsSection(title = "تخصيص المنبه والتحدي ⏰") {
                    SettingsRow(
                        title = "توقيت رنين المنبه",
                        valueText = alarmTimingDesc,
                        emoji = "⏱️",
                        onClick = { showTimingDialog = true }
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "تحدي الاستيقاظ المفضّل",
                        valueText = challengeText,
                        emoji = "🧩",
                        onClick = { showChallengeDialog = true }
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "صوت ونغمة المنبه",
                        valueText = alarmSoundText,
                        emoji = "🔔",
                        onClick = { showSoundDialog = true }
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "اهتزاز الهاتف أثناء الرنين",
                        isChecked = isVibrateEnabled,
                        onCheckedChange = onVibrateChange,
                        emoji = "📳"
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "عرض أذكار الصباح بعد الإغلاق",
                        isChecked = isAdhkarEnabled,
                        onCheckedChange = onAdhkarChange,
                        emoji = "🌅"
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "إشعار دعاء الفجر اليومي",
                        isChecked = isDuaEnabled,
                        onCheckedChange = onDuaChange,
                        emoji = "🤲"
                    )
                }

                // Section 4: Permissions & Test
                SettingsSection(title = "الصلاحيات وتجربة المنبه 🛡️") {
                    SettingsRow(
                        title = "التشغيل التلقائي (Auto-Start)",
                        subtitle = "ضمان رنين المنبه في أجهزة Xiaomi / Honor / Huawei",
                        emoji = "🚀",
                        onClick = onAutoStartClick
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "تجاهل تحسين البطارية",
                        subtitle = "حماية الخدمة من الإغلاق في الخلفية",
                        emoji = "🔋",
                        onClick = onBatteryClick
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "تجربة منبه تجريبي بعد 10 ثوانٍ",
                        subtitle = "لاختبار شاشة القفل ومستوى الصوت",
                        emoji = "🧪",
                        onClick = onTestAlarmClick
                    )
                }

                // Section 5: Guide & About
                SettingsSection(title = "عن التطبيق والدعم ℹ️") {
                    SettingsRow(
                        title = "دليل الاستخدام",
                        emoji = "📖",
                        onClick = onGuideClick
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "سياسة الخصوصية",
                        emoji = "🔒",
                        onClick = onPrivacyClick
                    )
                    HorizontalDivider(color = FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f))
                    SettingsRow(
                        title = "إصدار التطبيق",
                        valueText = "v1.0.0 (Compose)",
                        emoji = "📱"
                    )
                }

                // Section 6: Logout
                DangerButton(
                    text = "تسجيل الخروج 🚪",
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
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
                initialType = "with",
                initialOffset = 10,
                onSaveTiming = onSaveAlarmTiming,
                onDismiss = { showTimingDialog = false }
            )
        }

        if (showChallengeDialog) {
            ChallengeSettingsDialog(
                initialType = "math",
                initialDifficulty = "medium",
                onSaveChallenge = onSaveChallenge,
                onDismiss = { showChallengeDialog = false }
            )
        }

        if (showSoundDialog) {
            AlarmSoundDialog(
                currentSound = "default",
                onSoundSelect = onSaveAlarmSound,
                onDismiss = { showSoundDialog = false }
            )
        }

        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = { showLogoutConfirm = false },
                title = {
                    Text(
                        text = "تأكيد تسجيل الخروج 🚪",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        color = FajrLoopColors.Gold
                    )
                },
                text = {
                    Text(
                        text = "هل أنت تأكد من تسجيل الخروج؟ سيتوقف المنبه التضامني حتى تعاود تسجيل الدخول.",
                        fontFamily = PpNmArabic,
                        color = FajrLoopColors.TextPrimary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutConfirm = false
                        onLogoutClick()
                    }) {
                        Text("تسجيل الخروج", fontFamily = PpNmArabic, color = FajrLoopColors.DangerRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutConfirm = false }) {
                        Text("إلغاء", fontFamily = PpNmArabic, color = FajrLoopColors.TextSecondary)
                    }
                },
                containerColor = FajrLoopColors.Surface
            )
        }
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    FajrLoopTheme {
        SettingsScreen(
            userCity = "مكة المكرمة",
            calcMethod = "جامعة أم القرى",
            alarmTimingDesc = "مع أذان الفجر بالضبط 🕌",
            challengeText = "حل المعادلة - متوسط",
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
            onManageHalqasClick = {},
            onSaveCalcMethod = {},
            onSaveAlarmTiming = { _, _, _ -> },
            onSaveChallenge = { _, _ -> },
            onSaveAlarmSound = { _, _ -> },
            onAutoStartClick = {},
            onBatteryClick = {},
            onTestAlarmClick = {},
            onGuideClick = {},
            onPrivacyClick = {},
            onLogoutClick = {},
            onBackClick = {}
        )
    }
}
