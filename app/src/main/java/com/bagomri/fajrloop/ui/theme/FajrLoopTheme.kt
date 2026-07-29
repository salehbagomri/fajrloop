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

// ─────────────────────────────────────────────────────────────
// لوحة الألوان — Night → Dawn (ليل هادئ → فجر ذهبي)
// ─────────────────────────────────────────────────────────────
object FajrLoopColors {
    // ── الأسطح (Surfaces) ──
    /** خلفية التطبيق الرئيسية */
    val Background     = Color(0xFF080816)
    /** خلفية البطاقات والحاويات */
    val Surface        = Color(0xFF111128)
    /** خلفية عناصر ثانوية (TextField, Chip) */
    val SurfaceVariant = Color(0xFF1A1A3A)

    // ── الحدود (Borders) ──
    /** حدود البطاقات والعناصر */
    val Border         = Color(0xFF252548)
    /** حدود خفيفة (Dividers) */
    val BorderSubtle   = Color(0xFF1E1E40)

    // ── اللون الأساسي (Primary — الذهبي) ──
    /** اللون الذهبي الرئيسي — أزرار، عناوين مميزة */
    val Primary        = Color(0xFFD4A54A)
    /** ذهبي خافت — Hover/Disabled، نصوص ثانوية ذهبية */
    val PrimaryMuted   = Color(0xFFA88A3D)
    /** خلفية العناصر المميزة (Chip المحدد، Badge) */
    val PrimaryContainer = Color(0x1FD4A54A) // 12% alpha

    // ── النصوص (Text) ──
    /** النصوص الأساسية — عناوين، أسماء */
    val TextPrimary    = Color(0xFFE8ECF0)
    /** النصوص الوصفية والتوضيحية */
    val TextSecondary  = Color(0xFF8B8FA8)
    /** تلميحات، timestamps، تذييلات */
    val TextTertiary   = Color(0xFF5A5E78)

    // ── ألوان وظيفية (Semantic) ──
    /** حالة النجاح — استيقظ، صلاحية ممنوحة */
    val Success        = Color(0xFF34C759)
    /** تحذير — وقت قريب، صلاحية ناقصة */
    val Warning        = Color(0xFFFF9500)
    /** خطر / إيقاف — SOS، حذف، خروج */
    val Danger         = Color(0xFFFF3B30)
    /** معلومة / رابط */
    val Info           = Color(0xFF5AC8FA)

    // ── ألوان مساعدة (للتوافقية المؤقتة) ──
    @Deprecated("استخدم Primary بدلاً منه", replaceWith = ReplaceWith("Primary"))
    val Gold           = Primary
    @Deprecated("استخدم Danger بدلاً منه", replaceWith = ReplaceWith("Danger"))
    val DangerRed      = Danger
    @Deprecated("استخدم Success بدلاً منه", replaceWith = ReplaceWith("Success"))
    val SuccessGreen   = Success
    @Deprecated("استخدم Border بدلاً منه", replaceWith = ReplaceWith("Border"))
    val SurfaceBorder  = Border
}

// ─────────────────────────────────────────────────────────────
// عائلة الخط العربي
// ─────────────────────────────────────────────────────────────
val PpNmArabic = FontFamily(
    Font(R.font.pp_nm_arabic_thin, FontWeight.Thin),
    Font(R.font.pp_nm_arabic_light, FontWeight.Light),
    Font(R.font.pp_nm_arabic_regular, FontWeight.Normal),
    Font(R.font.pp_nm_arabic_book, FontWeight.Normal),
    Font(R.font.pp_nm_arabic_medium, FontWeight.Medium),
    Font(R.font.pp_nm_arabic_semibold, FontWeight.SemiBold),
    Font(R.font.pp_nm_arabic_bold, FontWeight.Bold)
)

// ─────────────────────────────────────────────────────────────
// سلم الطباعة — Major Second ratio (1.125)
// ─────────────────────────────────────────────────────────────
val FajrLoopTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PpNmArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    )
)

// ─────────────────────────────────────────────────────────────
// Material 3 Color Scheme
// ─────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = FajrLoopColors.Primary,
    onPrimary = Color(0xFF0D0B1A), // Dark text/icons on primary gold buttons
    primaryContainer = FajrLoopColors.PrimaryContainer,
    onPrimaryContainer = FajrLoopColors.TextPrimary,
    secondary = FajrLoopColors.Info,
    onSecondary = Color(0xFF0D0B1A),
    surface = FajrLoopColors.Surface,
    onSurface = FajrLoopColors.TextPrimary,
    surfaceVariant = FajrLoopColors.SurfaceVariant,
    onSurfaceVariant = FajrLoopColors.TextSecondary,
    background = FajrLoopColors.Background,
    onBackground = FajrLoopColors.TextPrimary,
    outline = FajrLoopColors.Border,
    outlineVariant = FajrLoopColors.BorderSubtle,
    error = FajrLoopColors.Danger,
    onError = Color.White
)

// ─────────────────────────────────────────────────────────────
// الثيم الرئيسي
// ─────────────────────────────────────────────────────────────
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
