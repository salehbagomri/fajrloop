package com.bagomri.fajrloop.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

/**
 * نظام الحركة والانتقالات الموحد
 */
object Motion {
    // ── المدد (Durations) ───────────────────────────
    /** Ripple, Press feedback */
    const val DurationInstant = 100

    /** Fade in/out, Switch toggle */
    const val DurationFast = 200

    /** Screen transition, Card expand */
    const val DurationMedium = 300

    /** BottomSheet, Full-screen overlay */
    const val DurationSlow = 500

    // ── منحنيات الحركة (Easing Curves) ──────────────
    /** انتقالات عامة */
    val EaseStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** دخول عنصر جديد */
    val EaseEnter = CubicBezierEasing(0f, 0f, 0f, 1f)

    /** خروج عنصر */
    val EaseExit = CubicBezierEasing(0.2f, 0f, 1f, 1f)
}
