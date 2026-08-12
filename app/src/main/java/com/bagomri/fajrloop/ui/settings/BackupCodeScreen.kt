package com.bagomri.fajrloop.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

/**
 * Generates a real TOTP-style 6-digit code based on halqaId + current 30-minute window.
 * The code rotates every 30 minutes automatically.
 */
fun generateTotpCode(halqaId: String, sharedSecret: String = ""): String {
    val secret = if (sharedSecret.isNotEmpty()) sharedSecret else {
        try {
            val prefs = com.bagomri.fajrloop.FajrLoopApp.instance.getSharedPreferences(com.bagomri.fajrloop.alarm.AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.getString("halqa_shared_secret_$halqaId", "") ?: ""
        } catch (e: Exception) { "" }
    }
    val rawCode = com.bagomri.fajrloop.alarm.EmergencyCodeUtils.generateTotpCode(halqaId, sharedSecret = secret)
    return com.bagomri.fajrloop.alarm.EmergencyCodeUtils.formatTotpDisplay(rawCode)
}

fun getRemainingSeconds(): Int {
    return com.bagomri.fajrloop.alarm.EmergencyCodeUtils.getRemainingSecondsInWindow()
}

fun formatTimeRemaining(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun BackupCodeScreen(
    halqaId: String?,
    totpCode: String,
    isAlarmEnabled: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Live TOTP code & countdown
    var liveCode by remember { mutableStateOf(if (!halqaId.isNullOrEmpty()) generateTotpCode(halqaId) else totpCode) }
    var remainingSeconds by remember { mutableStateOf(getRemainingSeconds()) }

    // Countdown timer that refreshes the code when window expires
    LaunchedEffect(halqaId) {
        if (!halqaId.isNullOrEmpty()) {
            while (true) {
                remainingSeconds = getRemainingSeconds()
                liveCode = generateTotpCode(halqaId)
                delay(1000L)
            }
        }
    }

    val remainingMinutes = remainingSeconds / 60
    val progressFraction = remainingSeconds / (30f * 60f)

    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            FajrLoopTopBar(
                title = "كود الطوارئ",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                if (halqaId.isNullOrEmpty()) {
                    // No Halqa State
                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1C30))
                            .border(1.dp, Color(0xFF2D2A45), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = FajrLoopColors.TextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "غير متصل بحلقة",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FajrLoopColors.TextPrimary
                    )

                    Text(
                        text = "يجب الانضمام لحلقة أولاً للحصول على كود الطوارئ الخاص بك.",
                        fontFamily = PpNmArabic,
                        fontSize = 13.sp,
                        color = FajrLoopColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.weight(1f))
                } else {
                    // Explanatory Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(Color(0xFF1A1830))
                            .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.25f), RoundedCornerShape(Radius.md))
                            .padding(Spacing.lg)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = FajrLoopColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Column {
                                Text(
                                    text = "كيف يعمل كود الطوارئ؟",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = FajrLoopColors.Primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "يُستخدم هذا الكود لإيقاف المنبه في الحالات الطارئة فقط (مثل المرض أو الظروف القاهرة). يتجدد الكود تلقائياً كل 30 دقيقة.",
                                    fontFamily = PpNmArabic,
                                    fontSize = 12.sp,
                                    color = FajrLoopColors.TextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Shield Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(FajrLoopColors.PrimaryContainer)
                            .border(2.dp, FajrLoopColors.Primary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = FajrLoopColors.Primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // TOTP Code Card
                    FajrCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(Spacing.xl),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "كود الطوارئ المؤقت",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FajrLoopColors.Primary,
                                modifier = Modifier.padding(bottom = Spacing.md)
                            )

                            // Large TOTP Code Display
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Radius.md))
                                    .background(Color(0xFF0D0B1A))
                                    .border(1.dp, FajrLoopColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(Radius.md))
                                    .padding(vertical = Spacing.xl, horizontal = Spacing.lg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = liveCode,
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 40.sp,
                                    color = FajrLoopColors.Primary,
                                    letterSpacing = 6.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.md))

                            // Countdown Timer
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = if (remainingMinutes < 5) Color(0xFFFF6B6B) else FajrLoopColors.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "يتجدد خلال: ${formatTimeRemaining(remainingSeconds)}",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (remainingMinutes < 5) Color(0xFFFF6B6B) else FajrLoopColors.TextSecondary
                                )
                            }

                            // Progress bar
                            Spacer(modifier = Modifier.height(Spacing.sm))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(Radius.full)),
                                color = if (remainingMinutes < 5) Color(0xFFFF6B6B) else FajrLoopColors.Primary,
                                trackColor = Color(0xFF1E1C30)
                            )
                        }
                    }

                    // Copy Button
                    FajrPrimaryButton(
                        text = "نسخ الكود",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("FajrLoop Emergency Code", liveCode.replace(" ", ""))
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ كود الطوارئ بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = Icons.Outlined.ContentCopy
                    )

                    // Warning
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(Color(0xFF2A1A10))
                            .border(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.3f), RoundedCornerShape(Radius.md))
                            .padding(Spacing.md)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(
                                text = "تنبيه: استخدام كود الطوارئ بشكل متكرر سيؤثر سلباً على سجل التزامك في الحلقة. استخدمه للضرورة القصوى فقط.",
                                fontFamily = PpNmArabic,
                                fontSize = 11.sp,
                                color = Color(0xFFFF9B9B),
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.lg))
                }
            }
        }
    }
}

@Preview
@Composable
private fun BackupCodeScreenPreview() {
    FajrLoopTheme {
        BackupCodeScreen(
            halqaId = "h1",
            totpCode = "482 910",
            isAlarmEnabled = true,
            onBackClick = {}
        )
    }
}
