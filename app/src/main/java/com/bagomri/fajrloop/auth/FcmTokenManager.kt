package com.bagomri.fajrloop.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

/**
 * FcmTokenManager — مسؤول عن تسجيل وتحديث وإلغاء تسجيل رموز الـ FCM للمستخدم
 */
object FcmTokenManager {
    private const val TAG = "FcmTokenManager"

    /**
     * جلب وتسجيل رمز الـ FCM للمستخدم الحالي سحابياً
     */
    fun registerToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d(TAG, "FCM registration token fetched: $token")
            saveTokenToDatabase(uid, token)
        }
    }

    /**
     * تسجيل رمز الـ FCM الممرر مباشرة للمستخدم الحالي سحابياً (يُستدعى من Service.onNewToken)
     */
    fun registerToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        saveTokenToDatabase(uid, token)
    }

    /**
     * مسح رمز الـ FCM عند تسجيل الخروج لمنع وصول إشعارات الحلقة لهذا الجهاز
     */
    fun unregisterToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("fcmToken").setValue("")
            .addOnSuccessListener {
                Log.d(TAG, "FCM token unregistered from database")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to unregister FCM token", e)
            }
    }

    private fun saveTokenToDatabase(uid: String, token: String) {
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("fcmToken").setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token registered successfully in database")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to register FCM token in database", e)
            }
    }
}
