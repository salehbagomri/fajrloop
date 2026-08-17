package com.bagomri.fajrloop.ui.settings.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.components.FajrSecondaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import com.bagomri.fajrloop.utils.AppInfoUtils

/**
 * 🔒 حوار سياسة الخصوصية والأمان (Privacy Policy Dialog)
 */
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        FajrCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = FajrLoopColors.Primary,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = Spacing.sm)
                )

                Text(
                    text = "سياسة الخصوصية وحماية البيانات 🔒",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.Primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )

                Surface(
                    color = FajrLoopColors.SurfaceVariant,
                    shape = RoundedCornerShape(Radius.md),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.lg)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(
                            text = "• حماية الخصوصية: نحترم خصوصيتك بالكامل. لا نقوم ببيع أو مشاركة بياناتك الشخصية مع أي طرف ثالث إطلاقاً.\n\n" +
                                   "• صلاحية الموقع (GPS): تقتصر الاستفادة منها حصراً على حساب التوقيت الفلكي الدقيق لأذان الفجر في مدينتك، ولا يتم تتبع تحركاتك.\n\n" +
                                   "• التشفير السحابي: تُحفظ بيانات الحلقة وسجلات الالتزام مشفرة وآمنة عبر خوادم Firebase السحابية.\n\n" +
                                   "• التنبيهات والمنبه: يُستخدم المنبه والخدمة الصوتية المحلية لضمان رنين جوالك في موعد الفجر المحدد فقط.",
                            fontFamily = PpNmArabic,
                            fontSize = 13.sp,
                            color = FajrLoopColors.TextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }

                FajrPrimaryButton(
                    text = "فهمت وموافق ✨",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 👨‍💻 حوار المطور والدعم الفني (Developer Info Dialog)
 */
@Composable
fun DeveloperInfoDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        FajrCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Code,
                    contentDescription = null,
                    tint = FajrLoopColors.Primary,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = Spacing.sm)
                )

                Text(
                    text = "معلومات المطور 👨‍💻",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FajrLoopColors.Primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Saleh Bagomri",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = FajrLoopColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.md)
                )

                Surface(
                    color = FajrLoopColors.SurfaceVariant,
                    shape = RoundedCornerShape(Radius.md),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.lg)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                    ) {
                        Text(
                            text = "📧 البريد الإلكتروني: s.bagomri@gmail.com",
                            fontFamily = PpNmArabic,
                            fontSize = 13.sp,
                            color = FajrLoopColors.TextPrimary
                        )

                        Text(
                            text = "💬 واتساب: +967770727055",
                            fontFamily = PpNmArabic,
                            fontSize = 13.sp,
                            color = FajrLoopColors.TextPrimary
                        )

                        Text(
                            text = "🌐 الموقع الإلكتروني: www.bagomri.com",
                            fontFamily = PpNmArabic,
                            fontSize = 13.sp,
                            color = FajrLoopColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        FajrSecondaryButton(
                            text = "مراسلة المطور عبر الإيميل 📧",
                            onClick = { AppInfoUtils.sendSupportEmail(context, "s.bagomri@gmail.com") },
                            leadingIcon = Icons.Outlined.Email,
                            modifier = Modifier.fillMaxWidth()
                        )

                        FajrSecondaryButton(
                            text = "التواصل عبر واتساب 💬",
                            onClick = { AppInfoUtils.openWhatsApp(context, "+967770727055") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        FajrSecondaryButton(
                            text = "زيارة الموقع الإلكتروني 🌐",
                            onClick = { AppInfoUtils.openWebsite(context, "https://www.bagomri.com") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                FajrPrimaryButton(
                    text = "إغلاق",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * ℹ️ حوار حول التطبيق والإصدار الأصلي (About App & Version Dialog)
 */
@Composable
fun AboutAppDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val versionName = AppInfoUtils.getAppVersionName(context)
    val versionCode = AppInfoUtils.getAppVersionCode(context)

    Dialog(onDismissRequest = onDismiss) {
        FajrCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.xl)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mosque,
                    contentDescription = null,
                    tint = FajrLoopColors.Primary,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(bottom = Spacing.xs)
                )

                Text(
                    text = "حلقة الفجر — FajrLoop 🕌",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = FajrLoopColors.Primary,
                    textAlign = TextAlign.Center
                )

                Surface(
                    color = FajrLoopColors.PrimaryContainer,
                    shape = RoundedCornerShape(Radius.sm),
                    modifier = Modifier.padding(vertical = Spacing.xs)
                ) {
                    Text(
                        text = "الإصدار $versionName ($versionCode)",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = FajrLoopColors.Primary,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                Text(
                    text = "تطبيق إيماني جماعي يهدف إلى إعانة المسلمين على الاستيقاظ لصلاة الفجر في وقتها عبر حلقات التزام دائرية وتنبيهات موحدة وتحديات ذهنية وإيمانية.",
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = Spacing.lg)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    FajrSecondaryButton(
                        text = "مشاركة",
                        onClick = { AppInfoUtils.shareApp(context) },
                        leadingIcon = Icons.Outlined.Share,
                        modifier = Modifier.weight(1f)
                    )

                    FajrSecondaryButton(
                        text = "تقييم",
                        onClick = { AppInfoUtils.openPlayStore(context) },
                        leadingIcon = Icons.Outlined.Star,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "جميع الحقوق محفوظة © 2026 FajrLoop",
                    fontFamily = PpNmArabic,
                    fontSize = 11.sp,
                    color = FajrLoopColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )

                FajrPrimaryButton(
                    text = "إغلاق",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
