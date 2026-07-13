package com.bagomri.fajrloop.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase get() = FirebaseDatabase.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    fun isUserSignedIn(): Boolean = currentUser != null

    fun getUserId(): String? = currentUser?.uid

    fun signOut() {
        auth.signOut()
    }

    /**
     * مراقبة ملف تعريف المستخدم سحابياً بشكل فوري
     */
    fun observeUserProfile(userId: String, onUpdate: (UserProfile?) -> Unit): ValueEventListener {
        val userRef = database.getReference("users").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val uid = snapshot.child("uid").value as? String ?: ""
                    val displayName = snapshot.child("displayName").value as? String ?: ""
                    val email = snapshot.child("email").value as? String ?: ""
                    val photoUrl = snapshot.child("photoUrl").value as? String ?: ""
                    val currentHalqaId = snapshot.child("currentHalqaId").value as? String ?: ""
                    val timezone = snapshot.child("timezone").value as? String ?: ""

                    val locationSnap = snapshot.child("location")
                    val location = UserLocation(
                        latitude = (locationSnap.child("latitude").value as? Number)?.toDouble() ?: 14.5425,
                        longitude = (locationSnap.child("longitude").value as? Number)?.toDouble() ?: 49.1242,
                        cityName = locationSnap.child("cityName").value as? String ?: "المكلا"
                    )

                    val settingsSnap = snapshot.child("settings")
                    val settings = UserSettings(
                        prayerCalcMethod = settingsSnap.child("prayerCalcMethod").value as? String ?: "umm_al_qura",
                        alarmMinutesBefore = (settingsSnap.child("alarmMinutesBefore").value as? Number)?.toInt() ?: 0,
                        challengeType = settingsSnap.child("challengeType").value as? String ?: "math",
                        challengeDifficulty = settingsSnap.child("challengeDifficulty").value as? String ?: "medium",
                        alarmSound = settingsSnap.child("alarmSound").value as? String ?: "default",
                        vibration = settingsSnap.child("vibration").value as? Boolean ?: true,
                        travelMode = settingsSnap.child("travelMode").value as? Boolean ?: false,
                        travelModeExpiry = settingsSnap.child("travelModeExpiry").value as? String ?: "",
                        showMorningAdhkar = settingsSnap.child("showMorningAdhkar").value as? Boolean ?: true,
                        showDailyDua = settingsSnap.child("showDailyDua").value as? Boolean ?: true
                    )

                    onUpdate(UserProfile(uid, displayName, email, photoUrl, currentHalqaId, timezone, location, settings))
                } else {
                    onUpdate(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onUpdate(null)
            }
        }
        userRef.addValueEventListener(listener)
        return listener
    }

    /**
     * إزالة مستمع ملف تعريف المستخدم
     */
    fun removeUserProfileObserver(userId: String, listener: ValueEventListener) {
        database.getReference("users").child(userId).removeEventListener(listener)
    }

    /**
     * تحديث إعدادات المستخدم سحابياً
     */
    fun updateUserSettings(userId: String, settings: UserSettings, onComplete: (Boolean) -> Unit) {
        val settingsMap = mapOf(
            "prayerCalcMethod" to settings.prayerCalcMethod,
            "alarmMinutesBefore" to settings.alarmMinutesBefore,
            "challengeType" to settings.challengeType,
            "challengeDifficulty" to settings.challengeDifficulty,
            "alarmSound" to settings.alarmSound,
            "vibration" to settings.vibration,
            "travelMode" to settings.travelMode,
            "travelModeExpiry" to settings.travelModeExpiry,
            "showMorningAdhkar" to settings.showMorningAdhkar,
            "showDailyDua" to settings.showDailyDua
        )
        database.getReference("users").child(userId).child("settings").setValue(settingsMap)
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }

    /**
     * تحديث موقع المستخدم الجغرافي سحابياً
     */
    fun updateUserLocation(userId: String, location: UserLocation, onComplete: (Boolean) -> Unit) {
        val locationMap = mapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "cityName" to location.cityName
        )
        database.getReference("users").child(userId).child("location").setValue(locationMap)
            .addOnCompleteListener { task -> onComplete(task.isSuccessful) }
    }
}
