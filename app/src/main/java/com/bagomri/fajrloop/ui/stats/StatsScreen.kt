package com.bagomri.fajrloop.ui.stats

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
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
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import java.util.*

@Composable
fun StatsScreen(
    state: StatsUiState,
    currentUid: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("نشاطي", "المتصدرون", "الإنجازات")

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            FajrLoopTopBar(
                title = "الإحصائيات",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "تقرير التزامي بصلاة الفجر في تطبيق حلقة الفجر!\nسلسلتي الحالية: ${state.currentStreak} أيام\nإجمالي الأيام: ${state.totalFajr} يوم\nنقاط حماية الحلقة: ${state.totalRescues}\nانضم إلينا وحافظ على فجرك!"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير الفجر"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "مشاركة التقرير",
                            tint = FajrLoopColors.Primary
                        )
                    }
                }
            )

            // Top Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                StatCard(
                    title = "السلسلة",
                    value = "${state.currentStreak} د",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "أطول سلسلة",
                    value = "${state.longestStreak} د",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "إجمالي الفجر",
                    value = "${state.totalFajr} د",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الإنقاذ",
                    value = "${state.totalRescues}",
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = FajrLoopColors.Surface,
                contentColor = FajrLoopColors.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    .clip(RoundedCornerShape(Radius.md))
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontFamily = PpNmArabic,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (selectedTabIndex == index) FajrLoopColors.Primary else FajrLoopColors.TextSecondary
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg)
            ) {
                when (selectedTabIndex) {
                    0 -> MyActivityTab(state = state)
                    1 -> LeaderboardTab(state = state, currentUid = currentUid)
                    2 -> AchievementsTab(state = state)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    FajrCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.md, horizontal = Spacing.xs),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = FajrLoopColors.Primary,
                modifier = Modifier.padding(vertical = Spacing.xxs)
            )
            Text(
                text = title,
                fontFamily = PpNmArabic,
                fontSize = 11.sp,
                color = FajrLoopColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MyActivityTab(state: StatsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        contentPadding = PaddingValues(vertical = Spacing.md)
    ) {
        // Weekly Chart
        item {
            FajrCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = "التزام الأسبوع الحالي",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FajrLoopColors.TextPrimary,
                        modifier = Modifier.padding(bottom = Spacing.lg)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        state.weeklyChart.forEach { bar ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                val color = when (bar.status) {
                                    "awake" -> FajrLoopColors.Success
                                    "travel" -> FajrLoopColors.Info
                                    "challenge_done" -> FajrLoopColors.Primary
                                    "ringing" -> FajrLoopColors.Warning
                                    else -> FajrLoopColors.Danger
                                }

                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .fillMaxHeight(bar.heightPercent.coerceIn(0.08f, 1f))
                                        .clip(RoundedCornerShape(topStart = Radius.sm, topEnd = Radius.sm))
                                        .background(color)
                                )

                                Spacer(modifier = Modifier.height(Spacing.xs))

                                Text(
                                    text = bar.dayName,
                                    fontFamily = PpNmArabic,
                                    fontSize = 11.sp,
                                    color = FajrLoopColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Calendar
        item {
            FajrCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Text(
                        text = "سجل تقويم الشهر الحالي",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FajrLoopColors.TextPrimary,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )

                    // Grid Days
                    val totalDays = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                    val daysList = (1..totalDays).toList()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        items(daysList) { day ->
                            val status = state.dayStatusMap[day]
                            val isToday = day == state.currentDayOfMonth

                            val bgColor = when (status) {
                                "awake", "challenge_done" -> FajrLoopColors.Success.copy(alpha = 0.2f)
                                "travel" -> FajrLoopColors.Info.copy(alpha = 0.2f)
                                "missed" -> FajrLoopColors.Danger.copy(alpha = 0.2f)
                                else -> FajrLoopColors.SurfaceVariant
                            }

                            val textColor = when {
                                isToday -> FajrLoopColors.Primary
                                status == "awake" || status == "challenge_done" -> FajrLoopColors.Success
                                status == "travel" -> FajrLoopColors.Info
                                status == "missed" -> FajrLoopColors.Danger
                                else -> FajrLoopColors.TextSecondary
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(Radius.sm))
                                    .background(bgColor)
                                    .border(
                                        if (isToday) 2.dp else 1.dp,
                                        if (isToday) FajrLoopColors.Primary else FajrLoopColors.Border,
                                        RoundedCornerShape(Radius.sm)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontFamily = PpNmArabic,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardTab(state: StatsUiState, currentUid: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(vertical = Spacing.md)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                FajrCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "الأكثر التزاماً", fontFamily = PpNmArabic, fontSize = 11.sp, color = FajrLoopColors.Primary)
                        Text(
                            text = state.fastestMember.ifEmpty { "-" },
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FajrLoopColors.TextPrimary,
                            modifier = Modifier.padding(top = Spacing.xxs)
                        )
                    }
                }

                FajrCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "البطل المنقذ", fontFamily = PpNmArabic, fontSize = 11.sp, color = FajrLoopColors.Info)
                        Text(
                            text = state.topRescuer.ifEmpty { "-" },
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FajrLoopColors.TextPrimary,
                            modifier = Modifier.padding(top = Spacing.xxs)
                        )
                    }
                }
            }
        }

        items(state.leaderboard) { item ->
            FajrCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val rankText = "#${item.rank}"
                    val rankColor = when (item.rank) {
                        1 -> FajrLoopColors.Primary
                        2 -> FajrLoopColors.TextPrimary
                        3 -> FajrLoopColors.PrimaryMuted
                        else -> FajrLoopColors.TextSecondary
                    }

                    Text(
                        text = rankText,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = rankColor,
                        modifier = Modifier.width(36.dp)
                    )

                    UserAvatar(
                        photoUrl = item.photoUrl,
                        userName = item.displayName,
                        size = 38.dp,
                        modifier = Modifier.padding(end = Spacing.sm)
                    )

                    Text(
                        text = if (item.userId == currentUid) "${item.displayName} (أنت)" else item.displayName,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (item.userId == currentUid) FajrLoopColors.Primary else FajrLoopColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${item.streak} يوم",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = FajrLoopColors.Primary
                        )
                        Text(
                            text = "${item.rescues} إنقاذ",
                            fontFamily = PpNmArabic,
                            fontSize = 10.sp,
                            color = FajrLoopColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsTab(state: StatsUiState) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        contentPadding = PaddingValues(vertical = Spacing.md)
    ) {
        items(state.achievements) { item ->
            val isAcquired = item.acquiredDate != null

            FajrCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(FajrLoopColors.SurfaceVariant)
                            .border(
                                2.dp,
                                if (isAcquired) FajrLoopColors.Primary else FajrLoopColors.Border,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(Spacing.md))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isAcquired) FajrLoopColors.TextPrimary else FajrLoopColors.TextSecondary
                        )
                        Text(
                            text = item.desc,
                            fontFamily = PpNmArabic,
                            fontSize = 12.sp,
                            color = FajrLoopColors.TextSecondary,
                            modifier = Modifier.padding(vertical = Spacing.xxs)
                        )
                        if (isAcquired) {
                            Text(
                                text = "حصلت عليه في: ${item.acquiredDate}",
                                fontFamily = PpNmArabic,
                                fontSize = 11.sp,
                                color = FajrLoopColors.Success
                            )
                        }
                    }

                    if (isAcquired) {
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "لقد حصلت على وسام «${item.title}» في تطبيق حلقة الفجر!\nالمتطلب: ${item.desc}\nانضم إلينا وحافظ على صلاتك!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الإنجاز"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "مشاركة الوسام",
                                tint = FajrLoopColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun StatsScreenPreview() {
    FajrLoopTheme {
        StatsScreen(
            state = StatsUiState(
                currentStreak = 5,
                longestStreak = 12,
                totalFajr = 24,
                totalRescues = 3,
                fastestMember = "صالح",
                topRescuer = "أحمد"
            ),
            currentUid = "u1",
            onBackClick = {}
        )
    }
}
