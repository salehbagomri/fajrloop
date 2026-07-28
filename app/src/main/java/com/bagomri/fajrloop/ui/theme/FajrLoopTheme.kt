package com.bagomri.fajrloop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.bagomri.fajrloop.R

object FajrLoopColors {
    val Background     = Color(0xFF07071B)     // الخلفية الرئيسية
    val Surface        = Color(0xFF0D0D2B)     // سطح البطاقات
    val SurfaceBorder  = Color(0xFF25254A)     // حدود البطاقات
    val Gold           = Color(0xFFFFD700)     // الذهبي الرئيسي
    val GoldDark       = Color(0xFFB8A373)     // الذهبي الداكن
    val TextPrimary    = Color(0xFFE6EDF3)     // نص أبيض
    val TextSecondary  = Color(0xFFB0B0C5)     // نص رمادي
    val SuccessGreen   = Color(0xFF2ECC71)     // أخضر نجاح
    val DangerRed      = Color(0xFFE74C3C)     // أحمر خطر
    val NightBlue      = Color(0xFF1A1A3E)     // أزرق ليلي (خلفية متحركة)
    val NightPurple    = Color(0xFF4A1A6B)     // بنفسجي (خلفية متحركة)
    val Teal           = Color(0xFF1ABC9C)     // لون تمييز ثانوي
}

val PpNmArabic = FontFamily(
    Font(R.font.pp_nm_arabic_thin, FontWeight.Thin),
    Font(R.font.pp_nm_arabic_light, FontWeight.Light),
    Font(R.font.pp_nm_arabic_regular, FontWeight.Normal),
    Font(R.font.pp_nm_arabic_book, FontWeight.Normal),
    Font(R.font.pp_nm_arabic_medium, FontWeight.Medium),
    Font(R.font.pp_nm_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.pp_nm_arabic_bold, FontWeight.Bold)
)

val FajrLoopTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp,
        color = FajrLoopColors.TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = FajrLoopColors.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = FajrLoopColors.TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = FajrLoopColors.TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        color = FajrLoopColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        color = FajrLoopColors.TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        color = FajrLoopColors.TextSecondary
    ),
    bodyLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = FajrLoopColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = FajrLoopColors.TextPrimary
    ),
    bodySmall = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = FajrLoopColors.TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = FajrLoopColors.TextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = FajrLoopColors.TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = FajrLoopColors.TextSecondary
    )
)

private val DarkColorScheme = darkColorScheme(
    primary = FajrLoopColors.Gold,
    onPrimary = FajrLoopColors.Background,
    primaryContainer = FajrLoopColors.GoldDark,
    onPrimaryContainer = FajrLoopColors.TextPrimary,
    secondary = FajrLoopColors.Teal,
    onSecondary = FajrLoopColors.Background,
    surface = FajrLoopColors.Surface,
    onSurface = FajrLoopColors.TextPrimary,
    background = FajrLoopColors.Background,
    onBackground = FajrLoopColors.TextPrimary,
    error = FajrLoopColors.DangerRed,
    onError = Color.White
)

@Composable
fun FajrLoopTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = FajrLoopTypography,
            content = content
        )
    }
}
