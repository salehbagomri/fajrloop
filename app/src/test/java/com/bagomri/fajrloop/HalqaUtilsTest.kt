package com.bagomri.fajrloop

import com.bagomri.fajrloop.data.HalqaUtils
import org.junit.Assert.*
import org.junit.Test

class HalqaUtilsTest {

    @Test
    fun testInviteCodeGeneration() {
        val code = HalqaUtils.generateInviteCode()
        assertNotNull(code)
        assertEquals(8, code.length)
        assertTrue(code.startsWith("FJR-"))

        val suffix = code.substring(4)
        assertEquals(4, suffix.length)
        val inviteChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        for (char in suffix) {
            assertTrue(inviteChars.contains(char))
        }
    }

    @Test
    fun testInviteCodeUniqueness() {
        val codes = mutableSetOf<String>()
        val count = 1000
        for (i in 0 until count) {
            codes.add(HalqaUtils.generateInviteCode())
        }
        // Assert that uniqueness holds with minimal collisions (allowing extremely rare random duplication)
        assertTrue(codes.size >= 995)
    }

    @Test
    fun testSharedSecretGeneration() {
        val secret = HalqaUtils.generateSharedSecret()
        assertNotNull(secret)
        assertEquals(16, secret.length)

        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        for (char in secret) {
            assertTrue(base32Chars.contains(char))
        }
    }
}
