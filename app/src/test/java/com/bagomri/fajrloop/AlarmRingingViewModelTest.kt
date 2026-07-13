package com.bagomri.fajrloop

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bagomri.fajrloop.ui.alarm.AlarmRingingViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class AlarmRingingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var viewModel: AlarmRingingViewModel

    @Before
    fun setUp() {
        application = Mockito.mock(Application::class.java)
        val prefs = Mockito.mock(android.content.SharedPreferences::class.java)
        Mockito.`when`(application.getSharedPreferences(
            com.bagomri.fajrloop.alarm.AlarmPreferences.PREFS_NAME,
            android.content.Context.MODE_PRIVATE
        )).thenReturn(prefs)

        viewModel = AlarmRingingViewModel(application)
    }

    @Test
    fun testMathPuzzleGeneration() {
        val easyQuestion = viewModel.generateMathQuestion("easy")
        assertNotNull(easyQuestion.first)
        assertTrue(easyQuestion.first.contains("+"))

        val hardQuestion = viewModel.generateMathQuestion("hard")
        assertNotNull(hardQuestion.first)
        assertTrue(hardQuestion.first.contains("*"))

        val mediumQuestion = viewModel.generateMathQuestion("medium")
        assertNotNull(mediumQuestion.first)
        assertTrue(mediumQuestion.first.contains("+") || mediumQuestion.first.contains("-"))
    }

    @Test
    fun testWordPuzzleGeneration() {
        val wordPair = viewModel.generateWordPuzzle()
        assertNotNull(wordPair.first)
        assertNotNull(wordPair.second)
        val originalSorted = wordPair.first.replace(" ", "").toCharArray().sorted()
        val scrambledSorted = wordPair.second.replace(" ", "").toCharArray().sorted()
        assertEquals(originalSorted, scrambledSorted)
    }

    @Test
    fun testTotpCodeVerification() {
        val halqaId = "test_halqa_123"
        val dateStr = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())
        val seed = (dateStr + halqaId).hashCode().let { if (it < 0) -it else it }
        val expected = (seed % 900000) + 100000

        val isValid = viewModel.verifyTotpCode(expected.toString(), halqaId)
        assertTrue(isValid)

        val isInvalid = viewModel.verifyTotpCode("123456", halqaId)
        if (expected != 123456) {
            assertFalse(isInvalid)
        }
    }
}
