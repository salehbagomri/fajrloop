package com.bagomri.fajrloop.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.ui.components.AnimatedGradientBackground
import com.bagomri.fajrloop.ui.components.GlassCard
import com.bagomri.fajrloop.ui.components.GoldButton
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedGradientBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo & Header
            Text(
                text = "🌙",
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "حلقة الفجر",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = FajrLoopColors.Gold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "استيقظ لصلاة الفجر جماعة مع أصدقائك",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = FajrLoopColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 36.dp)
            )

            // Sign-in card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل الدخول",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = FajrLoopColors.TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "قم بتسجيل الدخول بحساب Google للبدء في استخدام التطبيق والإنضمام للحلقات",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = FajrLoopColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = FajrLoopColors.Gold,
                            modifier = Modifier
                                .size(44.dp)
                                .padding(vertical = 4.dp)
                        )
                    } else {
                        GoldButton(
                            text = "تسجيل الدخول عبر Google  🔍",
                            onClick = onGoogleSignInClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    FajrLoopTheme {
        LoginScreen(
            onGoogleSignInClick = {},
            isLoading = false
        )
    }
}
