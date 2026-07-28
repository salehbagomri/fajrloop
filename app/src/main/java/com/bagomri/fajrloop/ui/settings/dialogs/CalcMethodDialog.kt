package com.bagomri.fajrloop.ui.settings.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic

val calcMethodsList = listOf(
    "جامعة أم القرى (مكة المكرمة)",
    "رابطة العالم الإسلامي",
    "الهيئة المصرية العامة للمساحة",
    "جامعة العلوم الإسلامية بكراتشي",
    "الجمعية الإسلامية لأمريكا الشمالية (ISNA)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcMethodDialog(
    currentMethod: String,
    onMethodSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FajrLoopColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "طريقة حساب مواقيت الصلاة 🕌",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Gold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                calcMethodsList.forEach { method ->
                    val isSelected = currentMethod.contains(method.take(6)) || method == currentMethod

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onMethodSelect(method)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = method,
                                fontFamily = PpNmArabic,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isSelected) FajrLoopColors.Gold else FajrLoopColors.TextPrimary
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = FajrLoopColors.Gold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
