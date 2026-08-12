package com.bagomri.fajrloop.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.data.UserProfile
import com.bagomri.fajrloop.data.UserRepository
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()

    private val initialProfile: UserProfile? = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(AlarmPreferences.KEY_CACHED_USER_DISPLAY_NAME, null)
        val photo = prefs.getString(AlarmPreferences.KEY_CACHED_USER_PHOTO_URL, "")
        val halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, "")
        if (name != null) UserProfile(displayName = name, photoUrl = photo ?: "", currentHalqaId = halqaId ?: "") else null
    }

    private val _userProfileFlow = MutableStateFlow<UserProfile?>(initialProfile)
    val userProfileFlow: StateFlow<UserProfile?> = _userProfileFlow.asStateFlow()

    private var userProfileListener: ValueEventListener? = null

    init {
        refreshUserData()
    }

    fun refreshUserData() {
        stopUserProfileObserver()
        val uid = userRepository.getUserId()
        if (uid != null) {
            userProfileListener = userRepository.observeUserProfile(uid) { profile ->
                _userProfileFlow.value = profile

                val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                if (profile != null) {
                    editor.putString(AlarmPreferences.KEY_CACHED_USER_DISPLAY_NAME, profile.displayName)
                    editor.putString(AlarmPreferences.KEY_CACHED_USER_PHOTO_URL, profile.photoUrl)
                    if (profile.currentHalqaId.isNotEmpty()) {
                        editor.putString(AlarmPreferences.KEY_CURRENT_HALQA_ID, profile.currentHalqaId)
                    }
                } else {
                    editor.remove(AlarmPreferences.KEY_CACHED_USER_DISPLAY_NAME)
                    editor.remove(AlarmPreferences.KEY_CACHED_USER_PHOTO_URL)
                }
                editor.apply()
            }
        }
    }

    fun clearUserDataOnLogout() {
        stopUserProfileObserver()
        _userProfileFlow.value = null

        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun stopUserProfileObserver() {
        userProfileListener?.let {
            val uid = userRepository.getUserId()
            if (uid != null) {
                userRepository.removeUserProfileObserver(uid, it)
            }
        }
        userProfileListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopUserProfileObserver()
    }
}
