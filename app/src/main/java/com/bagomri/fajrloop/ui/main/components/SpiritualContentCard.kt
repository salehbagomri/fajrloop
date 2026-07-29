package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing
import java.util.Calendar

@Composable
fun SpiritualContentCard(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Verse, 1 = Hadith

    val ayat = listOf(
        Pair("أَقِمِ الصَّلَاةَ لِدُلُوكِ الشَّمْسِ إِلَىٰ غَسَقِ اللَّيْلِ وَقُرْآنَ الْفَجْرِ ۖ إِنَّ قُرْآنَ الْفَجْرِ كَانَ مَشْهُودًا", "الإسراء: 78"),
        Pair("وَسَبِّحْ بِحَمْدِ رَبِّكَ قَبْلَ طُلُوعِ الشَّمْسِ وَقَبْلَ غُرُوبِهَا", "طه: 130"),
        Pair("وَالْفَجْرِ ∘ وَلَيَالٍ عَشْرٍ", "الفجر: 1-2"),
        Pair("حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ وَقُومُوا لِلَّهِ قَانِتِينَ", "البقرة: 238"),
        Pair("إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَّوقُوتًا", "النساء: 103"),
        Pair("وَأَقِمِ الصَّلَاةَ ۖ إِنَّ الصَّلَاةَ تَنْهَىٰ عَنِ الْفَحْشَاءِ وَالْمُنكَرِ", "العنكبوت: 45"),
        Pair("وَاسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ وَإِنَّهَا لَكَبِيرَةٌ إِلَّا عَلَى الْخَاشِعِينَ", "البقرة: 45"),
        Pair("فَسُبْحَانَ اللَّهِ حِينَ تُمْسُونَ وَحِينَ تُصْبِحُونَ", "الروم: 17"),
        Pair("يَا أَيُّهَا الَّذِينَ آمَنُوا اسْتَعِينُوا بِالصَّبْرِ وَالصَّلَاةِ ۚ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ", "البقرة: 153"),
        Pair("وَمِنَ اللَّيْلِ فَتَهَجَّدْ بِهِ نَافِلَةً لَّكَ عَسَىٰ أَن يَبْعَثَكَ رَبُّكَ مَقَامًا مَّحْمُودًا", "الإسراء: 79")
    )

    val ahadith = listOf(
        Pair("رَكْعَتَا الْفَجْرِ خَيْرٌ مِنَ الدُّنْيَا وَمَا فِيهَا", "صحيح مسلم"),
        Pair("مَنْ صَلَّى الصُّبْحَ فَهُوَ فِي ذِمَّةِ اللَّهِ", "صحيح مسلم"),
        Pair("لَنْ يَلِجَ النَّارَ أَحَدٌ صَلَّى قَبْلَ طُلُوعِ الشَّمْسِ وَقَبْلَ غُرُوبِهَا", "صحيح مسلم"),
        Pair("أثقل الصلاة على المنافقين صلاة العشاء وصلاة الفجر", "متفق عليه"),
        Pair("بشّر المشّائين في الظُّلَم إلى المساجد بالنور التام يوم القيامة", "صحيح أبي داود"),
        Pair("لو يعلمون ما في العتمة والصبح لأتوهما ولو حبواً", "متفق عليه"),
        Pair("من صلّى الفجر في جماعة فكأنما صلّى الليل كله", "صحيح مسلم"),
        Pair("إن أول ما يحاسب به العبد يوم القيامة من عمله صلاته", "صحيح أبي داود"),
        Pair("الصلاة على وقتها، ثم بر الوالدين، ثم الجهاد في سبيل الله", "متفق عليه"),
        Pair("ما من صلاة مفروضة إلا بين يديها ركعتان", "صحيح ابن حبان")
    )

    val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    val currentContent = if (selectedTab == 0) {
        ayat[dayIndex % ayat.size]
    } else {
        ahadith[dayIndex % ahadith.size]
    }

    FajrCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tab selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.md),
                horizontalArrangement = Arrangement.Center
            ) {
                TabChip(
                    text = "آية اليوم",
                    icon = Icons.Outlined.MenuBook,
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                TabChip(
                    text = "حديث اليوم",
                    icon = Icons.Outlined.WbTwilight,
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }

            Text(
                text = "﴿ ${currentContent.first} ﴾",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = FajrLoopColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.sm)
            )

            Text(
                text = currentContent.second,
                fontFamily = PpNmArabic,
                fontSize = 12.sp,
                color = FajrLoopColors.Primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TabChip(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) FajrLoopColors.PrimaryContainer else Color.Transparent,
                shape = RoundedCornerShape(Radius.sm)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) FajrLoopColors.Primary.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(Radius.sm)
            )
            .clickable { onClick() }
            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) FajrLoopColors.Primary else FajrLoopColors.TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontFamily = PpNmArabic,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) FajrLoopColors.Primary else FajrLoopColors.TextSecondary
            )
        }
    }
}
