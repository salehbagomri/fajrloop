package com.bagomri.fajrloop.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.DangerButton
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic

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

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Mosque icon header
            Text(
                text = "🕌",
                fontSize = 64.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = alarmLabel,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = FajrLoopColors.Gold,
                textAlign = TextAlign.Center
            )

            Text(
                text = alarmTimeFormatted,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = FajrLoopColors.TextPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Challenge / Confirmation Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isChallengeSolved || isPanicActive) {
                        // Waiting Supervisor Confirmation Mode
                        Text(
                            text = if (isPanicActive) "🚨 نداء الاستغاثة نشط!" else "🎉 تم تجاوز التحدي بنجاح!",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isPanicActive) FajrLoopColors.DangerRed else FajrLoopColors.SuccessGreen,
                            modifier = Modifier.padding(bottom = 8.dp)
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
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        GoldButton(
                            text = "تأكيد الاستيقاظ الفوري 🌅",
                            onClick = onConfirmWake,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        )

                        if (supervisorPhone.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { onCallPartnerClick(supervisorPhone) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FajrLoopColors.Gold)
                            ) {
                                Text(
                                    text = "الاتصال بزميلك: $supervisorName 📞",
                                    fontFamily = PpNmArabic,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        // Active Challenge Mode
                        when (challengeType) {
                            "shake" -> {
                                Text(
                                    text = "تحدي هز الهاتف 📱",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FajrLoopColors.Gold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "هز الهاتف $shakeRequired مرة متتالية بقوة لتنبيه جسمك",
                                    fontFamily = PpNmArabic,
                                    fontSize = 13.sp,
                                    color = FajrLoopColors.TextSecondary,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                    text = "$shakeCount / $shakeRequired",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = FajrLoopColors.Gold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                LinearProgressIndicator(
                                    progress = { (shakeCount.toFloat() / shakeRequired.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp),
                                    color = FajrLoopColors.Gold,
                                    trackColor = FajrLoopColors.SurfaceBorder
                                )
                            }

                            "word" -> {
                                Text(
                                    text = "ترتيب الحروف 🧩",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FajrLoopColors.Gold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "أعد كتابة الكلمة بشكل صحيح لتجاوز المنبه",
                                    fontFamily = PpNmArabic,
                                    fontSize = 13.sp,
                                    color = FajrLoopColors.TextSecondary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Text(
                                    text = scrambledWord,
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = FajrLoopColors.Gold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                OutlinedTextField(
                                    value = wordInput,
                                    onValueChange = { wordInput = it },
                                    placeholder = { Text("أدخل الكلمة هنا...", fontFamily = PpNmArabic) },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )

                                GoldButton(
                                    text = "تحقق من الكلمة",
                                    onClick = {
                                        onWordSubmit(wordInput.trim())
                                        wordInput = ""
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            else -> { // Math
                                Text(
                                    text = "تحدي الرياضيات 🧮",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FajrLoopColors.Gold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "حل المسألة الحسابية لإيقاظ عقلك",
                                    fontFamily = PpNmArabic,
                                    fontSize = 13.sp,
                                    color = FajrLoopColors.TextSecondary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Text(
                                    text = mathQuestion,
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = FajrLoopColors.Gold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                OutlinedTextField(
                                    value = mathInput,
                                    onValueChange = { mathInput = it },
                                    placeholder = { Text("أدخل الناتج...", fontFamily = PpNmArabic) },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )

                                GoldButton(
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

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Action Buttons (SOS, Snooze, Emergency TOTP)
            if (!isChallengeSolved && !isPanicActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (snoozeCountLeft > 0) {
                        OutlinedButton(
                            onClick = onSnoozeClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FajrLoopColors.Gold)
                        ) {
                            Text(
                                text = "غفوة ($snoozeCountLeft) ⏰",
                                fontFamily = PpNmArabic,
                                fontSize = 13.sp
                            )
                        }
                    }

                    DangerButton(
                        text = "استغاثة 🚨",
                        onClick = onSosClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TOTP Backup Code Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = totpInput,
                            onValueChange = { totpInput = it },
                            placeholder = { Text("كود الطوارئ 🔑", fontFamily = PpNmArabic, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                onTotpSubmit(totpInput.trim())
                                totpInput = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FajrLoopColors.Gold),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إلغاء 🔑", fontFamily = PpNmArabic, color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AlarmRingingScreenPreview() {
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
