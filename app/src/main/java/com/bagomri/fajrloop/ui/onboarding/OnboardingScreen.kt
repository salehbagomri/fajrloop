package com.bagomri.fajrloop.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.*
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
        FajrBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar — Skip button without card & padded away from status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLastPage) {
                    TextButton(onClick = onComplete) {
                        Text(
                            text = "تخطي",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = FajrLoopColors.TextSecondary
                        )
                    }
                }
            }

            // Pager content — Directly on screen without Card
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val item = onboardingItems[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon Badge Container
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(FajrLoopColors.PrimaryContainer)
                            .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = FajrLoopColors.Primary,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xxl))

                    // Title
                    Text(
                        text = item.title,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = FajrLoopColors.Primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )

                    // Subtitle — reduced size (14sp)
                    Text(
                        text = item.desc,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = FajrLoopColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Dot indicators
            Row(
                modifier = Modifier.padding(vertical = Spacing.md),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingItems.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
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

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Circular Next / Start Button at bottom center (Arrow pointing left in RTL)
            IconButton(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(FajrLoopColors.Primary)
            ) {
                Icon(
                    imageVector = if (isLastPage) Icons.Outlined.Check else Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = if (isLastPage) "ابدأ" else "التالي",
                    tint = FajrLoopColors.Background,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))
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
