package com.bagomri.fajrloop.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.FajrLoopTopBar
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

val guideItems = listOf(
    Pair("1️⃣ ما هي حلقة الفجر؟", "تطبيق حلقة الفجر هو نظام تضامني لإيقاظ الأصدقاء صلاة الفجر جماعة. تقوم بإنشاء حلقة أو الانضمام إليها، ويدق المنبه للجميع في وقت الفجر معاً."),
    Pair("2️⃣ كيف يعمل منبه الاستيقاظ التضامني؟", "عند حلول وقت الفجر، يدق المنبه بصوت الأذان ومزود بتحدٍّ لإلغاء الرنين. بمجرد إكمال التحدي يظهر استيقاظك لباقي أعضاء الحلقة فوراً!"),
    Pair("3️⃣ ميزة الإنقاذ السريع 🦸", "إذا لم يستيقظ صديقك بعد دقائق من الأذان، يتلقى أعضاء الحلقة تنبيهاً بالاتصال لإنقاذه وإيقاظه قبل خروج الوقت."),
    Pair("4️⃣ وضع السفر ✈️", "إذا كنت مسافراً أو لديك عذر شرعي، يمكنك تفعيل وضع السفر حتى لا يتأثر تقييم الحلقة ولا يتلقى أصدقاؤك تنبيهات إنقاذ لك."),
    Pair("5️⃣ حماية المنبه ومنع الإغلاق", "يستخدم التطبيق صلاحيات قفل الشاشة والظهور فوق التطبيقات لمنع إغلاق المنبه بالخطأ وضمان استيقاظك الفعلي.")
)

@Composable
fun GuideScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            FajrLoopTopBar(
                title = "دليل الاستخدام 📖",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                guideItems.forEach { (title, body) ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = title,
                                fontFamily = PpNmArabic,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = FajrLoopColors.Gold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Text(
                                text = body,
                                fontFamily = PpNmArabic,
                                fontSize = 14.sp,
                                color = FajrLoopColors.TextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
