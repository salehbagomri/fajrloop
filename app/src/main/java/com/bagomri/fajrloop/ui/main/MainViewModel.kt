package com.bagomri.fajrloop.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val halqaRepository = HalqaRepository()
    private val alarmRepository = AlarmRepository(application)
    private val prayerTimesRepository = PrayerTimesRepository(application)

    // UI state LiveData
    private val _userProfile = MutableLiveData<UserProfile?>().apply {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString("cached_user_display_name", null)
        val photo = prefs.getString("cached_user_photo_url", "")
        val halqaId = prefs.getString("current_halqa_id", "")
        if (name != null) {
            value = UserProfile(
                displayName = name,
                photoUrl = photo ?: "",
                currentHalqaId = halqaId ?: ""
            )
        }
    }
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val _halqaId = MutableLiveData<String?>().apply {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        value = prefs.getString("current_halqa_id", null)
    }
    val halqaId: LiveData<String?> = _halqaId

    private val _halqaName = MutableLiveData<String>().apply {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        value = prefs.getString("current_halqa_name", "") ?: ""
    }
    val halqaName: LiveData<String> = _halqaName

    private val _isCurrentUserAdmin = MutableLiveData<Boolean>()
    val isCurrentUserAdmin: LiveData<Boolean> = _isCurrentUserAdmin

    private val _loopMembers = MutableLiveData<List<LoopMemberItem>>()
    val loopMembers: LiveData<List<LoopMemberItem>> = _loopMembers

    private val _todaySummaryText = MutableLiveData<String>().apply {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        value = prefs.getString("cached_today_summary_text", "") ?: ""
    }
    val todaySummaryText: LiveData<String> = _todaySummaryText

    private val _awakeCountText = MutableLiveData<String>().apply {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        value = prefs.getString("cached_awake_count_text", "") ?: ""
    }
    val awakeCountText: LiveData<String> = _awakeCountText

    private val _friendWakeAlert = MutableLiveData<FriendWakeAlert?>()
    val friendWakeAlert: LiveData<FriendWakeAlert?> = _friendWakeAlert

    // Countdown and Prayer times
    private val _fajrTimeStr = MutableLiveData<String>()
    val fajrTimeStr: LiveData<String> = _fajrTimeStr

    private val _sunriseTimeStr = MutableLiveData<String>()
    val sunriseTimeStr: LiveData<String> = _sunriseTimeStr

    private val _countdownText = MutableLiveData<String>()
    val countdownText: LiveData<String> = _countdownText

    private val _countdownColor = MutableLiveData<String>()
    val countdownColor: LiveData<String> = _countdownColor

    private val _countdownCardBorderMode = MutableLiveData<Int>() // 0: default, 1: gold, 2: red, 3: red pulse
    val countdownCardBorderMode: LiveData<Int> = _countdownCardBorderMode

    private var userProfileListener: ValueEventListener? = null
    private var halqaListener: ValueEventListener? = null
    private var dailyRecordsListener: ValueEventListener? = null

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null

    init {
        val uid = userRepository.getUserId()
        if (uid != null) {
            userProfileListener = userRepository.observeUserProfile(uid) { profile ->
                _userProfile.value = profile
                val hId = if (profile != null && profile.currentHalqaId.isNotEmpty()) profile.currentHalqaId else null
                _halqaId.value = hId

                // مزامنة مع SharedPreferences
                val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString("current_halqa_id", hId)
                if (profile != null) {
                    editor.putString("cached_user_display_name", profile.displayName)
                    editor.putString("cached_user_photo_url", profile.photoUrl)
                } else {
                    editor.remove("cached_user_display_name")
                    editor.remove("cached_user_photo_url")
                }
                editor.apply()
            }

            halqaListener = halqaRepository.observeUserHalqa { snapshot ->
                val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                if (snapshot == null || !snapshot.exists()) {
                    _halqaName.value = ""
                    _isCurrentUserAdmin.value = false
                    _loopMembers.value = emptyList()
                    _todaySummaryText.value = ""
                    _awakeCountText.value = ""
                    _friendWakeAlert.value = null
                    stopObservingDailyRecords()

                    // مسح المعرف والاسم والإحصائيات في SharedPreferences
                    prefs.edit()
                        .remove("current_halqa_id")
                        .remove("current_halqa_name")
                        .remove("cached_awake_count_text")
                        .remove("cached_today_summary_text")
                        .apply()
                } else {
                    val name = snapshot.child("name").value as? String ?: "حلقة"
                    _halqaName.value = name

                    val chain = (snapshot.child("chain").value as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList()

                    val membersSnap = snapshot.child("members")
                    val currentUid = userRepository.getUserId()
                    val isAdmin = membersSnap.child(currentUid ?: "").child("role").value as? String == "admin"
                    _isCurrentUserAdmin.value = isAdmin

                    val halqaId = snapshot.key!!
                    // حفظ المعرف والاسم في SharedPreferences
                    prefs.edit()
                        .putString("current_halqa_id", halqaId)
                        .putString("current_halqa_name", name)
                        .apply()

                    startObservingDailyRecords(halqaId, chain, membersSnap)
                }
            }
        }
        startFajrCountdown()
    }

    private fun startObservingDailyRecords(halqaId: String, chain: List<String>, membersSnap: DataSnapshot) {
        stopObservingDailyRecords()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val recordsRef = FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)

        dailyRecordsListener = recordsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updateChainAndSummary(chain, membersSnap, snapshot)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun stopObservingDailyRecords() {
        dailyRecordsListener?.let {
            val currentId = _userProfile.value?.currentHalqaId
            if (!currentId.isNullOrEmpty()) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                FirebaseDatabase.getInstance()
                    .getReference("dailyRecords")
                    .child(currentId)
                    .child(currentDate)
                    .removeEventListener(it)
            }
        }
        dailyRecordsListener = null
    }

    private fun updateChainAndSummary(
        chain: List<String>,
        membersSnap: DataSnapshot,
        recordsSnap: DataSnapshot
    ) {
        val currentUid = userRepository.getUserId() ?: ""
        val membersList = mutableListOf<LoopMemberItem>()
        var awakeCount = 0
        var alertFriend: FriendWakeAlert? = null

        for (mId in chain) {
            val mSnap = membersSnap.child(mId)
            if (!mSnap.exists()) continue

            val displayName = mSnap.child("displayName").value as? String ?: "عضو"
            val photoUrl = mSnap.child("photoUrl").value as? String ?: ""
            val responsibleForUserId = mSnap.child("responsibleForUserId").value as? String ?: ""

            var status = "pending"
            val profileStatus = mSnap.child("status").value as? String
            if (profileStatus == "travel" || profileStatus == "traveling") {
                status = "travel"
            } else if (recordsSnap.child(mId).exists()) {
                status = recordsSnap.child(mId).child("status").value as? String ?: "pending"
            }

            if (status == "awake") {
                awakeCount++
            }

            if (status == "challenge_done" && responsibleForUserId == currentUid) {
                val firstName = displayName.split(" ").first()
                alertFriend = FriendWakeAlert(
                    uid = mId,
                    displayName = firstName,
                    message = "صديقك $firstName حل تحدي الاستيقاظ وبانتظار تأكيدك لإيقاف منبهه."
                )
            }

            membersList.add(
                LoopMemberItem(
                    userId = mId,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    status = status,
                    isCurrentUser = mId == currentUid
                )
            )
        }

        _loopMembers.value = membersList
        _friendWakeAlert.value = alertFriend

        val total = chain.size
        val countText = "$awakeCount / $total"
        _awakeCountText.value = countText
        val summaryText = if (awakeCount == total && total > 0) {
            "ما شاء الله! استيقظت الحلقة بالكامل 🎉"
        } else {
            "استيقظ $awakeCount من أصل $total أعضاء حتى الآن."
        }
        _todaySummaryText.value = summaryText

        // حفظ في SharedPreferences للمرة القادمة
        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("cached_awake_count_text", countText)
            .putString("cached_today_summary_text", summaryText)
            .apply()
    }

    fun confirmFriendWake(friendUid: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = userRepository.getUserId() ?: return
        val halqaId = _userProfile.value?.currentHalqaId ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .child(friendUid)
            .updateChildren(mapOf(
                "status" to "awake",
                "confirmedBy" to currentUid
            ))
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage)
            }
    }

    fun startFajrCountdown() {
        countdownRunnable?.let { handler.removeCallbacks(it) }

        val now = System.currentTimeMillis()
        var prayerTimes = prayerTimesRepository.getPrayerTimesForDate(Date())
        if (prayerTimes.fajr < now) {
            val tomorrow = Date(now + 86_400_000L)
            prayerTimes = prayerTimesRepository.getPrayerTimesForDate(tomorrow)
        }

        val arLocale = Locale.forLanguageTag("ar")
        val timeFormat = SimpleDateFormat("hh:mm a", arLocale)
        _fajrTimeStr.value = timeFormat.format(Date(prayerTimes.fajr))
        _sunriseTimeStr.value = timeFormat.format(Date(prayerTimes.sunrise))

        val config = alarmRepository.getAlarmConfig()
        if (config.enabled) {
            val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            val todayTimes = prayerTimesRepository.getPrayerTimesForDate(Date())
            val tomorrowTimes = prayerTimesRepository.getPrayerTimesForDate(Date(System.currentTimeMillis() + 86_400_000L))
            
            val type = prefs.getString("alarm_timing_type", "with") ?: "with"
            val offset = prefs.getInt("alarm_timing_offset_minutes", 0)
            val offsetMillis = offset * 60 * 1000L
            val adjustedToday = when (type) {
                "before" -> todayTimes.fajr - offsetMillis
                "after" -> todayTimes.fajr + offsetMillis
                else -> todayTimes.fajr
            }
            
            val targetAlarmTime = if (adjustedToday > System.currentTimeMillis()) {
                adjustedToday
            } else {
                when (type) {
                    "before" -> tomorrowTimes.fajr - offsetMillis
                    "after" -> tomorrowTimes.fajr + offsetMillis
                    else -> tomorrowTimes.fajr
                }
            }

            if (config.triggerTimeMillis != targetAlarmTime) {
                alarmRepository.saveAlarmConfig(config.copy(triggerTimeMillis = targetAlarmTime))
            }
        }

        countdownRunnable = object : Runnable {
            override fun run() {
                val remaining = prayerTimes.fajr - System.currentTimeMillis()
                if (remaining > 0) {
                    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(remaining)
                    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
                    val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
                    _countdownText.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                    val totalMinutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(remaining)
                    val textColor = when {
                        totalMinutes >= 60 -> "#2ECC71"
                        totalMinutes in 15..59 -> "#FFD700"
                        else -> "#E74C3C"
                    }
                    _countdownColor.value = textColor

                    val borderMode = when {
                        totalMinutes < 5 -> 3
                        totalMinutes < 15 -> 2
                        totalMinutes < 60 -> 1
                        else -> 0
                    }
                    _countdownCardBorderMode.value = borderMode
                } else {
                    _countdownText.value = "00:00:00"
                    _countdownColor.value = "#E74C3C"
                    _countdownCardBorderMode.value = 2
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(countdownRunnable!!)
    }

    override fun onCleared() {
        super.onCleared()
        countdownRunnable?.let { handler.removeCallbacks(it) }
        userProfileListener?.let {
            val uid = userRepository.getUserId()
            if (uid != null) {
                userRepository.removeUserProfileObserver(uid, it)
            }
        }
        halqaListener?.let {
            halqaRepository.removeObserver(it)
        }
        stopObservingDailyRecords()
    }
}

/**
 * LoopMemberItem — نموذج بيانات مبسط لعضو الحلقة يعرض بالواجهة
 */
data class LoopMemberItem(
    val userId: String,
    val displayName: String,
    val photoUrl: String,
    val status: String,
    val isCurrentUser: Boolean
)

/**
 * FriendWakeAlert — نموذج تنبيه استيقاظ الصديق المسؤولين عنه
 */
data class FriendWakeAlert(
    val uid: String,
    val displayName: String,
    val message: String
)
