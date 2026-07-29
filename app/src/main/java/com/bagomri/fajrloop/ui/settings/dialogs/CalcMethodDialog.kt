package com.bagomri.fajrloop.ui.settings.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

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
                .padding(Spacing.xl)
        ) {
            Text(
                text = "طريقة حساب مواقيت الصلاة",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = FajrLoopColors.Primary,
                modifier = Modifier.padding(bottom = Spacing.lg)
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                calcMethodsList.forEach { method ->
                    val isSelected = currentMethod.contains(method.take(6)) || method == currentMethod

                    FajrCard(
                        borderColor = if (isSelected) FajrLoopColors.Primary.copy(alpha = 0.5f) else FajrLoopColors.Border,
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
                                .padding(Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = method,
                                fontFamily = PpNmArabic,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isSelected) FajrLoopColors.Primary else FajrLoopColors.TextPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "محدد",
                                    tint = FajrLoopColors.Primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
        }
    }
}
