package com.bagomri.fajrloop.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bagomri.fajrloop.data.*
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val prayerTimesRepository = PrayerTimesRepository(application)

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile
    private val _userProfileFlow = MutableStateFlow<UserProfile?>(null)
    val userProfileFlow: StateFlow<UserProfile?> = _userProfileFlow.asStateFlow()

    private var userProfileListener: ValueEventListener? = null

    init {
        val uid = userRepository.getUserId()
        if (uid != null) {
            userProfileListener = userRepository.observeUserProfile(uid) { profile ->
                _userProfile.value = profile
                _userProfileFlow.value = profile
            }
        }
    }


    /**
     * تحديث الإعدادات سحابياً
     */
    fun updateUserSettings(settings: UserSettings, onComplete: (Boolean) -> Unit) {
        val uid = userRepository.getUserId() ?: return
        userRepository.updateUserSettings(uid, settings, onComplete)
    }

    /**
     * تحديث الموقع الجغرافي سحابياً ومحلياً
     */
    fun updateUserLocation(location: UserLocation, onComplete: (Boolean) -> Unit) {
        val uid = userRepository.getUserId() ?: return
        userRepository.updateUserLocation(uid, location) { success ->
            if (success) {
                val method = _userProfile.value?.settings?.prayerCalcMethod ?: "umm_al_qura"
                prayerTimesRepository.saveLocationAndMethod(location.latitude, location.longitude, location.cityName, method)
            }
            onComplete(success)
        }
    }

    /**
     * حفظ طريقة الحساب وموقع الصلاة محلياً
     */
    fun saveLocalLocationAndMethod(latitude: Double, longitude: Double, cityName: String, method: String) {
        prayerTimesRepository.saveLocationAndMethod(latitude, longitude, cityName, method)
    }

    override fun onCleared() {
        super.onCleared()
        userProfileListener?.let {
            val uid = userRepository.getUserId()
            if (uid != null) {
                userRepository.removeUserProfileObserver(uid, it)
            }
        }
    }
}
