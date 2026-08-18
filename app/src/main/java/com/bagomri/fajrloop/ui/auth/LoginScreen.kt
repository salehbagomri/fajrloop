package com.bagomri.fajrloop.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
    loadingMessage: String? = null,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        FajrBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = Spacing.xxl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // App Logo & Text Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app_logo),
                    contentDescription = "شعار حلقة الفجر",
                    modifier = Modifier
                        .size(210.dp)
                        .padding(bottom = Spacing.sm)
                )

                Text(
                    text = "حلقة الفجر",
                    fontFamily = PpNmArabic,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
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
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error message card (in-screen)
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                if (errorMessage != null) {
                    FajrCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = Spacing.md)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage,
                                fontFamily = PpNmArabic,
                                fontSize = 13.sp,
                                color = Color(0xFFFF6B6B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

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
                        LoadingSignInState(message = loadingMessage)
                    } else {
                        Button(
                            onClick = onGoogleSignInClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(Radius.md),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FajrLoopColors.Primary,
                                contentColor = Color(0xFF0D0B1A)
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
                                    color = Color(0xFF0D0B1A)
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

            Spacer(modifier = Modifier.height(Spacing.md))

            // Privacy notice
            Text(
                text = "بتسجيل الدخول، توافق على سياسة الخصوصية",
                fontFamily = PpNmArabic,
                fontSize = 12.sp,
                color = FajrLoopColors.TextTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xs))
        }
    }
}

@Composable
private fun LoadingSignInState(message: String?) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
        modifier = Modifier.padding(vertical = Spacing.xs)
    ) {
        CircularProgressIndicator(
            color = FajrLoopColors.Primary,
            strokeWidth = 3.dp,
            modifier = Modifier
                .size(44.dp)
                .scale(scale)
        )
        Text(
            text = message ?: "جاري تسجيل الدخول...",
            fontFamily = PpNmArabic,
            fontSize = 14.sp,
            color = FajrLoopColors.TextSecondary,
            textAlign = TextAlign.Center
        )
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

@Preview
@Composable
private fun LoginScreenLoadingPreview() {
    FajrLoopTheme {
        LoginScreen(
            onGoogleSignInClick = {},
            isLoading = true,
            loadingMessage = "جاري التحقق من هويتك..."
        )
    }
}


