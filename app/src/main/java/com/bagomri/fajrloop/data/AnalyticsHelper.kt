package com.bagomri.fajrloop.data

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * AnalyticsHelper — فئة مساعدة لتسجيل وتحليل أحداث التطبيق تلقائياً في Firebase Analytics
 */
object AnalyticsHelper {

    fun logAlarmTriggered(time: String, city: String) {
        val bundle = Bundle().apply {
            putString("trigger_time", time)
            putString("city", city)
        }
        Firebase.analytics.logEvent("alarm_triggered", bundle)
    }

    fun logChallengeSolved(type: String, difficulty: String, durationSeconds: Long) {
        val bundle = Bundle().apply {
            putString("challenge_type", type)
            putString("difficulty", difficulty)
            putLong("duration_seconds", durationSeconds)
        }
        Firebase.analytics.logEvent("challenge_solved", bundle)
    }

    fun logWakeConfirmed(durationSeconds: Long) {
        val bundle = Bundle().apply {
            putLong("duration_from_trigger_seconds", durationSeconds)
        }
        Firebase.analytics.logEvent("wake_confirmed", bundle)
    }

    fun logEmergencyPanic() {
        Firebase.analytics.logEvent("emergency_panic", null)
    }

    fun logHalqaCreated() {
        Firebase.analytics.logEvent("halqa_created", null)
    }

    fun logHalqaJoined() {
        Firebase.analytics.logEvent("halqa_joined", null)
    }
}
