package com.bagomri.fajrloop.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.ui.components.FajrBackground
import com.bagomri.fajrloop.ui.components.FajrCard
import com.bagomri.fajrloop.ui.components.FajrPrimaryButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo — أيقونة التطبيق الفعلية
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "شعار حلقة الفجر",
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = Spacing.lg)
            )

            Text(
                text = "حلقة الفجر",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = FajrLoopColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = Spacing.sm)
            )

            Text(
                text = "استيقظ لصلاة الفجر مع أصدقائك",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = FajrLoopColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = Spacing.section)
            )

            // Sign-in card
            FajrCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل الدخول",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp,
                        color = FajrLoopColors.TextPrimary,
                        modifier = Modifier.padding(bottom = Spacing.sm)
                    )

                    Text(
                        text = "سجّل دخولك بحساب Google للبدء",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = FajrLoopColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = Spacing.xxl)
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = FajrLoopColors.Primary,
                            modifier = Modifier
                                .size(44.dp)
                                .padding(vertical = Spacing.xs)
                        )
                    } else {
                        FajrPrimaryButton(
                            text = "سجّل الدخول بحساب Google",
                            onClick = onGoogleSignInClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))

            // Privacy notice
            Text(
                text = "بتسجيل الدخول، توافق على سياسة الخصوصية",
                fontFamily = PpNmArabic,
                fontSize = 11.sp,
                color = FajrLoopColors.TextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    FajrLoopTheme {
        LoginScreen(
            onGoogleSignInClick = {},
            isLoading = false
        )
    }
}
