package com.bagomri.fajrloop.ui.permissions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
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

/**
 * تحديد الأيقونة المناسبة لكل نوع صلاحية
 */
private fun getPermissionIcon(id: String): ImageVector {
    return when (id) {
        "notifications" -> Icons.Outlined.Notifications
        "exact_alarm"   -> Icons.Outlined.Alarm
        "battery"       -> Icons.Outlined.BatterySaver
        "fullscreen"    -> Icons.Outlined.PhonelinkRing
        "overlays"      -> Icons.Outlined.Layers
        "oem_battery"   -> Icons.Outlined.PhoneAndroid
        else            -> Icons.Outlined.Security
    }
}

@Composable
fun PermissionScreen(
    permissions: List<PermissionItemData>,
    allGranted: Boolean,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val grantedCount = permissions.count { it.isGranted }
    val totalCount = permissions.size

    // حالة Dialog إعدادات OEM
    var showOemDialog by remember { mutableStateOf(false) }
    var oemOnRequest by remember { mutableStateOf<(() -> Unit)?>(null) }

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
            // Header Badge Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(FajrLoopColors.PrimaryContainer)
                    .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (allGranted) Icons.Outlined.VerifiedUser else Icons.Outlined.Security,
                    contentDescription = null,
                    tint = if (allGranted) FajrLoopColors.Success else FajrLoopColors.Primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = "إعداد الصلاحيات المطلوبة",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = FajrLoopColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = Spacing.xs)
            )

            Text(
                text = "يرجى تفعيل الصلاحيات التالية لضمان رنين المنبه بدقة ودون انقطاع عند موعد صلاة الفجر",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = FajrLoopColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = Spacing.md)
            )

            // Progress Summary Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.full))
                    .background(
                        if (allGranted) FajrLoopColors.Success.copy(alpha = 0.12f)
                        else FajrLoopColors.Surface
                    )
                    .border(
                        1.dp,
                        if (allGranted) FajrLoopColors.Success.copy(alpha = 0.4f)
                        else FajrLoopColors.Border,
                        RoundedCornerShape(Radius.full)
                    )
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (allGranted) Icons.Outlined.CheckCircle else Icons.Outlined.Info,
                        contentDescription = null,
                        tint = if (allGranted) FajrLoopColors.Success else FajrLoopColors.TextSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 0.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = if (allGranted) "جميع الصلاحيات مكتملة ($totalCount / $totalCount)"
                        else "مكتمل $grantedCount من أصل $totalCount صلاحية",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (allGranted) FajrLoopColors.Success else FajrLoopColors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // قائمة الصلاحيات
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(permissions, key = { it.id }) { item ->
                    // عنصر OEM يفتح Dialog تعليمي بدلاً من فتح الإعدادات مباشرة
                    if (item.id == "oem_battery" && !item.isGranted) {
                        PermissionCardRow(
                            item = item.copy(
                                onRequest = {
                                    oemOnRequest = item.onRequest
                                    showOemDialog = true
                                }
                            )
                        )
                    } else {
                        PermissionCardRow(item = item)
                    }
                }
            }

            // Dialog تعليمي خاص بإعدادات OEM
            if (showOemDialog) {
                OemBatteryDialog(
                    onOpenSettings = {
                        oemOnRequest?.invoke()
                    },
                    onDismiss = { showOemDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Bottom Action Area
            AnimatedVisibility(
                visible = allGranted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = onDoneClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FajrLoopColors.Primary,
                        contentColor = Color(0xFF0D0B1A)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "متابعة",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0D0B1A)
                        )
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF0D0B1A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (!allGranted) {
                Text(
                    text = "انقر على أي صلاحية أعلاه لتفعيلها مباشرة من إعدادات النظام",
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = Spacing.sm)
                )
            }
        }
    }
}

@Composable
private fun PermissionCardRow(
    item: PermissionItemData,
    modifier: Modifier = Modifier
) {
    val icon = getPermissionIcon(item.id)
    val isOem = item.id == "oem_battery"
    // لون برتقالي للعناصر التي تحتاج إجراءً يدوياً (OEM)
    val accentColor = if (isOem && !item.isGranted) FajrLoopColors.Warning else FajrLoopColors.Primary

    FajrCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !item.isGranted, onClick = item.onRequest)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Feature Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(
                        if (item.isGranted) FajrLoopColors.Success.copy(alpha = 0.12f)
                        else accentColor.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (item.isGranted) FajrLoopColors.Success else accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // Title & Description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = FajrLoopColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = item.description,
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextSecondary,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            // Status Action Button / Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(
                        if (item.isGranted) FajrLoopColors.Success.copy(alpha = 0.15f)
                        else accentColor
                    )
                    .padding(horizontal = Spacing.md, vertical = Spacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (item.isGranted) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            tint = FajrLoopColors.Success,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(end = 0.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = if (item.isGranted) "ممنوحة" else if (isOem) "فتح" else "تفعيل",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (item.isGranted) FajrLoopColors.Success else FajrLoopColors.Background
                    )
                }
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
                PermissionItemData("notifications", "إشعارات التطبيق", "لعرض إشعار المنبه على شاشة القفل", true, {}),
                PermissionItemData("exact_alarm", "المنبه الدقيق", "لضمان رنين المنبه في الوقت المحدد بدقة الثانية", false, {}),
                PermissionItemData("battery", "تجاهل تحسين البطارية", "لحماية خدمة الرنين من القتل في الخلفية", false, {})
            ),
            allGranted = false,
            onDoneClick = {}
        )
    }
}
