package com.bagomri.fajrloop.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val halqaRepository = HalqaRepository()
    private val alarmRepository = AlarmRepository(application)
    private val prayerTimesRepository = PrayerTimesRepository(application)

    // Initial state loading
    private val initialProfile: UserProfile? = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString("cached_user_display_name", null)
        val photo = prefs.getString("cached_user_photo_url", "")
        val halqaId = prefs.getString("current_halqa_id", "")
        if (name != null) UserProfile(displayName = name, photoUrl = photo ?: "", currentHalqaId = halqaId ?: "") else null
    }

    private val initialHalqaId: String? = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString("current_halqa_id", null)
    }

    private val initialHalqaName: String = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString("current_halqa_name", "") ?: ""
    }

    private val initialTodaySummary: String = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString("cached_today_summary_text", "") ?: ""
    }

    private val initialAwakeCount: String = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString("cached_awake_count_text", "") ?: ""
    }

    // StateFlows
    private val _userProfileFlow = MutableStateFlow<UserProfile?>(initialProfile)
    val userProfileFlow: StateFlow<UserProfile?> = _userProfileFlow.asStateFlow()

    private val _halqaIdFlow = MutableStateFlow<String?>(initialHalqaId)
    val halqaIdFlow: StateFlow<String?> = _halqaIdFlow.asStateFlow()

    private val _halqaNameFlow = MutableStateFlow<String>(initialHalqaName)
    val halqaNameFlow: StateFlow<String> = _halqaNameFlow.asStateFlow()

    private val _inviteCodeFlow = MutableStateFlow<String>("")
    val inviteCodeFlow: StateFlow<String> = _inviteCodeFlow.asStateFlow()

    private val _isCurrentUserAdminFlow = MutableStateFlow<Boolean>(false)
    val isCurrentUserAdminFlow: StateFlow<Boolean> = _isCurrentUserAdminFlow.asStateFlow()

    private val _loopMembersFlow = MutableStateFlow<List<LoopMemberItem>>(emptyList())
    val loopMembersFlow: StateFlow<List<LoopMemberItem>> = _loopMembersFlow.asStateFlow()

    private val _todaySummaryTextFlow = MutableStateFlow<String>(initialTodaySummary)
    val todaySummaryTextFlow: StateFlow<String> = _todaySummaryTextFlow.asStateFlow()

    private val _awakeCountTextFlow = MutableStateFlow<String>(initialAwakeCount)
    val awakeCountTextFlow: StateFlow<String> = _awakeCountTextFlow.asStateFlow()

    private val _friendWakeAlertFlow = MutableStateFlow<FriendWakeAlert?>(null)
    val friendWakeAlertFlow: StateFlow<FriendWakeAlert?> = _friendWakeAlertFlow.asStateFlow()

    // Countdown and Prayer times
    private val _fajrTimeStrFlow = MutableStateFlow<String>("")
    val fajrTimeStrFlow: StateFlow<String> = _fajrTimeStrFlow.asStateFlow()

    private val _sunriseTimeStrFlow = MutableStateFlow<String>("")
    val sunriseTimeStrFlow: StateFlow<String> = _sunriseTimeStrFlow.asStateFlow()

    private val _countdownTextFlow = MutableStateFlow<String>("")
    val countdownTextFlow: StateFlow<String> = _countdownTextFlow.asStateFlow()

    private val _countdownColorFlow = MutableStateFlow<String>("#2ECC71")
    val countdownColorFlow: StateFlow<String> = _countdownColorFlow.asStateFlow()

    private val _countdownCardBorderModeFlow = MutableStateFlow<Int>(0)
    val countdownCardBorderModeFlow: StateFlow<Int> = _countdownCardBorderModeFlow.asStateFlow()


    private var userProfileListener: ValueEventListener? = null
    private var halqaListener: ValueEventListener? = null
    private var dailyRecordsListener: ValueEventListener? = null
    private var lastScheduledTestAlarmTime: Long = 0L

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null

    init {
        refreshUserData()
        startFajrCountdown()
    }

    fun refreshUserData() {
        stopAllObservers()
        val uid = userRepository.getUserId()
        if (uid != null) {
            userProfileListener = userRepository.observeUserProfile(uid) { profile ->
                _userProfileFlow.value = profile
                val hId = if (profile != null && profile.currentHalqaId.isNotEmpty()) profile.currentHalqaId else null
                _halqaIdFlow.value = hId

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

                startFajrCountdown()
            }

            halqaListener = halqaRepository.observeUserHalqa { snapshot ->
                val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                if (snapshot == null || !snapshot.exists()) {
                    _halqaNameFlow.value = ""
                    _isCurrentUserAdminFlow.value = false
                    _loopMembersFlow.value = emptyList()
                    _todaySummaryTextFlow.value = ""
                    _awakeCountTextFlow.value = ""
                    _friendWakeAlertFlow.value = null
                    stopObservingDailyRecords()

                    prefs.edit()
                        .remove("current_halqa_id")
                        .remove("current_halqa_name")
                        .remove("cached_awake_count_text")
                        .remove("cached_today_summary_text")
                        .apply()
                } else {
                    val name = snapshot.child("name").value as? String ?: "حلقة"
                    val inviteCode = snapshot.child("inviteCode").value as? String ?: ""
                    _halqaNameFlow.value = name
                    _inviteCodeFlow.value = inviteCode

                    val chain = (snapshot.child("chain").value as? List<*>)
                        ?.filterIsInstance<String>() ?: emptyList()

                    val membersSnap = snapshot.child("members")
                    val currentUid = userRepository.getUserId()
                    if (currentUid != null && !membersSnap.hasChild(currentUid)) {
                        leaveHalqa { _, _ -> }
                        android.widget.Toast.makeText(getApplication(), "ℹ️ تم إزالتك من الحلقة بواسطة مسؤول الحلقة", android.widget.Toast.LENGTH_LONG).show()
                        return@observeUserHalqa
                    }

                    val isAdmin = membersSnap.child(currentUid ?: "").child("role").value as? String == "admin"
                    _isCurrentUserAdminFlow.value = isAdmin

                    val halqaId = snapshot.key!!
                    prefs.edit()
                        .putString("current_halqa_id", halqaId)
                        .putString("current_halqa_name", name)
                        .apply()

                    val now = System.currentTimeMillis()
                    var earliestFajr: Long? = null
                    for (mChild in membersSnap.children) {
                        val mFajr = mChild.child("fajrTimeMillis").value as? Long
                        if (mFajr != null && mFajr > now) {
                            if (earliestFajr == null || mFajr < earliestFajr) {
                                earliestFajr = mFajr
                            }
                        }
                    }

                    if (earliestFajr != null) {
                        prefs.edit().putLong("halqa_earliest_fajr_millis", earliestFajr).apply()
                    } else {
                        prefs.edit().remove("halqa_earliest_fajr_millis").apply()
                    }

                    com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler.scheduleNextFajrAlarm(getApplication())

                    val testAlarmTime = snapshot.child("testAlarmTime").value as? Long
                    if (testAlarmTime != null && testAlarmTime > System.currentTimeMillis() && testAlarmTime != lastScheduledTestAlarmTime) {
                        lastScheduledTestAlarmTime = testAlarmTime
                        com.bagomri.fajrloop.alarm.AlarmScheduler.scheduleAlarm(getApplication(), testAlarmTime, "اختبار منبه الحلقة 🧪")
                        android.widget.Toast.makeText(getApplication(), "🧪 تمت جدولة اختبار منبه الحلقة ليرن بعد دقيقة!", android.widget.Toast.LENGTH_LONG).show()
                    }

                    startObservingDailyRecords(halqaId, chain, membersSnap)
                }
            }
        }
    }

    fun clearUserDataOnLogout() {
        stopAllObservers()
        _userProfileFlow.value = null
        _halqaIdFlow.value = null
        _halqaNameFlow.value = ""
        _isCurrentUserAdminFlow.value = false
        _loopMembersFlow.value = emptyList()
        _todaySummaryTextFlow.value = ""
        _awakeCountTextFlow.value = ""
        _friendWakeAlertFlow.value = null

        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun stopAllObservers() {
        userProfileListener?.let {
            val uid = userRepository.getUserId()
            if (uid != null) {
                userRepository.removeUserProfileObserver(uid, it)
            }
        }
        userProfileListener = null
        halqaListener?.let {
            halqaRepository.removeObserver(it)
        }
        halqaListener = null
        stopObservingDailyRecords()
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
            val currentId = _userProfileFlow.value?.currentHalqaId
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

        val effectiveChain = chain.toMutableList()
        for (mChild in membersSnap.children) {
            val mId = mChild.key ?: continue
            if (!effectiveChain.contains(mId)) {
                effectiveChain.add(mId)
            }
        }

        for ((idx, mId) in effectiveChain.withIndex()) {
            val mSnap = membersSnap.child(mId)
            if (!mSnap.exists()) continue

            val displayName = mSnap.child("displayName").value as? String ?: "عضو"
            val photoUrl = mSnap.child("photoUrl").value as? String ?: ""
            val role = mSnap.child("role").value as? String ?: "member"
            val responsibleForUserId = mSnap.child("responsibleForUserId").value as? String ?: ""

            val targetSnap = membersSnap.child(responsibleForUserId)
            val targetName = targetSnap.child("displayName").value as? String ?: ""

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
                    isCurrentUser = mId == currentUid,
                    role = role,
                    responsibleForUserId = responsibleForUserId,
                    targetName = targetName,
                    position = idx + 1
                )
            )
        }

        _loopMembersFlow.value = membersList
        _friendWakeAlertFlow.value = alertFriend

        val total = effectiveChain.size
        val countText = "$awakeCount / $total"
        _awakeCountTextFlow.value = countText
        val summaryText = if (awakeCount == total && total > 0) {
            "ما شاء الله! استيقظت الحلقة بالكامل 🎉"
        } else {
            "استيقظ $awakeCount من أصل $total أعضاء حتى الآن."
        }
        _todaySummaryTextFlow.value = summaryText

        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("cached_awake_count_text", countText)
            .putString("cached_today_summary_text", summaryText)
            .apply()
    }

    fun confirmFriendWake(friendUid: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = userRepository.getUserId() ?: return
        val halqaId = _userProfileFlow.value?.currentHalqaId ?: return
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
        val fajrStr = timeFormat.format(Date(prayerTimes.fajr))
        val sunriseStr = timeFormat.format(Date(prayerTimes.sunrise))
        _fajrTimeStrFlow.value = fajrStr
        _sunriseTimeStrFlow.value = sunriseStr

        com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler.scheduleNextFajrAlarm(getApplication())
        com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler.startPeriodicRescheduler(getApplication())

        countdownRunnable = object : Runnable {
            override fun run() {
                val remaining = prayerTimes.fajr - System.currentTimeMillis()
                if (remaining > 0) {
                    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(remaining)
                    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
                    val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
                    val cText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    _countdownTextFlow.value = cText

                    val totalMinutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(remaining)
                    val textColor = when {
                        totalMinutes >= 60 -> "#2ECC71"
                        totalMinutes in 15..59 -> "#FFD700"
                        else -> "#E74C3C"
                    }
                    _countdownColorFlow.value = textColor

                    val borderMode = when {
                        totalMinutes < 5 -> 3
                        totalMinutes < 15 -> 2
                        totalMinutes < 60 -> 1
                        else -> 0
                    }
                    _countdownCardBorderModeFlow.value = borderMode
                } else {
                    _countdownTextFlow.value = "00:00:00"
                    _countdownColorFlow.value = "#E74C3C"
                    _countdownCardBorderModeFlow.value = 2
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(countdownRunnable!!)
    }

    fun reorderMember(fromIndex: Int, toIndex: Int, onResult: (Boolean, String?) -> Unit) {
        val halqaId = _halqaIdFlow.value ?: return
        val currentMembers = _loopMembersFlow.value
        if (fromIndex !in currentMembers.indices || toIndex !in currentMembers.indices) return

        val newChain = currentMembers.map { it.userId }.toMutableList()
        val movedId = newChain.removeAt(fromIndex)
        newChain.add(toIndex, movedId)

        halqaRepository.reorderChain(halqaId, newChain, onResult)
    }

    fun removeMemberFromHalqa(targetUid: String, onResult: (Boolean, String?) -> Unit) {
        val halqaId = _halqaIdFlow.value ?: return
        HalqaManager.removeMemberFromHalqa(halqaId, targetUid, onResult)
    }

    fun leaveHalqa(onResult: (Boolean, String?) -> Unit) {
        halqaRepository.leaveHalqa { success, error ->
            if (success) {
                _halqaIdFlow.value = null
                _halqaNameFlow.value = ""
                _inviteCodeFlow.value = ""
                _loopMembersFlow.value = emptyList()
                _isCurrentUserAdminFlow.value = false
                _friendWakeAlertFlow.value = null
                _awakeCountTextFlow.value = ""
                _todaySummaryTextFlow.value = ""

                val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .remove("current_halqa_id")
                    .remove("current_halqa_name")
                    .remove("cached_awake_count_text")
                    .remove("cached_today_summary_text")
                    .apply()
            }
            onResult(success, error)
        }
    }

    fun createHalqa(name: String, onResult: (Boolean, String?) -> Unit) {
        halqaRepository.createHalqa(name) { success, result ->
            if (success) {
                refreshUserData()
            }
            onResult(success, result)
        }
    }

    fun joinHalqa(inviteCode: String, onResult: (Boolean, String?) -> Unit) {
        halqaRepository.joinHalqa(inviteCode) { success, result ->
            if (success) {
                refreshUserData()
            }
            onResult(success, result)
        }
    }

    fun triggerTestLoopAlarm(onResult: (Boolean, String?) -> Unit) {
        val halqaId = _halqaIdFlow.value
        if (halqaId.isNullOrEmpty()) {
            onResult(false, "يجب الانضمام لحلقة أولاً لإجراء الاختبار")
            return
        }
        halqaRepository.triggerTestLoopAlarm(halqaId, onResult)
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

data class LoopMemberItem(
    val userId: String,
    val displayName: String,
    val photoUrl: String,
    val status: String,
    val isCurrentUser: Boolean,
    val role: String = "member",
    val responsibleForUserId: String = "",
    val targetName: String = "",
    val position: Int = 1
)

data class FriendWakeAlert(
    val uid: String,
    val displayName: String,
    val message: String
)
