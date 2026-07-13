package com.bagomri.fajrloop.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.util.TimeZone

/**
 * AuthManager — المسؤول عن إدارة المصادقة بـ Firebase Auth وتأمين Google Sign-In
 *
 * يقوم بـ:
 * 1. فحص حالة المصادقة للمستخدم الحالي.
 * 2. ربط حساب جوجل بـ Firebase Auth.
 * 3. إنشاء ملف المستخدم الافتراضي في Firebase Realtime Database عند أول دخول.
 */
object AuthManager {

    private const val TAG = "AuthManager"
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase get() = FirebaseDatabase.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun isUserSignedIn(): Boolean = currentUser != null

    fun getUserId(): String? = currentUser?.uid

    /**
     * تسجيل الخروج من Firebase Auth
     */
    fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out successfully")
    }

    /**
     * إعداد ملف المستخدم أو تحديثه في قاعدة البيانات السحابية (Realtime Database)
     * يتم استدعاؤها فور تسجيل الدخول بنجاح.
     */
    fun checkOrCreateUserProfile(user: FirebaseUser, onComplete: (Boolean) -> Unit) {
        val uid = user.uid
        val userRef = database.getReference("users").child(uid)

        // التحقق من وجود المستخدم مسبقاً لتجنب تهيئة الحقول وإعادة تصفير الإعدادات
        userRef.child("uid").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.d(TAG, "User profile already exists in RTDB. Updating FCM Token and Timezone.")
                // تحديث الـ FCM token والمنطقة الزمنية فقط
                val updates = mapOf(
                    "timezone" to TimeZone.getDefault().id
                )
                userRef.updateChildren(updates)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to update basic user info", it)
                        onComplete(false)
                    }
            } else {
                Log.d(TAG, "Creating new user profile in RTDB...")
                // إنشاء ملف مستخدم جديد بالكامل بالقيم الافتراضية
                val newUserMap = mapOf(
                    "uid" to uid,
                    "displayName" to (user.displayName ?: "مستخدم جديد"),
                    "email" to (user.email ?: ""),
                    "photoUrl" to (user.photoUrl?.toString() ?: ""),
                    "fcmToken" to "",
                    "currentHalqaId" to "",
                    "timezone" to TimeZone.getDefault().id,
                    // آلية GPS Fallback بإحداثيات المكلا (حسب وثيقة قرارات البناء لكوتلن)
                    "location" to mapOf(
                        "latitude" to 14.5425,
                        "longitude" to 49.1242,
                        "cityName" to "المكلا"
                    ),
                    "settings" to mapOf(
                        "prayerCalcMethod" to "umm_al_qura",
                        "alarmMinutesBefore" to 0,
                        "challengeType" to "math",
                        "challengeDifficulty" to "medium",
                        "alarmSound" to "default",
                        "vibration" to true,
                        "travelMode" to false,
                        "travelModeExpiry" to "",
                        "showMorningAdhkar" to true,
                        "showDailyDua" to true
                    )
                )

                userRef.setValue(newUserMap)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ User profile created successfully in RTDB")
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "❌ Failed to create user profile in RTDB", it)
                        onComplete(false)
                    }
            }
        }.addOnFailureListener {
            Log.e(TAG, "Failed to fetch user snapshot from database", it)
            onComplete(false)
        }
    }
}
