package com.bagomri.fajrloop.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * AnimatedGradientView — خلفية متحركة بتدرج لوني دوّار
 *
 * مواصفات مأخوذة مباشرة من كود Flutter الأصلي (AnimatedGradientBg):
 *
 * الألوان (بالترتيب): #07071B → #1A1A3E → #4A1A6B → #07071B
 * المدة: 15 ثانية للدورة الكاملة
 * التكرار: لانهائي مع عكس الاتجاه (REVERSE)
 * المنحنى: خطي (Linear)
 *
 * آلية الحركة:
 * - لا يتم تدوير العنصر، بل تتحرك نقطتا البداية (start) والنهاية (end)
 *   للتدرج اللوني على زوايا الشاشة الأربع بالترتيب التالي (4 مراحل):
 *
 *   start: TopLeft → TopRight → BottomRight → BottomLeft → TopLeft
 *   end:   BottomRight → BottomLeft → TopLeft → TopRight → BottomRight
 */
class AnimatedGradientView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ألوان التدرج من design_tokens.md
    private val gradientColors = intArrayOf(
        Color.parseColor("#07071B"),   // background الداكن — بداية
        Color.parseColor("#1A1A3E"),   // primaryNight الأزرق الليلي
        Color.parseColor("#4A1A6B"),   // primaryPurple البنفسجي (لب الشعاع)
        Color.parseColor("#07071B")    // background الداكن — نهاية
    )

    // توزيع الألوان على مسافة التدرج
    private val gradientPositions = floatArrayOf(0f, 0.33f, 0.66f, 1f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // قيمة الأنيميشن الحالية (0.0 → 1.0)
    private var animProgress = 0f

    // الـ ValueAnimator: 15 ثانية، لانهائي، عكس، خطي
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 15_000L
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = LinearInterpolator()
        addUpdateListener { anim ->
            animProgress = anim.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        // تقسيم الـ 15 ثانية إلى 4 مراحل متساوية
        val progress4 = animProgress * 4f
        val phase = progress4.toInt().coerceIn(0, 3)
        val t = progress4 - phase   // 0.0 → 1.0 داخل كل مرحلة

        // حساب نقطة البداية (start) للتدرج
        val startX: Float
        val startY: Float
        when (phase) {
            0 -> { startX = lerp(0f, w, t); startY = 0f  }        // TopLeft  → TopRight
            1 -> { startX = w;               startY = lerp(0f, h, t) } // TopRight → BottomRight
            2 -> { startX = lerp(w, 0f, t); startY = h  }         // BottomRight → BottomLeft
            else -> { startX = 0f;           startY = lerp(h, 0f, t) } // BottomLeft → TopLeft
        }

        // حساب نقطة النهاية (end) للتدرج — معاكسة قطرياً
        val endX: Float
        val endY: Float
        when (phase) {
            0 -> { endX = lerp(w, 0f, t); endY = h  }         // BottomRight → BottomLeft
            1 -> { endX = 0f;              endY = lerp(h, 0f, t) } // BottomLeft → TopLeft
            2 -> { endX = lerp(0f, w, t); endY = 0f  }        // TopLeft → TopRight
            else -> { endX = w;            endY = lerp(0f, h, t) } // TopRight → BottomRight
        }

        // رسم التدرج اللوني المتحرك
        paint.shader = LinearGradient(
            startX, startY,
            endX, endY,
            gradientColors,
            gradientPositions,
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, paint)
    }

    /** دالة مساعدة: interpolation خطي بين قيمتين */
    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
