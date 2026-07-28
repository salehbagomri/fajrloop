package com.bagomri.fajrloop.ui.adhkar

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DhikrItem(val text: String, val targetCount: Int)

val defaultAdhkarList = listOf(
    DhikrItem(
        "أعوذ بالله من الشيطان الرجيم: {اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَؤُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ}",
        1
    ),
    DhikrItem(
        "بسم الله الرحمن الرحيم: {قُلْ هُوَ اللَّهُ أَحَدٌ * اللَّهُ الصَّمَدُ * لَمْ يَلِدْ وَلَمْ يُولَدْ * وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ}",
        3
    ),
    DhikrItem(
        "بسم الله الرحمن الرحيم: {قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ * مِن شَرِّ مَا خَلَقَ * وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ * وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ * وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ}",
        3
    ),
    DhikrItem(
        "بسم الله الرحمن الرحيم: {قُلْ أَعُوذُ بِرَبِّ النَّاسِ * مَلِكِ النَّاسِ * إِلَٰهِ النَّاسِ * مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ * الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ * مِنَ الْجِنَّةِ وَالنَّاسِ}",
        3
    ),
    DhikrItem(
        "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ، وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ.",
        1
    ),
    DhikrItem(
        "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ.",
        1
    ),
    DhikrItem(
        "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إلاَّ أَنْتَ. (سيد الاستغفار)",
        1
    ),
    DhikrItem(
        "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ، وَمَلاَئِكَتَكَ، وَجَمِيعَ خَلْقِكَ، أَنَّكَ أَنْتَ اللَّهُ لاَ إِلَهَ إلاَّ أَنْتَ وَحْدَهُ لاَ شَرِيكَ لَهُ، وَأَنَّ مُحَمَّداً عَبْدُكَ وَرَسُولُكَ.",
        4
    ),
    DhikrItem(
        "رَضِيتُ بِاللَّهِ رَبَّاً، وَبِالإِسْلاَمِ دِيناً، وَبِمُحَمَّدٍ صلى الله عليه وسلم نَبِيَّاً.",
        3
    ),
    DhikrItem(
        "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
        3
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    items: List<DhikrItem> = defaultAdhkarList
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { items.size })
    val coroutineScope = rememberCoroutineScope()
    val counts = remember { mutableStateListOf(*items.map { it.targetCount }.toTypedArray()) }
    var isAllCompleted by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFinish) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = FajrLoopColors.TextPrimary
                    )
                }

                Text(
                    text = "أذكار الصباح 🌅",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = FajrLoopColors.TextPrimary
                )

                Text(
                    text = "${pagerState.currentPage + 1} / ${items.size}",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = FajrLoopColors.Gold
                )
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val item = items[page]
                val currentCount = counts[page]
                val isCompleted = currentCount == 0

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.text,
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = FajrLoopColors.TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            Text(
                                text = "التكرار المطلوب: ${item.targetCount} ${if (item.targetCount == 1) "مرة واحدة" else "مرات"}",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = FajrLoopColors.TextSecondary,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Tap counter button
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCompleted) FajrLoopColors.SuccessGreen.copy(alpha = 0.2f)
                                        else FajrLoopColors.Gold.copy(alpha = 0.15f)
                                    )
                                    .border(
                                        2.dp,
                                        if (isCompleted) FajrLoopColors.SuccessGreen else FajrLoopColors.Gold,
                                        CircleShape
                                    )
                                    .clickable(enabled = !isCompleted) {
                                        if (currentCount > 0) {
                                            counts[page] = currentCount - 1
                                            if (counts[page] == 0) {
                                                if (page + 1 < items.size) {
                                                    coroutineScope.launch {
                                                        delay(300)
                                                        pagerState.animateScrollToPage(page + 1)
                                                    }
                                                } else {
                                                    isAllCompleted = true
                                                    Toast
                                                        .makeText(
                                                            context,
                                                            "تقبل الله طاعاتكم وغفر ذنوبكم 🌅",
                                                            Toast.LENGTH_LONG
                                                        )
                                                        .show()
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isCompleted) "✓" else currentCount.toString(),
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = if (isCompleted) FajrLoopColors.SuccessGreen else FajrLoopColors.Gold
                                )
                            }
                        }
                    }
                }
            }

            // Bottom action
            Spacer(modifier = Modifier.height(16.dp))
            GoldButton(
                text = if (isAllCompleted) "تقبل الله 🌅 (اضغط للإغلاق)" else "إغلاق الشاشة",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
fun AdhkarScreenPreview() {
    FajrLoopTheme {
        AdhkarScreen(onFinish = {})
    }
}
