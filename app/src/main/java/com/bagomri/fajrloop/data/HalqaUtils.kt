package com.bagomri.fajrloop.data

import java.security.SecureRandom

/**
 * HalqaUtils — أدوات مساعدة لتوليد كود الدعوة والمفتاح السري المشترك
 */
object HalqaUtils {

    private val random = SecureRandom()
    private val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray()
    private val INVITE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray()

    /**
     * توليد مفتاح سري مشترك بطول 16 حرفاً بنظام ترميز Base32 لرموز الطوارئ (TOTP)
     */
    fun generateSharedSecret(): String {
        val sb = StringBuilder(16)
        for (i in 0 until 16) {
            sb.append(BASE32_CHARS[random.nextInt(BASE32_CHARS.size)])
        }
        return sb.toString()
    }

    /**
     * توليد كود دعوة عشوائي فريد بصيغة FJR-XXXX
     */
    fun generateInviteCode(): String {
        val sb = StringBuilder(4)
        for (i in 0 until 4) {
            sb.append(INVITE_CHARS[random.nextInt(INVITE_CHARS.size)])
        }
        return "FJR-$sb"
    }
}
