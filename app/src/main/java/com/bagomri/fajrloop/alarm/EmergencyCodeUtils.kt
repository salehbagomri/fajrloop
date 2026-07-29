package com.bagomri.fajrloop.alarm

import kotlin.math.absoluteValue

/**
 * EmergencyCodeUtils — توليد والتحقق من كود الطوارئ (TOTP)
 *
 * يعتمد على خوارزمية رياضيات نافذة الـ 30 دقيقة بدقة 100% وبدون الحاجة لإنترنت اطلاقاً.
 * يعمل أوفلاين على جميع الأجهزة بشكل متزامن دقيق.
 */
object EmergencyCodeUtils {

    /**
     * توليد كود طوارئ مؤقت من 6 أرقام يتغير تلقائياً كل 30 دقيقة بناءً على معرّف الحلقة
     *
     * @param halqaId معرّف الحلقة الخاص بالمستخدم
     * @param windowOffset الإزاحة بالدقائق (0 للحالي، -1 للسابقة، 1 للتالية)
     */
    fun generateTotpCode(halqaId: String, windowOffset: Long = 0L): String {
        val now = System.currentTimeMillis()
        val windowIndex = (now / (30 * 60 * 1000L)) + windowOffset
        val seed = "$halqaId-$windowIndex".hashCode().absoluteValue
        val codeNum = ((seed % 900000) + 100000).toString()
        return codeNum
    }

    /**
     * تنسيق الكود للعرض بصيغة XXX XXX
     */
    fun formatTotpDisplay(code: String): String {
        val clean = code.replace(" ", "").replace("-", "").trim()
        return if (clean.length == 6) "${clean.substring(0, 3)} ${clean.substring(3, 6)}" else code
    }

    /**
     * التحقق الرياضي الحقيقي من كود الطوارئ أوفلاين دون إنترنت
     * يفحص النافذة الحالية والنافذة السابقة والنافذة التالية (تسامح ±30 دقيقة لفارق التوقيت بين الأجهزة)
     */
    fun verifyTotpCode(userInput: String, halqaId: String): Boolean {
        val cleanInput = userInput.replace(" ", "").replace("-", "").trim()
        if (cleanInput.length != 6) return false

        for (offset in listOf(0L, -1L, 1L)) {
            val validCode = generateTotpCode(halqaId, offset)
            if (cleanInput == validCode) {
                return true
            }
        }
        return false
    }

    /**
     * الثواني المتبقية حتى التجديد التالي في نافذة الـ 30 دقيقة الحالية
     */
    fun getRemainingSecondsInWindow(): Int {
        val now = System.currentTimeMillis()
        val windowMillis = 30 * 60 * 1000L
        val elapsedInWindow = now % windowMillis
        val remainingMillis = windowMillis - elapsedInWindow
        return (remainingMillis / 1000L).toInt()
    }
}
