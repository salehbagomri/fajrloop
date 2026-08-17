package com.bagomri.fajrloop.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BatteryChargingFull
import androidx.compose.material.icons.outlined.FlightTakeoff
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

data class GuideStepItem(
    val stepNumber: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

val officialGuideSteps = listOf(
    GuideStepItem(
        stepNumber = "١",
        title = "ما هي حلقة الفجر؟",
        description = "تطبيق إيماني تضامني لإعانة المسلمين على الاستيقاظ لصلاة الفجر في وقتها عبر حلقات دائرية مترابطة. ينضم الأعضاء برمز كود واحد، ويدق المنبه للجميع بأمان ومزامنة سحابية.",
        icon = Icons.Outlined.Group
    ),
    GuideStepItem(
        stepNumber = "٢",
        title = "توقيت الرنين الموحد للحلقة",
        description = "يُحدد توقيت رنين المنبه موحّداً لجميع أعضاء الحلقة بواسطة مسؤول الحلقة (Admin) فقط (مع الأذان مباشرة أو قبل الأذان بـ 10 دقائق)، وذلك لمنع التضارب وتأمين استيقاظ الجميع في نفس اللحظة.",
        icon = Icons.Outlined.Timer
    ),
    GuideStepItem(
        stepNumber = "٣",
        title = "رحلة الاستيقاظ والتعهد الإيماني",
        description = "فور رنين المنبه، يحل العضو تحدي الرياضيات لإنعاش عقله، ثم يقرأ دعاء الاستيقاظ («الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ»). بعد ذلك يكتب تعهده الإيماني يدوياً («أتعهد بأن أستيقظ لصلاة الفجر الآن والله على ما أقول شهيد») لينتقل الصوت إلى التكبيرات الفجرية الهادئة.",
        icon = Icons.Outlined.TaskAlt
    ),
    GuideStepItem(
        stepNumber = "٤",
        title = "تأخر الصديق والقيام التضامني",
        description = "إذا تأخر صديقك المسؤول عن إيقاظك، يوضح لك التطبيق إمكانية كتابة التعهد والقيام لصلاة الفجر فوراً دون انتظار، حتى لا تضيع عليك الفريضة.",
        icon = Icons.Outlined.HourglassTop
    ),
    GuideStepItem(
        stepNumber = "٥",
        title = "نمط السفر والظروف الشرعية",
        description = "عند السفر أو وجود عذر شرعي، يمكنك تفعيل «وضع السفر» مما يعفيك مؤقتاً من واجبات حلقة الاستيقاظ دون التأثير على تقييم الحلقة أو سلسلة التزامك.",
        icon = Icons.Outlined.FlightTakeoff
    ),
    GuideStepItem(
        stepNumber = "٦",
        title = "سجل الالتزام والإحصائيات",
        description = "يحسب التطبيق أيام التزامك المتواصلة (Streak)، والأوسمة الإيمانية، وترتيبك بين أعضاء الحلقة لزيادة المنافسة في الخيرات والطاعات.",
        icon = Icons.Outlined.BarChart
    ),
    GuideStepItem(
        stepNumber = "٧",
        title = "ضمان رنين المنبه وإعدادات البطارية",
        description = "للحفاظ على رنين المنبه حتى في وضع النوم العميق (Doze Mode)، يُفضل تفعيل صلاحية «استثناء البطارية» و «التشغيل التلقائي» من شاشة الإعدادات والصلاحيات.",
        icon = Icons.Outlined.BatteryChargingFull
    )
)

@Composable
fun GuideScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground(modifier = Modifier.fillMaxSize())

        Column(modifier = Modifier.fillMaxSize()) {
            FajrLoopTopBar(
                title = "دليل الاستخدام",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Header Banner Card
                FajrCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = FajrLoopColors.PrimaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                                    contentDescription = null,
                                    tint = FajrLoopColors.Primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.sm))

                        Text(
                            text = "دليل الاستخدام والتشغيل الرسمي 🕌",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = FajrLoopColors.Primary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "دليلك الميسّر لربط حلقات الاستيقاظ، التعهد الإيماني، والالتزام بدقة لصلاة الفجر في وقتها.",
                            fontFamily = PpNmArabic,
                            fontSize = 13.sp,
                            color = FajrLoopColors.TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = Spacing.xs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Guide Steps Cards
                officialGuideSteps.forEach { item ->
                    GuideStepCard(item = item)
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

@Composable
private fun GuideStepCard(item: GuideStepItem) {
    FajrCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.Top
        ) {
            // Step Number Badge with Icon
            Surface(
                color = FajrLoopColors.PrimaryContainer,
                shape = RoundedCornerShape(Radius.md),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = FajrLoopColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.title,
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FajrLoopColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = FajrLoopColors.SurfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.padding(start = Spacing.xs)
                    ) {
                        Text(
                            text = "الخطوة ${item.stepNumber}",
                            fontFamily = PpNmArabic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = FajrLoopColors.Primary,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = item.description,
                    fontFamily = PpNmArabic,
                    fontSize = 13.sp,
                    color = FajrLoopColors.TextSecondary,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = Spacing.xs)
                )
            }
        }
    }
}

@Preview
@Composable
private fun GuideScreenPreview() {
    FajrLoopTheme {
        GuideScreen(onBackClick = {})
    }
}
