package com.bagomri.fajrloop.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

/**
 * GlassCardView — بطاقة بتأثير الزجاج المعتم (Glassmorphism)
 *
 * مطابق مباشرة لمكوّن GlassCard في Flutter الأصلي (lib/widgets/glass_card.dart).
 *
 * المواصفات الدقيقة من design_tokens.md:
 *  • الخلفية  : #0D0D2B بشفافية 8% → يسمح بظهور الخلفية المتحركة خلفها
 *  • الحد     : 1.5dp، لون #25254A بشفافية 30%
 *  • الزوايا  : 24dp مستديرة
 *  • البريق   : تدرج أبيض شفاف في الأعلى لمحاكاة الزجاج الضبابي
 *
 * خاصيات ديناميكية (للبطاقات التفاعلية):
 *  • [setBorderColor] — تغيير لون الحد (عداد الفجر عند اقتراب الوقت)
 *  • [startPulse] / [stopPulse] — نبض scale 0.98 ↔ 1.02 (حين <5 دقائق)
 */
class GlassCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // ───────────────────── ثوابت المواصفات ─────────────────────

    companion object {
        private const val DEFAULT_CORNER_RADIUS_DP = 16f
        private const val STROKE_WIDTH_DP          = 1.5f
        // #0D0D2B عند 4% opacity  → argb(10, 13, 13, 43)
        private val DEFAULT_BG_COLOR     = Color.argb(10, 0x0D, 0x0D, 0x2B)
        // #25254A عند 30% opacity → argb(77, 37, 37, 74)
        private val DEFAULT_BORDER_COLOR = Color.argb(77, 0x25, 0x25, 0x4A)
    }

    // ───────────────────── Paints ─────────────────────

    /** خلفية شفافة داكنة (8% من surface) */
    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_BG_COLOR
        style = Paint.Style.FILL
    }

    /** حد خارجي (30% من border) */
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEFAULT_BORDER_COLOR
        style = Paint.Style.STROKE
    }

    /** بريق داخلي علوي — يحاكي انعكاس الضوء على الزجاج */
    private val innerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // ───────────────────── متغيرات الحجم ─────────────────────

    private var cornerRadiusDp = DEFAULT_CORNER_RADIUS_DP

    // ───────────────────── مساعدات الرسم ─────────────────────

    private val density get() = resources.displayMetrics.density
    private val cornerRadius get() = cornerRadiusDp * density
    private val strokeWidth get() = STROKE_WIDTH_DP * density

    private val clipPath  = Path()
    private val drawRect  = RectF()
    private val borderRect = RectF()

    // ───────────────────── أنيميشن النبض ─────────────────────

    private var pulseAnimator: ValueAnimator? = null

    // ───────────────────── تهيئة ─────────────────────

    init {
        setWillNotDraw(false)
        // Hardware layer لأداء أفضل مع الشفافية والرسم
        setLayerType(LAYER_TYPE_HARDWARE, null)
        // لا ظل ولا ارتفاع افتراضي
        elevation = 0f
        // الخلفية الأصلية شفافة (الرسم يتم في onDraw)
        setBackgroundColor(Color.TRANSPARENT)
    }

    // ───────────────────── قياس وتهيئة المسارات ─────────────────────

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        recalculatePath(w, h)
    }

    private fun recalculatePath(w: Int, h: Int) {
        val wf = w.toFloat()
        val hf = h.toFloat()
        val r  = cornerRadius
        val sw = strokeWidth

        // مسار القص للمحتوى
        clipPath.reset()
        clipPath.addRoundRect(0f, 0f, wf, hf, r, r, Path.Direction.CW)

        // مستطيل الرسم الكامل
        drawRect.set(0f, 0f, wf, hf)

        // مستطيل الحد (داخل نصف سُمك الخط لمنع القطع)
        val sw2 = sw / 2f
        borderRect.set(sw2, sw2, wf - sw2, hf - sw2)
        borderPaint.strokeWidth = sw

        // تدرج البريق الداخلي: أبيض شفاف يتلاشى عند 35% من الارتفاع
        innerGlowPaint.shader = LinearGradient(
            0f, 0f,
            0f, hf * 0.35f,
            intArrayOf(
                Color.argb(22, 255, 255, 255),  // أبيض 8.6% في الأعلى
                Color.argb(0,  255, 255, 255)   // شفاف كلياً
            ),
            null,
            Shader.TileMode.CLAMP
        )
    }

    // ───────────────────── الرسم ─────────────────────

    override fun onDraw(canvas: Canvas) {
        val r  = cornerRadius
        val r2 = r - strokeWidth / 2f

        // 1. الخلفية الشفافة الداكنة (تسمح للتدرج المتحرك بالظهور خلفها)
        canvas.drawRoundRect(drawRect, r, r, bgPaint)

        // 2. البريق الداخلي العلوي (يحاكي تأثير الضوء على الزجاج الضبابي)
        canvas.drawRoundRect(drawRect, r, r, innerGlowPaint)

        // 3. الحد الخارجي الدقيق
        canvas.drawRoundRect(borderRect, r2, r2, borderPaint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        // قص جميع العناصر الداخلية بالشكل المستدير
        canvas.save()
        canvas.clipPath(clipPath)
        super.dispatchDraw(canvas)
        canvas.restore()
    }

    // ───────────────────── واجهة ديناميكية ─────────────────────

    /**
     * تغيير لون الحد ديناميكياً (مثلاً عند اقتراب وقت الفجر).
     *
     * @param argbColor اللون بصيغة ARGB (يتضمن الشفافية)
     */
    fun setBorderColor(argbColor: Int) {
        borderPaint.color = argbColor
        invalidate()
    }

    /**
     * تغيير لون الخلفية والحد معاً بشكل مخصص.
     */
    fun setCustomBgAndBorder(bgColor: Int, borderColor: Int) {
        bgPaint.color = bgColor
        borderPaint.color = borderColor
        invalidate()
    }

    /** إعادة الحد للون الافتراضي */
    fun resetBorderColor() {
        borderPaint.color = DEFAULT_BORDER_COLOR
        bgPaint.color = DEFAULT_BG_COLOR
        invalidate()
    }

    /** تعيين انحناء الزوايا بالـ DP ديناميكياً */
    fun setCornerRadiusDp(dp: Float) {
        cornerRadiusDp = dp
        recalculatePath(width, height)
        invalidate()
    }

    /**
     * تشغيل نبض scale 0.98 ↔ 1.02 (حين تبقى أقل من 5 دقائق على الفجر).
     * مدة: 1000ms، منحنى: easeInOut
     */
    fun startPulse() {
        if (pulseAnimator?.isRunning == true) return
        pulseAnimator = ValueAnimator.ofFloat(0.98f, 1.02f).apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val s = anim.animatedValue as Float
                scaleX = s
                scaleY = s
            }
            start()
        }
    }

    /** إيقاف النبض وإعادة الحجم الطبيعي */
    fun stopPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        scaleX = 1f
        scaleY = 1f
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pulseAnimator?.cancel()
    }
}
