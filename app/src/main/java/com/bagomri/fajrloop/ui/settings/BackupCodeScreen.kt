package com.bagomri.fajrloop.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun BackupCodeScreen(
    halqaId: String?,
    totpCode: String,
    isAlarmEnabled: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            FajrLoopTopBar(
                title = "كود الطوارئ 🔑",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (halqaId.isNullOrEmpty()) {
                    Text(
                        text = "⚠️ يجب الانضمام لحلقة أولاً للحصول على كود الطوارئ لليوم الحالي.",
                        fontFamily = PpNmArabic,
                        fontSize = 15.sp,
                        color = FajrLoopColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "كود الطوارئ اليومي (TOTP) 🔑",
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FajrLoopColors.Gold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Text(
                                text = "يُستخدم هذا الكود المؤقت لإيقاف المنبه في الحالات الطارئة فقط.",
                                fontFamily = PpNmArabic,
                                fontSize = 13.sp,
                                color = FajrLoopColors.TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Display TOTP Code
                            Text(
                                text = totpCode,
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = FajrLoopColors.Gold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Text(
                                text = if (isAlarmEnabled) "الكود نشط وجاهز! متبقي 20 دقيقة على انتهاء صلاحيته."
                                else "هذا الكود ينشط تلقائياً ولمدة 30 دقيقة فقط فور دخول وقت أذان الفجر اليوم.",
                                fontFamily = PpNmArabic,
                                fontSize = 12.sp,
                                color = if (isAlarmEnabled) FajrLoopColors.SuccessGreen else FajrLoopColors.TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            GoldButton(
                                text = "نسخ كود الطوارئ 📋",
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("FajrLoop Emergency Code", totpCode.replace(" ", ""))
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ كود الطوارئ بنجاح! 📋", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
