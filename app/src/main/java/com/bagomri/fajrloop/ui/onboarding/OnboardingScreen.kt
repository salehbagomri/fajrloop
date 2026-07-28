package com.bagomri.fajrloop.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.components.TransparentButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import kotlinx.coroutines.launch

data class OnboardingItem(val icon: String, val title: String, val desc: String)

val onboardingItems = listOf(
    OnboardingItem("🌙", "مرحباً بك في حلقة الفجر 🌙", "نظام حلقات تفاعلي يساعدك وأعضاء حلقتك على الاستيقاظ لصلاة الفجر جماعة يومياً."),
    OnboardingItem("🤝", "كوّن حلقتك الأولى 🤝", "أنشئ حلقة جديدة، ادعُ أصدقاءك، وكن مسؤولاً عن إيقاظهم ليكونوا هم أيضاً عوناً لك."),
    OnboardingItem("⏰", "منبه ذكي لا يمكن تجاهله ⏰", "لن يتوقف منبهك عن الرنين إلا بعد حل التحدي والحصول على تأكيد الاستيقاظ من صديقك المسؤول."),
    OnboardingItem("🚀", "ابدأ رحلتك الآن! 🚀", "سجل الدخول، انضم لحلقة الفجر، واستمتع بنشاط الصباح وأجره العظيم.")
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { onboardingItems.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingItems.size - 1

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val item = onboardingItems[page]
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.icon,
                                fontSize = 64.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = item.title,
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = FajrLoopColors.Gold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = item.desc,
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = FajrLoopColors.TextPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            // Dot indicators
            Row(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingItems.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) FajrLoopColors.Gold else Color.White.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastPage) {
                    TransparentButton(
                        text = "تخطي",
                        onClick = onComplete,
                        modifier = Modifier.weight(1f)
                    )
                }
                GoldButton(
                    text = if (isLastPage) "ابدأ الآن 🚀" else "التالي",
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.weight(if (isLastPage) 2f else 1f)
                )
            }
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    FajrLoopTheme {
        OnboardingScreen(onComplete = {})
    }
}
