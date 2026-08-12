package com.bagomri.fajrloop.alarm

import kotlin.math.absoluteValue

/**
 * EmergencyCodeUtils — توليد والتحقق من كود الطوارئ (TOTP)
 *
 * يعتمد على خوارزمية HMAC-SHA256 لمعالجة الـ sharedSecret مع نافذة الـ 30 دقيقة بدقة 100% أوفلاين.
 */
object EmergencyCodeUtils {

    private fun hmacSha256(key: String, data: String): String {
        return try {
            val mac = javax.crypto.Mac.getInstance("HmacSHA256")
            val secretKey = javax.crypto.spec.SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            mac.init(secretKey)
            val hash = mac.doFinal(data.toByteArray(Charsets.UTF_8))
            // نأخذ أول 4 bytes ونحوّلها لرقم 6 خانات
            val num = ((hash[0].toLong() and 0xFF) shl 24 or
                       ((hash[1].toLong() and 0xFF) shl 16) or
                       ((hash[2].toLong() and 0xFF) shl 8) or
                       (hash[3].toLong() and 0xFF))
            ((num % 900000L).absoluteValue + 100000L).toString()
        } catch (e: Exception) {
            // fallback للـ hashCode في حالة خطأ غير متوقع
            val seed = data.hashCode().absoluteValue
            ((seed % 900000) + 100000).toString()
        }
    }

    /**
     * توليد كود طوارئ مؤقت من 6 أرقام يتغير تلقائياً كل 30 دقيقة بناءً على معرّف الحلقة والـ sharedSecret
     *
     * @param halqaId معرّف الحلقة الخاص بالمستخدم
     * @param windowOffset الإزاحة بالدقائق (0 للحالي، -1 للسابقة، 1 للتالية)
     * @param sharedSecret المفتاح السري المشترك للحلقة
     */
    fun generateTotpCode(halqaId: String, windowOffset: Long = 0L, sharedSecret: String = ""): String {
        val now = System.currentTimeMillis()
        val windowIndex = (now / (30 * 60 * 1000L)) + windowOffset
        val data = "$halqaId-$windowIndex"

        return if (sharedSecret.isNotEmpty()) {
            hmacSha256(sharedSecret, data)
        } else {
            // fallback إذا لم يتوفر الـ secret
            val seed = data.hashCode().absoluteValue
            ((seed % 900000) + 100000).toString()
        }
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
    fun verifyTotpCode(userInput: String, halqaId: String, sharedSecret: String = ""): Boolean {
        val cleanInput = userInput.replace(" ", "").replace("-", "").trim()
        if (cleanInput.length != 6) return false

        for (offset in listOf(0L, -1L, 1L)) {
            val validCode = generateTotpCode(halqaId, offset, sharedSecret)
            if (cleanInput == validCode) return true
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
