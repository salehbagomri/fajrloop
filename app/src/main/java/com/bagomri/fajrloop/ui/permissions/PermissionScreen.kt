package com.bagomri.fajrloop.ui.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

data class PermissionItemData(
    val id: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val onRequest: () -> Unit
)

@Composable
fun PermissionScreen(
    permissions: List<PermissionItemData>,
    allGranted: Boolean,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "إعداد الصلاحيات 🛡️",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = FajrLoopColors.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "يرجى منح الصلاحيات التالية لضمان عمل المنبه في الوقت المحدد وظهوره فوق قفل الشاشة.",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = FajrLoopColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(permissions, key = { it.id }) { item ->
                    PermissionRowItem(item = item)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (allGranted) {
                GoldButton(
                    text = "جميع الصلاحيات ممنوحة — المتابعة 🚀",
                    onClick = onDoneClick,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "⚠️ يرجى منح جميع الصلاحيات أعلاه للبدء",
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.DangerRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionRowItem(
    item: PermissionItemData,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !item.isGranted, onClick = item.onRequest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = item.title,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FajrLoopColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = item.description,
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (item.isGranted) FajrLoopColors.SuccessGreen.copy(alpha = 0.2f)
                        else FajrLoopColors.Gold.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (item.isGranted) "ممنوحة ✓" else "منح الصلاحية",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (item.isGranted) FajrLoopColors.SuccessGreen else FajrLoopColors.Gold
                )
            }
        }
    }
}

@Preview
@Composable
fun PermissionScreenPreview() {
    FajrLoopTheme {
        PermissionScreen(
            permissions = listOf(
                PermissionItemData("1", "إشعارات التطبيق", "لعرض إشعار المنبه على شاشة القفل", true, {}),
                PermissionItemData("2", "المنبه الدقيق", "لضمان رنين المنبه في الوقت المحدد", false, {})
            ),
            allGranted = false,
            onDoneClick = {}
        )
    }
}
