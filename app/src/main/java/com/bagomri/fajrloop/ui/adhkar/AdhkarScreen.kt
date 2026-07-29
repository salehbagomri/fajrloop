package com.bagomri.fajrloop.ui.adhkar

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DhikrItem(
    val title: String,
    val text: String,
    val targetCount: Int,
    val reward: String = ""
)

val defaultAdhkarList = listOf(
    DhikrItem(
        title = "آية الكرسي",
        text = "أعوذ بالله من الشيطان الرجيم: {اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَؤُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ}",
        targetCount = 1,
        reward = "من قالها حين يصبح أُجير من الجن حتى يمسي"
    ),
    DhikrItem(
        title = "سورة الإخلاص",
        text = "بسم الله الرحمن الرحيم: {قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ}",
        targetCount = 3,
        reward = "تكفيك من كل شيء"
    ),
    DhikrItem(
        title = "سورة الفلق",
        text = "بسم الله الرحمن الرحيم: {قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ * مِن شَرِّ مَا خَلَقَ * وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ * وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ * وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ}",
        targetCount = 3,
        reward = "التحصين والوقاية من الشرور"
    ),
    DhikrItem(
        title = "سورة الناس",
        text = "بسم الله الرحمن الرحيم: {قُلْ أَعُوذُ بِرَبِّ النَّاسِ * مَلِكِ النَّاسِ * إِلَٰهِ النَّاسِ * مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ * الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ * مِنَ الْجِنَّةِ وَالنَّاسِ}",
        targetCount = 3,
        reward = "التحصين من الوسواس والشرور"
    ),
    DhikrItem(
        title = "أذكار الصباح",
        text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ، وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ.",
        targetCount = 1,
        reward = "دعاء بداية اليوم والبركة فيه"
    ),
    DhikrItem(
        title = "دعاء الإصباح",
        text = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ.",
        targetCount = 1
    ),
    DhikrItem(
        title = "سيد الاستغفار",
        text = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إلاَّ أَنْتَ.",
        targetCount = 1,
        reward = "من قالها موقناً بها فمات من يومه دخل الجنة"
    ),
    DhikrItem(
        title = "الإشهاد والتوحيد",
        text = "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلاَئِكَتَكَ، وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لاَ إِلَهَ إلاَّ أَنْتَ وَحْدَهُ لاَ شَرِيكَ لَهُ، وَأَنَّ مُحَمَّداً عَبْدُكَ وَرَسُولُكَ.",
        targetCount = 4,
        reward = "من قالها أعتد الله رُبُعَهُ من النار"
    ),
    DhikrItem(
        title = "الرضا بالله ورسوله",
        text = "رَضِيتُ بِاللَّهِ رَبَّاً، وَبِالإِسْلاَمِ دِيناً، وَبِمُحَمَّدٍ صلى الله عليه وسلم نَبِيَّاً.",
        targetCount = 3,
        reward = "حقٌّ على الله أن يرضيه يوم القيامة"
    ),
    DhikrItem(
        title = "التحصين الشامل",
        text = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
        targetCount = 3,
        reward = "لم يضره شيء في ذلك اليوم"
    )
)

@Composable
fun AdhkarScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    items: List<DhikrItem> = defaultAdhkarList
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()
    val counts = remember { mutableStateListOf(*items.map { it.targetCount }.toTypedArray()) }
    var isAllCompleted by remember { mutableStateOf(false) }

    fun triggerVibration() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(40)
            }
        } catch (e: Exception) {
            // Ignore if vibration unavailable
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar with Back Button & Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onFinish,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(FajrLoopColors.SurfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "إغلاق الشاشة",
                        tint = FajrLoopColors.TextPrimary
                    )
                }

                Text(
                    text = "🌅 أذكار الصباح",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = FajrLoopColors.Primary
                )

                Surface(
                    shape = RoundedCornerShape(Radius.sm),
                    color = FajrLoopColors.PrimaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${items.size}",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FajrLoopColors.Primary,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    )
                }
            }

            // Top Progress Bar
            val progress = (pagerState.currentPage + 1).toFloat() / items.size.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = FajrLoopColors.Primary,
                trackColor = FajrLoopColors.BorderSubtle
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            if (isAllCompleted) {
                // Completion Screen
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FajrCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xxl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(FajrLoopColors.Success.copy(alpha = 0.2f))
                                    .border(2.dp, FajrLoopColors.Success, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🤲", fontSize = 36.sp)
                            }

                            Spacer(modifier = Modifier.height(Spacing.xl))

                            Text(
                                text = "تقبل الله طاعاتكم وغفر ذنوبكم",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = FajrLoopColors.Primary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(Spacing.md))

                            Text(
                                text = "اللهم إنا نسألك خير هذا اليوم فتحه ونصره ونوره وبركته وهداه، ونعوذ بك من شر ما فيه وشر ما بعده.",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = FajrLoopColors.TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            } else {
                // Dhikr Pager View
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    val item = items[page]
                    val currentCount = counts[page]
                    val isCompleted = currentCount == 0

                    var isPressed by remember { mutableStateOf(false) }
                    val scaleAnimated by animateFloatAsState(
                        targetValue = if (isPressed) 0.97f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "cardScale"
                    )

                    fun decrementCount() {
                        if (currentCount > 0) {
                            triggerVibration()
                            counts[page] = currentCount - 1
                            if (counts[page] == 0) {
                                if (page + 1 < items.size) {
                                    coroutineScope.launch {
                                        delay(250)
                                        pagerState.animateScrollToPage(page + 1)
                                    }
                                } else {
                                    isAllCompleted = true
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(scaleAnimated)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = !isCompleted
                            ) {
                                coroutineScope.launch {
                                    isPressed = true
                                    delay(80)
                                    isPressed = false
                                }
                                decrementCount()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        FajrCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.xl),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Title Badge
                                Surface(
                                    shape = RoundedCornerShape(Radius.sm),
                                    color = FajrLoopColors.PrimaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = Spacing.lg)
                                ) {
                                    Text(
                                        text = item.title,
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = FajrLoopColors.Primary,
                                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                                    )
                                }

                                // Dhikr Content Text
                                Text(
                                    text = item.text,
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 17.sp,
                                    color = FajrLoopColors.TextPrimary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 32.sp,
                                    modifier = Modifier.padding(bottom = Spacing.lg)
                                )

                                if (item.reward.isNotEmpty()) {
                                    Text(
                                        text = "💡 ${item.reward}",
                                        fontFamily = PpNmArabic,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 12.sp,
                                        color = FajrLoopColors.Primary.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = Spacing.lg)
                                    )
                                }

                                HorizontalDivider(
                                    color = FajrLoopColors.BorderSubtle,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(vertical = Spacing.sm)
                                )

                                Spacer(modifier = Modifier.height(Spacing.sm))

                                // Tap counter circle indicator
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCompleted) FajrLoopColors.Success.copy(alpha = 0.15f)
                                            else FajrLoopColors.PrimaryContainer
                                        )
                                        .border(
                                            2.5.dp,
                                            if (isCompleted) FajrLoopColors.Success else FajrLoopColors.Primary,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = "تم التكرار",
                                            tint = FajrLoopColors.Success,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = currentCount.toString(),
                                                fontFamily = PpNmArabic,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 26.sp,
                                                color = FajrLoopColors.Primary
                                            )
                                            Text(
                                                text = "متبقي",
                                                fontFamily = PpNmArabic,
                                                fontSize = 10.sp,
                                                color = FajrLoopColors.TextSecondary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(Spacing.sm))

                                Text(
                                    text = if (isCompleted) "تم هذا الذكر بنجاح ✨" else "👈 انقر في أي مكان بالشاشة للتكرار",
                                    fontFamily = PpNmArabic,
                                    fontSize = 12.sp,
                                    color = if (isCompleted) FajrLoopColors.Success else FajrLoopColors.TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Bottom action button
            Spacer(modifier = Modifier.height(Spacing.md))
            FajrPrimaryButton(
                text = if (isAllCompleted) "تم الأذكار — العودة للرئيسية 🤲" else "إغلاق الأذكار",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun AdhkarScreenPreview() {
    FajrLoopTheme {
        AdhkarScreen(onFinish = {})
    }
}
