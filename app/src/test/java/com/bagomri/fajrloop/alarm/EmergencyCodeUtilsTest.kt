package com.bagomri.fajrloop.alarm

import org.junit.Test
import org.junit.Assert.*

class EmergencyCodeUtilsTest {

    @Test
    fun `generateTotpCode returns 6-digit string`() {
        val code = EmergencyCodeUtils.generateTotpCode("test-halqa-id")
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun `generateTotpCode is deterministic for same inputs`() {
        val halqaId = "halqa-123"
        val code1 = EmergencyCodeUtils.generateTotpCode(halqaId, 0L)
        val code2 = EmergencyCodeUtils.generateTotpCode(halqaId, 0L)
        assertEquals(code1, code2)
    }

    @Test
    fun `generateTotpCode differs for different windows`() {
        val halqaId = "halqa-123"
        val code0 = EmergencyCodeUtils.generateTotpCode(halqaId, 0L)
        val codeMinus1 = EmergencyCodeUtils.generateTotpCode(halqaId, -1L)
        val codePlus1 = EmergencyCodeUtils.generateTotpCode(halqaId, 1L)
        assertNotEquals(code0, codeMinus1)
        assertNotEquals(code0, codePlus1)
    }

    @Test
    fun `verifyTotpCode accepts current window`() {
        val halqaId = "test-halqa"
        val currentCode = EmergencyCodeUtils.generateTotpCode(halqaId, 0L)
        assertTrue(EmergencyCodeUtils.verifyTotpCode(currentCode, halqaId))
    }

    @Test
    fun `verifyTotpCode accepts previous window`() {
        val halqaId = "test-halqa"
        val prevCode = EmergencyCodeUtils.generateTotpCode(halqaId, -1L)
        assertTrue(EmergencyCodeUtils.verifyTotpCode(prevCode, halqaId))
    }

    @Test
    fun `verifyTotpCode rejects wrong code`() {
        val halqaId = "test-halqa"
        assertFalse(EmergencyCodeUtils.verifyTotpCode("000000", halqaId))
        assertFalse(EmergencyCodeUtils.verifyTotpCode("123456", halqaId))
    }

    @Test
    fun `verifyTotpCode rejects input with wrong length`() {
        val halqaId = "test-halqa"
        assertFalse(EmergencyCodeUtils.verifyTotpCode("12345", halqaId))   // 5 digits
        assertFalse(EmergencyCodeUtils.verifyTotpCode("1234567", halqaId)) // 7 digits
        assertFalse(EmergencyCodeUtils.verifyTotpCode("", halqaId))
    }

    @Test
    fun `formatTotpDisplay formats 6 digits correctly`() {
        val formatted = EmergencyCodeUtils.formatTotpDisplay("123456")
        assertEquals("123 456", formatted)
    }

    @Test
    fun `getRemainingSecondsInWindow returns value between 0 and 1800`() {
        val remaining = EmergencyCodeUtils.getRemainingSecondsInWindow()
        assertTrue(remaining in 0..1800)
    }

    @Test
    fun `generateTotpCode differs for different halqaIds`() {
        val code1 = EmergencyCodeUtils.generateTotpCode("halqa-aaa", 0L)
        val code2 = EmergencyCodeUtils.generateTotpCode("halqa-bbb", 0L)
        assertNotEquals(code1, code2)
    }
}
