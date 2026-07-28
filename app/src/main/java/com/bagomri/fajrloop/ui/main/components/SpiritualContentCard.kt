package com.bagomri.fajrloop.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import java.util.Calendar

@Composable
fun SpiritualContentCard(
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Verse, 1 = Hadith

    val ayat = listOf(
        Pair("أَقِمِ الصَّلَاةَ لِدُلُوكِ الشَّمْسِ إِلَىٰ غَسَقِ اللَّيْلِ وَقُرْآنَ الْفَجْرِ ۖ إِنَّ قُرْآنَ الْفَجْرِ كَانَ مَشْهُودًا", "سورة الإسراء: 78"),
        Pair("وَسَبِّحْ بِحَمْدِ رَبِّكَ قَبْلَ طُلُوعِ الشَّمْسِ وَقَبْلَ غُرُوبِهَا", "سورة طه: 130"),
        Pair("وَالْفَجْرِ * وَلَيَالٍ عَشْرٍ", "سورة الفجر: 1-2")
    )

    val ahadith = listOf(
        Pair("رَكْعَتَا الْفَجْرِ خَيْرٌ مِنَ الدُّنْيَا وَمَا فِيهَا.", "صحيح مسلم"),
        Pair("مَنْ صَلَّى الصُّبْحَ فَهُوَ فِي ذِمَّةِ اللَّهِ.", "صحيح مسلم"),
        Pair("لَنْ يَلِجَ النَّارَ أَحَدٌ صَلَّى قَبْلَ طُلُوعِ الشَّمْسِ وَقَبْلَ غُرُوبِهَا.", "صحيح مسلم")
    )

    val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    val currentContent = if (selectedTab == 0) {
        ayat[dayIndex % ayat.size]
    } else {
        ahadith[dayIndex % ahadith.size]
    }

    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TabPill(
                    text = "آية اليوم 📖",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                Spacer(modifier = Modifier.width(8.dp))
                TabPill(
                    text = "حديث اليوم 📜",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }

            Text(
                text = if (selectedTab == 0) "» ${currentContent.first} «" else "« ${currentContent.first} »",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = FajrLoopColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            Text(
                text = currentContent.second,
                fontFamily = PpNmArabic,
                fontSize = 12.sp,
                color = FajrLoopColors.Gold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TabPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) FajrLoopColors.Gold.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) FajrLoopColors.Gold.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = PpNmArabic,
            fontSize = 12.sp,
            color = if (isSelected) FajrLoopColors.Gold else FajrLoopColors.TextSecondary
        )
    }
}
