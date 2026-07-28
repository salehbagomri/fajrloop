package com.bagomri.fajrloop.ui.stats

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
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
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.UserAvatar
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
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
    val tabTitles = listOf("نشاطي 📅", "الصدارة 🏆", "الإنجازات 🌟")

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            FajrLoopTopBar(
                title = "إحصائيات الفجر 📊",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "تقرير التزامي بصلاة الفجر في تطبيق حلقة الفجر! 🌅🕋\nسلسلتي الحالية: ${state.currentStreak} أيام\nإجمالي الأيام: ${state.totalFajr} يوم\nنقاط حماية الحلقة (الإنقاذ): ${state.totalRescues} 🦸\nانضم إلينا وحافظ على فجرك!"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير الفجر"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة التقرير",
                            tint = FajrLoopColors.Gold
                        )
                    }
                }
            )

            // Top Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "السلسلة",
                    value = "${state.currentStreak} د",
                    emoji = "🔥",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "أطول سلسلة",
                    value = "${state.longestStreak} د",
                    emoji = "⭐",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "إجمالي الفجر",
                    value = "${state.totalFajr} د",
                    emoji = "🕋",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "الإنقاذ",
                    value = "${state.totalRescues}",
                    emoji = "🦸",
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = FajrLoopColors.Surface.copy(alpha = 0.6f),
                contentColor = FajrLoopColors.Gold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
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
                                fontSize = 14.sp
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
                    .padding(horizontal = 16.dp)
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
    emoji: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = value,
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FajrLoopColors.Gold,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = title,
                fontFamily = PpNmArabic,
                fontSize = 10.sp,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        // Weekly Chart
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التزام الأسبوع الحالي 📈",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FajrLoopColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
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
                                    "awake" -> FajrLoopColors.SuccessGreen
                                    "travel" -> Color(0xFF3498DB)
                                    "challenge_done" -> FajrLoopColors.Gold
                                    "ringing" -> Color(0xFFB57CFF)
                                    else -> FajrLoopColors.DangerRed
                                }

                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .fillMaxHeight(bar.heightPercent.coerceIn(0.08f, 1f))
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(color)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

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
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "سجل تقويم الشهر الحالي 📅",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FajrLoopColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Grid Days
                    val totalDays = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                    val daysList = (1..totalDays).toList()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(daysList) { day ->
                            val status = state.dayStatusMap[day]
                            val isToday = day == state.currentDayOfMonth

                            val bgColor = when (status) {
                                "awake", "challenge_done" -> FajrLoopColors.SuccessGreen.copy(alpha = 0.25f)
                                "travel" -> Color(0xFF3498DB).copy(alpha = 0.25f)
                                "missed" -> FajrLoopColors.DangerRed.copy(alpha = 0.25f)
                                else -> FajrLoopColors.Surface.copy(alpha = 0.4f)
                            }

                            val textColor = when {
                                isToday -> FajrLoopColors.Gold
                                status == "awake" || status == "challenge_done" -> FajrLoopColors.SuccessGreen
                                status == "travel" -> Color(0xFF3498DB)
                                status == "missed" -> FajrLoopColors.DangerRed
                                else -> FajrLoopColors.TextSecondary
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .border(
                                        if (isToday) 2.dp else 1.dp,
                                        if (isToday) FajrLoopColors.Gold else FajrLoopColors.SurfaceBorder.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "👑 الأكثر التزاماً", fontFamily = PpNmArabic, fontSize = 11.sp, color = FajrLoopColors.Gold)
                        Text(
                            text = state.fastestMember,
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FajrLoopColors.TextPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🦸 البطل المنقذ", fontFamily = PpNmArabic, fontSize = 11.sp, color = Color(0xFFB57CFF))
                        Text(
                            text = state.topRescuer,
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FajrLoopColors.TextPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        items(state.leaderboard) { item ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Rank badge
                    val rankText = when (item.rank) {
                        1 -> "👑 1"
                        2 -> "🥈 2"
                        3 -> "🥉 3"
                        else -> "#${item.rank}"
                    }
                    val rankColor = when (item.rank) {
                        1 -> FajrLoopColors.Gold
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> FajrLoopColors.TextSecondary
                    }

                    Text(
                        text = rankText,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = rankColor,
                        modifier = Modifier.width(44.dp)
                    )

                    UserAvatar(
                        photoUrl = item.photoUrl,
                        size = 38.dp,
                        modifier = Modifier.padding(end = 10.dp)
                    )

                    Text(
                        text = if (item.userId == currentUid) "${item.displayName} (أنت)" else item.displayName,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (item.userId == currentUid) FajrLoopColors.Gold else FajrLoopColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${item.streak} يوم",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = FajrLoopColors.Gold
                        )
                        Text(
                            text = "${item.rescues} إنقاذ 🦸",
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(state.achievements) { item ->
            val isAcquired = item.acquiredDate != null

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isAcquired) Color.Transparent else Color.Black.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji Circle
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(FajrLoopColors.Surface.copy(alpha = 0.6f))
                            .border(
                                2.dp,
                                runCatching { Color(android.graphics.Color.parseColor(item.colorCode)) }.getOrDefault(FajrLoopColors.Gold),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item.emoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

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
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        if (isAcquired) {
                            Text(
                                text = "حصلت عليه في: ${item.acquiredDate}",
                                fontFamily = PpNmArabic,
                                fontSize = 11.sp,
                                color = FajrLoopColors.SuccessGreen
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
                                        "لقد حصلت على وسام «${item.title}» في تطبيق حلقة الفجر! 🌅🏆\nالمتطلب: ${item.desc}\nانضم إلينا وحافظ على صلاتك!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة الإنجاز"))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "مشاركة الوسام",
                                tint = FajrLoopColors.Gold
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
fun StatsScreenPreview() {
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
