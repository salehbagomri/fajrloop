package com.bagomri.fajrloop.ui.main

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.alarm.AlarmScheduler
import com.bagomri.fajrloop.alarm.FajrAlarmAutoScheduler
import com.bagomri.fajrloop.data.HalqaManager
import com.bagomri.fajrloop.data.HalqaRepository
import com.bagomri.fajrloop.data.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class HalqaViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val halqaRepository = HalqaRepository()

    private val initialHalqaId: String? = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
    }

    private val initialHalqaName: String = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_NAME, "") ?: ""
    }

    private val initialTodaySummary: String = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(AlarmPreferences.KEY_CACHED_TODAY_SUMMARY_TEXT, "") ?: ""
    }

    private val initialAwakeCount: String = run {
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(AlarmPreferences.KEY_CACHED_AWAKE_COUNT_TEXT, "") ?: ""
    }

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

    private val _isHalqaEffectiveFlow = MutableStateFlow<Boolean>(false)
    val isHalqaEffectiveFlow: StateFlow<Boolean> = _isHalqaEffectiveFlow.asStateFlow()

    private val _todaySummaryTextFlow = MutableStateFlow<String>(initialTodaySummary)
    val todaySummaryTextFlow: StateFlow<String> = _todaySummaryTextFlow.asStateFlow()

    private val _awakeCountTextFlow = MutableStateFlow<String>(initialAwakeCount)
    val awakeCountTextFlow: StateFlow<String> = _awakeCountTextFlow.asStateFlow()

    private val _friendWakeAlertFlow = MutableStateFlow<FriendWakeAlert?>(null)
    val friendWakeAlertFlow: StateFlow<FriendWakeAlert?> = _friendWakeAlertFlow.asStateFlow()

    private var halqaListener: ValueEventListener? = null
    private var dailyRecordsListener: ValueEventListener? = null
    private var lastScheduledTestAlarmTime: Long = 0L

    init {
        startObservingHalqa()
    }

    fun startObservingHalqa() {
        stopObservingHalqa()
        val uid = userRepository.getUserId() ?: return

        halqaListener = halqaRepository.observeUserHalqa { snapshot ->
            val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            if (snapshot == null || !snapshot.exists()) {
                clearHalqaState(prefs)
            } else {
                val name = snapshot.child("name").value as? String ?: "حلقة"
                val inviteCode = snapshot.child("inviteCode").value as? String ?: ""
                _halqaNameFlow.value = name
                _inviteCodeFlow.value = inviteCode

                val chain = (snapshot.child("chain").value as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()

                val membersSnap = snapshot.child("members")
                val isAdmin = membersSnap.child(uid).child("role").value as? String == "admin"
                _isCurrentUserAdminFlow.value = isAdmin

                val halqaId = snapshot.key!!
                _halqaIdFlow.value = halqaId
                prefs.edit()
                    .putString(AlarmPreferences.KEY_CURRENT_HALQA_ID, halqaId)
                    .putString(AlarmPreferences.KEY_CURRENT_HALQA_NAME, name)
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

                val prevEarliest = prefs.getLong(AlarmPreferences.KEY_HALQA_EARLIEST_FAJR_MILLIS, -1L)
                val newEarliest = earliestFajr ?: -1L

                if (prevEarliest != newEarliest) {
                    if (earliestFajr != null) {
                        prefs.edit().putLong(AlarmPreferences.KEY_HALQA_EARLIEST_FAJR_MILLIS, earliestFajr).apply()
                    } else {
                        prefs.edit().remove(AlarmPreferences.KEY_HALQA_EARLIEST_FAJR_MILLIS).apply()
                    }
                    FajrAlarmAutoScheduler.scheduleNextFajrAlarm(getApplication())
                }

                val testAlarmTime = snapshot.child("testAlarmTime").value as? Long
                if (testAlarmTime != null && testAlarmTime > System.currentTimeMillis() && testAlarmTime != lastScheduledTestAlarmTime) {
                    lastScheduledTestAlarmTime = testAlarmTime
                    AlarmScheduler.scheduleAlarm(getApplication(), testAlarmTime, "اختبار منبه الحلقة 🧪")
                    Toast.makeText(getApplication(), "🧪 تمت جدولة اختبار منبه الحلقة ليرن بعد دقيقة!", Toast.LENGTH_LONG).show()
                }

                startObservingDailyRecords(halqaId, chain, membersSnap)
            }
        }
    }

    private fun clearHalqaState(prefs: android.content.SharedPreferences) {
        _halqaIdFlow.value = null
        _halqaNameFlow.value = ""
        _inviteCodeFlow.value = ""
        _isCurrentUserAdminFlow.value = false
        _loopMembersFlow.value = emptyList()
        _isHalqaEffectiveFlow.value = false
        _todaySummaryTextFlow.value = ""
        _awakeCountTextFlow.value = ""
        _friendWakeAlertFlow.value = null
        stopObservingDailyRecords()

        prefs.edit()
            .remove(AlarmPreferences.KEY_CURRENT_HALQA_ID)
            .remove(AlarmPreferences.KEY_CURRENT_HALQA_NAME)
            .remove(AlarmPreferences.KEY_CACHED_AWAKE_COUNT_TEXT)
            .remove(AlarmPreferences.KEY_CACHED_TODAY_SUMMARY_TEXT)
            .apply()
    }

    fun stopObservingHalqa() {
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
            val currentId = _halqaIdFlow.value
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
        _isHalqaEffectiveFlow.value = effectiveChain.size >= 2
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
            .putString(AlarmPreferences.KEY_CACHED_AWAKE_COUNT_TEXT, countText)
            .putString(AlarmPreferences.KEY_CACHED_TODAY_SUMMARY_TEXT, summaryText)
            .apply()
    }

    fun confirmFriendWake(friendUid: String, onResult: (Boolean, String?) -> Unit) {
        val currentUid = userRepository.getUserId() ?: return
        val halqaId = _halqaIdFlow.value ?: return
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
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.localizedMessage) }
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
                val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                clearHalqaState(prefs)
            }
            onResult(success, error)
        }
    }

    fun createHalqa(name: String, onResult: (Boolean, String?) -> Unit) {
        halqaRepository.createHalqa(name) { success, result ->
            if (success) {
                startObservingHalqa()
            }
            onResult(success, result)
        }
    }

    fun joinHalqa(inviteCode: String, onResult: (Boolean, String?) -> Unit) {
        halqaRepository.joinHalqa(inviteCode) { success, result ->
            if (success) {
                startObservingHalqa()
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

    fun clearHalqaData() {
        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        clearHalqaState(prefs)
        stopObservingHalqa()
    }

    override fun onCleared() {
        super.onCleared()
        stopObservingHalqa()
    }
}
