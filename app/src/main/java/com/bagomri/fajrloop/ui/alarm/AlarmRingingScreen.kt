package com.bagomri.fajrloop.ui.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrDestructiveButton
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.components.FajrSecondaryButton
import com.bagomri.fajrloop.ui.components.FajrTextField
import com.bagomri.fajrloop.ui.theme.FajrIcons
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun AlarmRingingScreen(
    alarmLabel: String,
    alarmTimeFormatted: String,
    challengeType: String,
    challengeDifficulty: String,
    mathQuestion: String,
    scrambledWord: String,
    shakeCount: Int,
    shakeRequired: Int,
    isChallengeSolved: Boolean,
    isPanicActive: Boolean,
    snoozeCountLeft: Int,
    supervisorName: String,
    supervisorPhone: String,
    onMathSubmit: (Int) -> Unit,
    onWordSubmit: (String) -> Unit,
    onTotpSubmit: (String) -> Unit,
    onSosClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onCallPartnerClick: (String) -> Unit,
    onConfirmWake: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mathInput by remember { mutableStateOf("") }
    var wordInput by remember { mutableStateOf("") }
    var totpInput by remember { mutableStateOf("") }
    var showEmergencyInput by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Mosque icon header
            Icon(
                imageVector = Icons.Outlined.Mosque,
                contentDescription = null,
                tint = FajrLoopColors.Primary,
                modifier = Modifier
                    .size(56.dp)
                    .padding(bottom = Spacing.sm)
            )

            Text(
                text = alarmLabel,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = FajrLoopColors.Primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = alarmTimeFormatted,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = FajrLoopColors.TextPrimary,
                modifier = Modifier.padding(vertical = Spacing.xs)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Main Challenge / Confirmation Card
            FajrCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isChallengeSolved || isPanicActive) {
                        // Waiting Supervisor Confirmation Mode
                        Text(
                            text = if (isPanicActive) "نداء الاستغاثة نشط!" else "تم تجاوز التحدي بنجاح!",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isPanicActive) FajrLoopColors.Danger else FajrLoopColors.Success,
                            modifier = Modifier.padding(bottom = Spacing.sm)
                        )

                        Text(
                            text = if (isPanicActive)
                                "تم تنبيه زملائك في الحلقة لمساعدتك على الاستيقاظ."
                            else
                                "بانتظار تأكيد استيقاظك من زميلك المسؤول: ${supervisorName.ifEmpty { "المسؤول" }}",
                            fontFamily = PpNmArabic,
                            fontSize = 14.sp,
                            color = FajrLoopColors.TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = Spacing.xl)
                        )

                        FajrPrimaryButton(
                            text = "تأكيد الاستيقاظ الفوري",
                            onClick = onConfirmWake,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = Spacing.md)
                        )

                        if (supervisorPhone.isNotEmpty()) {
                            FajrSecondaryButton(
                                text = "الاتصال بزميلك: $supervisorName",
                                onClick = { onCallPartnerClick(supervisorPhone) },
                                leadingIcon = Icons.Outlined.Phone,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Active Challenge Mode
                        when (challengeType) {
                            "shake" -> {
                                Icon(
                                    imageVector = Icons.Outlined.Vibration,
                                    contentDescription = null,
                                    tint = FajrLoopColors.Primary,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(bottom = Spacing.xs)
                                )
                                Text(
                                    text = "تحدي هز الهاتف",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FajrLoopColors.Primary,
                                    modifier = Modifier.padding(bottom = Spacing.xs)
                                )
                                Text(
                                    text = "هز الهاتف $shakeRequired مرة متتالية بقوة لتنبيه جسمك",
                                    fontFamily = PpNmArabic,
                                    fontSize = 13.sp,
                                    color = FajrLoopColors.TextSecondary,
                                    modifier = Modifier.padding(bottom = Spacing.lg)
                                )

                                Text(
                                    text = "$shakeCount / $shakeRequired",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = FajrLoopColors.Primary,
                                    modifier = Modifier.padding(bottom = Spacing.md)
                                )

                                LinearProgressIndicator(
                                    progress = { (shakeCount.toFloat() / shakeRequired.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = FajrLoopColors.Primary,
                                    trackColor = FajrLoopColors.Border
                                )
                            }

                            "word" -> {
                                Icon(
                                    imageVector = Icons.Outlined.Extension,
                                    contentDescription = null,
                                    tint = FajrLoopColors.Primary,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(bottom = Spacing.xs)
                                )
                                Text(
                                    text = "ترتيب الحروف",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FajrLoopColors.Primary,
                                    modifier = Modifier.padding(bottom = Spacing.xs)
                                )
                                Text(
                                    text = "أعد كتابة الكلمة بشكل صحيح لتجاوز المنبه",
                                    fontFamily = PpNmArabic,
                                    fontSize = 13.sp,
                                    color = FajrLoopColors.TextSecondary,
                                    modifier = Modifier.padding(bottom = Spacing.md)
                                )

                                Text(
                                    text = scrambledWord,
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = FajrLoopColors.Primary,
                                    modifier = Modifier.padding(bottom = Spacing.lg)
                                )

                                FajrTextField(
                                    value = wordInput,
                                    onValueChange = { wordInput = it },
                                    placeholder = "أدخل الكلمة هنا...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = Spacing.md)
                                )

                                FajrPrimaryButton(
                                    text = "تحقق من الكلمة",
                                    onClick = {
                                        onWordSubmit(wordInput.trim())
                                        wordInput = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            else -> { // Math
                                Icon(
                                    imageVector = Icons.Outlined.Extension,
                                    contentDescription = null,
                                    tint = FajrLoopColors.Primary,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(bottom = Spacing.xs)
                                )
                                Text(
                                    text = "تحدي الرياضيات",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FajrLoopColors.Primary,
                                    modifier = Modifier.padding(bottom = Spacing.xs)
                                )
                                Text(
                                    text = "حل المسألة الحسابية لإيقاظ عقلك",
                                    fontFamily = PpNmArabic,
                                    fontSize = 13.sp,
                                    color = FajrLoopColors.TextSecondary,
                                    modifier = Modifier.padding(bottom = Spacing.md)
                                )

                                Text(
                                    text = mathQuestion,
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = FajrLoopColors.Primary,
                                    modifier = Modifier.padding(bottom = Spacing.lg)
                                )

                                FajrTextField(
                                    value = mathInput,
                                    onValueChange = { mathInput = it },
                                    placeholder = "أدخل الناتج...",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = Spacing.md)
                                )

                                FajrPrimaryButton(
                                    text = "تحقق من الناتج",
                                    onClick = {
                                        val valInt = mathInput.toIntOrNull()
                                        if (valInt != null) {
                                            onMathSubmit(valInt)
                                            mathInput = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Secondary Action Buttons (SOS & Hidden Emergency TOTP)
            if (!isChallengeSolved && !isPanicActive) {
                FajrDestructiveButton(
                    text = "إرسال نداء استغاثة للحلقة 🚨",
                    onClick = onSosClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                if (!showEmergencyInput) {
                    TextButton(onClick = { showEmergencyInput = true }) {
                        Text(
                            text = "حالة طارئة؟ إدخال كود الطوارئ 🔑",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = FajrLoopColors.TextSecondary
                        )
                    }
                } else {
                    FajrCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.md)
                        ) {
                            Text(
                                text = "رمز الطوارئ المؤقت (TOTP)",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FajrLoopColors.Primary,
                                modifier = Modifier.padding(bottom = Spacing.xs)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FajrTextField(
                                    value = totpInput,
                                    onValueChange = { totpInput = it },
                                    placeholder = "6 أرقام...",
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(Spacing.sm))

                                FajrPrimaryButton(
                                    text = "إلغاء المنبه",
                                    onClick = {
                                        onTotpSubmit(totpInput.trim())
                                        totpInput = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlarmRingingScreenPreview() {
    FajrLoopTheme {
        AlarmRingingScreen(
            alarmLabel = "صلاة الفجر",
            alarmTimeFormatted = "04:30",
            challengeType = "math",
            challengeDifficulty = "medium",
            mathQuestion = "12 + 15 = ?",
            scrambledWord = "ف ج ر",
            shakeCount = 10,
            shakeRequired = 30,
            isChallengeSolved = false,
            isPanicActive = false,
            snoozeCountLeft = 2,
            supervisorName = "أحمد",
            supervisorPhone = "770000000",
            onMathSubmit = {},
            onWordSubmit = {},
            onTotpSubmit = {},
            onSosClick = {},
            onSnoozeClick = {},
            onCallPartnerClick = {},
            onConfirmWake = {}
        )
    }
}
