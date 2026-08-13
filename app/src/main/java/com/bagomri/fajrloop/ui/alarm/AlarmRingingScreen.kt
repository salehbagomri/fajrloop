package com.bagomri.fajrloop.ui.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.components.FajrSecondaryButton
import com.bagomri.fajrloop.ui.components.FajrTextField
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

const val REQUIRED_PLEDGE_TEXT = "أتعهد بأن أستيقظ لصلاة الفجر الآن والله على ما أقول شهيد"

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
    snoozeCountLeft: Int,
    supervisorName: String,
    onMathSubmit: (Int) -> Unit,
    onWordSubmit: (String) -> Unit,
    onPledgeSubmit: (String) -> Unit,
    onSnoozeClick: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    var mathInput by remember { mutableStateOf("") }
    var wordInput by remember { mutableStateOf("") }
    var pledgeInput by remember { mutableStateOf("") }

    // Pulsing animation for the Mosque Header Icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Spacing.xl))

                // Pulsing Mosque Header Icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                ) {
                    Surface(
                        color = FajrLoopColors.PrimaryContainer,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .size(72.dp)
                            .scale(pulseScale)
                    ) {}

                    Icon(
                        imageVector = Icons.Outlined.Mosque,
                        contentDescription = null,
                        tint = FajrLoopColors.Primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

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
                    fontSize = 44.sp,
                    color = FajrLoopColors.TextPrimary,
                    modifier = Modifier.padding(vertical = Spacing.xs)
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // Main Challenge OR Spiritual Pledge Card
                FajrCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isChallengeSolved) {
                            // Spiritual Pledge & Supplication Mode
                            Text(
                                text = "🎉 تم تجاوز التحدي بنجاح!",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = FajrLoopColors.Primary,
                                modifier = Modifier.padding(bottom = Spacing.xs)
                            )

                            Text(
                                text = "« الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ »",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = FajrLoopColors.PrimaryMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = Spacing.lg)
                            )

                            if (supervisorName.isNotEmpty()) {
                                Surface(
                                    color = FajrLoopColors.PrimaryContainer,
                                    shape = RoundedCornerShape(Radius.md),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = Spacing.md)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(Spacing.md),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = null,
                                            tint = FajrLoopColors.Primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.xs))
                                        Text(
                                            text = "إذا تأخر صديقك ($supervisorName) عن الاستيقاظ، يمكنك كتابة التعهد والقيام لصلاة الفجر الآن 🌅",
                                            fontFamily = PpNmArabic,
                                            fontSize = 12.sp,
                                            color = FajrLoopColors.TextPrimary
                                        )
                                    }
                                }
                            }

                            Surface(
                                color = FajrLoopColors.SurfaceVariant,
                                shape = RoundedCornerShape(Radius.md),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = Spacing.md)
                            ) {
                                Column(modifier = Modifier.padding(Spacing.md)) {
                                    Text(
                                        text = "عبارة التعهد الإيماني المطلوب كتابتها لتأكيد القيام:",
                                        fontFamily = PpNmArabic,
                                        fontSize = 12.sp,
                                        color = FajrLoopColors.TextSecondary,
                                        modifier = Modifier.padding(bottom = Spacing.xs)
                                    )
                                    Text(
                                        text = "« $REQUIRED_PLEDGE_TEXT »",
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = FajrLoopColors.Primary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            FajrTextField(
                                value = pledgeInput,
                                onValueChange = { pledgeInput = it },
                                placeholder = "اكتب عبارة التعهد هنا بنفسك...",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = Spacing.md)
                            )

                            FajrPrimaryButton(
                                text = "أشهد الله وأتعهد بالقيام للصلاة 🕌",
                                onClick = {
                                    onPledgeSubmit(pledgeInput)
                                    pledgeInput = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Active Challenge Mode
                            when (challengeType) {
                                "shake" -> {
                                    Icon(
                                        imageVector = Icons.Outlined.Vibration,
                                        contentDescription = null,
                                        tint = FajrLoopColors.Primary,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(bottom = Spacing.xs)
                                    )
                                    Text(
                                        text = "تحدي هز الهاتف 📱",
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = FajrLoopColors.Primary,
                                        modifier = Modifier.padding(bottom = Spacing.xs)
                                    )
                                    Text(
                                        text = "هز الهاتف $shakeRequired مرة متتالية لتنشيط جسمك",
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
                                            .size(36.dp)
                                            .padding(bottom = Spacing.xs)
                                    )
                                    Text(
                                        text = "ترتيب الحروف 🧩",
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = FajrLoopColors.Primary,
                                        modifier = Modifier.padding(bottom = Spacing.xs)
                                    )
                                    Text(
                                        text = "أعد كتابة الكلمة بشكل صحيح لتجاوز التحدي",
                                        fontFamily = PpNmArabic,
                                        fontSize = 13.sp,
                                        color = FajrLoopColors.TextSecondary,
                                        modifier = Modifier.padding(bottom = Spacing.md)
                                    )

                                    Text(
                                        text = scrambledWord,
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
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
                                        text = "تحقق من الكلمة ⚡",
                                        onClick = {
                                            onWordSubmit(wordInput.trim())
                                            wordInput = ""
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                else -> { // Math Challenge (Default)
                                    Icon(
                                        imageVector = Icons.Outlined.Extension,
                                        contentDescription = null,
                                        tint = FajrLoopColors.Primary,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .padding(bottom = Spacing.xs)
                                    )
                                    Text(
                                        text = "تحدي الرياضيات 🧮",
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = FajrLoopColors.Primary,
                                        modifier = Modifier.padding(bottom = Spacing.xs)
                                    )
                                    Text(
                                        text = "حل المسألة الحسابية لإيقاظ عقلك وجسمك",
                                        fontFamily = PpNmArabic,
                                        fontSize = 13.sp,
                                        color = FajrLoopColors.TextSecondary,
                                        modifier = Modifier.padding(bottom = Spacing.md)
                                    )

                                    Text(
                                        text = mathQuestion,
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp,
                                        color = FajrLoopColors.Primary,
                                        modifier = Modifier.padding(bottom = Spacing.lg)
                                    )

                                    FajrTextField(
                                        value = mathInput,
                                        onValueChange = { mathInput = it },
                                        placeholder = "أدخل الناتج هنا...",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = Spacing.md)
                                    )

                                    FajrPrimaryButton(
                                        text = "تحقق من الناتج ⚡",
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

                // Optional Snooze Button
                if (!isChallengeSolved && snoozeCountLeft > 0) {
                    FajrSecondaryButton(
                        text = "غفوة (5 دقائق) — متبقي $snoozeCountLeft",
                        onClick = onSnoozeClick,
                        modifier = Modifier.fillMaxWidth()
                    )
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
            challengeDifficulty = "easy",
            mathQuestion = "5 + 7 = ?",
            scrambledWord = "ف ج ر",
            shakeCount = 10,
            shakeRequired = 30,
            isChallengeSolved = true,
            snoozeCountLeft = 2,
            supervisorName = "أحمد",
            onMathSubmit = {},
            onWordSubmit = {},
            onPledgeSubmit = {},
            onSnoozeClick = {}
        )
    }
}
