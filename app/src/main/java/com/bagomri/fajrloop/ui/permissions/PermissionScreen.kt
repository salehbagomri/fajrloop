package com.bagomri.fajrloop.ui.permissions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
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
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

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
        FajrBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "إعداد الصلاحيات",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = FajrLoopColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            Text(
                text = "يرجى منح الصلاحيات التالية لضمان عمل المنبه في الوقت المحدد",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = FajrLoopColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = Spacing.xxl)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                items(permissions, key = { it.id }) { item ->
                    PermissionRowItem(item = item)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            if (allGranted) {
                FajrPrimaryButton(
                    text = "جميع الصلاحيات ممنوحة — متابعة",
                    onClick = onDoneClick,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "يرجى منح جميع الصلاحيات للمتابعة",
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.Warning,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = Spacing.md)
                )
            }
        }
    }
}

@Composable
private fun PermissionRowItem(
    item: PermissionItemData,
    modifier: Modifier = Modifier
) {
    FajrCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !item.isGranted, onClick = item.onRequest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Status icon
            Icon(
                imageVector = if (item.isGranted) {
                    Icons.Outlined.CheckCircleOutline
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = if (item.isGranted) "ممنوحة" else "مطلوبة",
                tint = if (item.isGranted) FajrLoopColors.Success else FajrLoopColors.Primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = Spacing.md)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md)
            ) {
                Text(
                    text = item.title,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = FajrLoopColors.TextPrimary,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
                Text(
                    text = item.description,
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextSecondary,
                    lineHeight = 18.sp
                )
            }

            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(
                        if (item.isGranted) FajrLoopColors.Success.copy(alpha = 0.12f)
                        else FajrLoopColors.PrimaryContainer
                    )
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            ) {
                Text(
                    text = if (item.isGranted) "ممنوحة" else "منح",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (item.isGranted) FajrLoopColors.Success else FajrLoopColors.Primary
                )
            }
        }
    }
}

@Preview
@Composable
private fun PermissionScreenPreview() {
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
