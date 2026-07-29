package com.bagomri.fajrloop.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.components.FajrSecondaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing
import kotlinx.coroutines.launch

data class OnboardingItem(
    val icon: ImageVector,
    val title: String,
    val desc: String
)

val onboardingItems = listOf(
    OnboardingItem(
        Icons.Outlined.NightsStay,
        "حلقة الفجر",
        "نظام تفاعلي يجمعك بأصدقائك للاستيقاظ لصلاة الفجر يومياً"
    ),
    OnboardingItem(
        Icons.Outlined.Group,
        "كوّن حلقتك",
        "أنشئ حلقة، ادعُ أصدقاءك، وكن عوناً لهم على صلاة الفجر"
    ),
    OnboardingItem(
        Icons.Outlined.Alarm,
        "منبه لا يُتجاهل",
        "لا يتوقف المنبه إلا بعد حل التحدي وتأكيد صديقك لاستيقاظك"
    ),
    OnboardingItem(
        Icons.Outlined.RocketLaunch,
        "ابدأ الآن",
        "سجّل دخولك وانضم لحلقتك الأولى"
    )
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
        FajrBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xxl, vertical = Spacing.section),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))

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
                    FajrCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.lg)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.xxl),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // أيقونة Outlined بدل إيموجي
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = FajrLoopColors.Primary,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = Spacing.lg)
                            )

                            Text(
                                text = item.title,
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = FajrLoopColors.Primary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = Spacing.md)
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
                modifier = Modifier.padding(vertical = Spacing.xl),
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
                            .padding(horizontal = Spacing.xs)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) FajrLoopColors.Primary
                                else FajrLoopColors.Border
                            )
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastPage) {
                    FajrSecondaryButton(
                        text = "تخطي",
                        onClick = onComplete,
                        modifier = Modifier.weight(1f)
                    )
                }
                FajrPrimaryButton(
                    text = if (isLastPage) "ابدأ" else "التالي",
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
private fun OnboardingScreenPreview() {
    FajrLoopTheme {
        OnboardingScreen(onComplete = {})
    }
}
