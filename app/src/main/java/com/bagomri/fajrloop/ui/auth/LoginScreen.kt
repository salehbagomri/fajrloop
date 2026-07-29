package com.bagomri.fajrloop.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.bagomri.fajrloop.ui.theme.FajrLoopColors
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.bagomri.fajrloop.ui.theme.PpNmArabic
import com.bagomri.fajrloop.ui.theme.Radius
import com.bagomri.fajrloop.ui.theme.Spacing

@Composable
fun LoginScreen(
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        FajrBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.xxl, vertical = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Spacing.xl))

            // App Logo — تكبير الشعار إلى 170dp
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "شعار حلقة الفجر",
                modifier = Modifier
                    .size(170.dp)
                    .padding(bottom = Spacing.sm)
            )

            Text(
                text = "حلقة الفجر",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = FajrLoopColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = Spacing.xs)
            )

            Text(
                text = "استيقظ لصلاة الفجر جماعة مع أصدقائك",
                fontFamily = PpNmArabic,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = FajrLoopColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            // دفع كارد تسجيل الدخول لأسفل الصفحة
            Spacer(modifier = Modifier.weight(1f))

            // Sign-in card (في أسفل الصفحة)
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
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = FajrLoopColors.TextPrimary,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )

                    Text(
                        text = "سجّل دخولك بحساب Google للبدء في استخدام التطبيق والإنضمام للحلقات",
                        fontFamily = PpNmArabic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = FajrLoopColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
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
                        Button(
                            onClick = onGoogleSignInClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(Radius.md),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FajrLoopColors.Primary,
                                contentColor = FajrLoopColors.Background
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "تسجيل الدخول عبر Google",
                                    fontFamily = PpNmArabic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = FajrLoopColors.Background
                                )
                                Spacer(modifier = Modifier.width(Spacing.md))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google",
                                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Privacy notice
            Text(
                text = "بتسجيل الدخول، توافق على سياسة الخصوصية",
                fontFamily = PpNmArabic,
                fontSize = 12.sp,
                color = FajrLoopColors.TextTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.sm))
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
