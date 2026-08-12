package com.bagomri.fajrloop.ui.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class AlarmRingingViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()

    private val _errorFlow = MutableSharedFlow<String>(replay = 0)
    val errorFlow: SharedFlow<String> = _errorFlow.asSharedFlow()

    fun emitError(message: String) {
        viewModelScope.launch {
            _errorFlow.emit(message)
        }
    }

    private val _isChallengeSolvedFlow = MutableStateFlow(false)
    val isChallengeSolvedFlow: StateFlow<Boolean> = _isChallengeSolvedFlow.asStateFlow()

    private val _isAlarmDismissedFlow = MutableStateFlow(false)
    val isAlarmDismissedFlow: StateFlow<Boolean> = _isAlarmDismissedFlow.asStateFlow()

    private val _isPanicActiveFlow = MutableStateFlow(false)
    val isPanicActiveFlow: StateFlow<Boolean> = _isPanicActiveFlow.asStateFlow()

    private val _supervisorNameFlow = MutableStateFlow("المسؤول")
    val supervisorNameFlow: StateFlow<String> = _supervisorNameFlow.asStateFlow()

    private val _supervisorPhoneFlow = MutableStateFlow("")
    val supervisorPhoneFlow: StateFlow<String> = _supervisorPhoneFlow.asStateFlow()

    private val _dismissFinishedFlow = MutableStateFlow(false)
    val dismissFinishedFlow: StateFlow<Boolean> = _dismissFinishedFlow.asStateFlow()

    private val _snoozeCountLeftFlow = MutableStateFlow(2)
    val snoozeCountLeftFlow: StateFlow<Int> = _snoozeCountLeftFlow.asStateFlow()

    private var dailyRecordListener: ValueEventListener? = null
    private var supervisorRecordListener: ValueEventListener? = null
    private var supervisorUid: String? = null

    private var scrambledWords = listOf(
        Pair("ج ر ف", "فجر"),
        Pair("ة ا ل ص", "صلاة"),
        Pair("د ج س م", "مسجد"),
        Pair("ن ا م ي إ", "إيمان"),
        Pair("ن آ ر ق", "قرآن"),
        Pair("ة ك م", "مكة"),
        Pair("ة ن ي د م", "مدينة")
    )

    fun onChallengePassed() {
        _isChallengeSolvedFlow.value = true
        updateDailyStatus("challenge_done")

        val sUid = supervisorUid
        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
        if (!sUid.isNullOrEmpty() && !halqaId.isNullOrEmpty()) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            FirebaseDatabase.getInstance()
                .getReference("dailyRecords")
                .child(halqaId)
                .child(currentDate)
                .child(sUid)
                .child("status")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val status = snapshot.value as? String
                        if (status == "challenge_done" || status == "awake") {
                            checkAndAutoConfirm(halqaId, sUid)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun startObservingSupervisorStatus(halqaId: String, sUid: String) {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val supervisorRecordRef = FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .child(sUid)

        supervisorRecordListener = supervisorRecordRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val status = snapshot.child("status").value as? String
                    if (status == "challenge_done" || status == "awake") {
                        checkAndAutoConfirm(halqaId, sUid)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkAndAutoConfirm(halqaId: String, sUid: String) {
        val isSolved = _isChallengeSolvedFlow.value
        if (isSolved && !_isAlarmDismissedFlow.value) {
            val uid = userRepository.getUserId() ?: return
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val recordRef = FirebaseDatabase.getInstance()
                .getReference("dailyRecords")
                .child(halqaId)
                .child(currentDate)
                .child(uid)

            val isoDate = getIso8601String(Date())
            val recordMap = mapOf(
                "status" to "awake",
                "updatedAt" to isoDate,
                "alarmTime" to isoDate,
                "challengeDoneAt" to isoDate,
                "confirmedBy" to sUid
            )
            recordRef.setValue(recordMap).addOnSuccessListener {
                dismissAlarm("awake")
            }
        }
    }

    fun triggerEmergencySos() {
        _isPanicActiveFlow.value = true
        updateDailyStatus("panic")
    }

    fun dismissAlarm(status: String) {
        _isAlarmDismissedFlow.value = true
        updateDailyStatus(status)
        _dismissFinishedFlow.value = true
    }

    fun triggerSnooze(halqaId: String) {
        val currentLeft = _snoozeCountLeftFlow.value
        if (currentLeft <= 0) return

        val newLeft = currentLeft - 1
        _snoozeCountLeftFlow.value = newLeft

        val uid = userRepository.getUserId() ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val recordRef = FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .child(uid)

        val countRef = recordRef.child("snoozeCount")
        val currentSnoozes = 2 - currentLeft
        countRef.setValue(currentSnoozes + 1)

        val snoozeTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000L
        com.bagomri.fajrloop.alarm.AlarmScheduler.scheduleAlarm(getApplication(), snoozeTimeMillis, "غفوة صلاة الفجر")

        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().putLong(AlarmPreferences.KEY_ALARM_TIME_MILLIS, snoozeTimeMillis).apply()

        _isAlarmDismissedFlow.value = true
        _dismissFinishedFlow.value = true
    }

    fun generateMathQuestion(difficulty: String): Pair<String, Int> {
        val random = Random()
        val a: Int
        val b: Int
        val op: String
        val answer: Int
        when (difficulty) {
            "easy" -> {
                a = random.nextInt(15) + 1
                b = random.nextInt(15) + 1
                op = "+"
                answer = a + b
            }
            "hard" -> {
                a = random.nextInt(8) + 2
                b = random.nextInt(9) + 11
                op = "*"
                answer = a * b
            }
            else -> { // medium
                a = random.nextInt(50) + 10
                b = random.nextInt(40) + 10
                op = if (random.nextBoolean()) "+" else "-"
                answer = if (op == "+") a + b else a - b
            }
        }
        return Pair("$a $op $b = ?", answer)
    }

    fun generateWordPuzzle(): Pair<String, String> {
        val random = Random()
        return scrambledWords[random.nextInt(scrambledWords.size)]
    }

    fun verifyTotpCode(userInput: String, halqaId: String): Boolean {
        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val secret = prefs.getString("halqa_shared_secret_$halqaId", "") ?: ""
        return com.bagomri.fajrloop.alarm.EmergencyCodeUtils.verifyTotpCode(userInput, halqaId, secret)
    }

    fun startObservingDailyRecord(halqaId: String) {
        val uid = userRepository.getUserId() ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val dailyRecordRef = FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .child(uid)

        dailyRecordListener = dailyRecordRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val status = snapshot.child("status").value as? String
                    if (status == "awake") {
                        dismissAlarm("awake")
                    }
                    val count = snapshot.child("snoozeCount").value as? Long
                    if (count != null) {
                        val left = (2 - count.toInt()).coerceAtLeast(0)
                        _snoozeCountLeftFlow.value = left
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                emitError(error.message)
            }
        })
    }

    fun findActiveSupervisor(chain: List<String>, members: Map<String, Any?>, startUid: String): String {
        val n = chain.size
        val startIndex = chain.indexOf(startUid)
        if (startIndex == -1 || n <= 1) return startUid

        // ابدأ من العضو المسؤول (التالي في السلسلة)
        val responsibleIndex = (startIndex + 1) % n

        for (step in 0 until n) {
            val candidateIndex = (responsibleIndex + step) % n
            val candidateUid = chain[candidateIndex]
            if (candidateUid == startUid) continue // تجنب المستخدم نفسه

            val candidateData = members[candidateUid] as? Map<*, *>
            val candidateStatus = candidateData?.get("status") as? String

            if (candidateStatus != "travel") {
                return candidateUid  // أول عضو غير مسافر
            }
        }

        // إذا كل الأعضاء مسافرون، ارجع للمسؤول الأصلي
        return chain[(startIndex + 1) % n]
    }

    fun loadPartnerDetails(halqaId: String) {
        val uid = userRepository.getUserId() ?: return
        FirebaseDatabase.getInstance().getReference("halqas").child(halqaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val chain = (snapshot.child("chain").value as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        val membersSnap = snapshot.child("members")

                        val membersMap = mutableMapOf<String, Any?>()
                        for (member in membersSnap.children) {
                            val key = member.key ?: continue
                            membersMap[key] = member.value as? Map<*, *>
                        }

                        var supervisorUid: String? = null

                        if (chain.isNotEmpty()) {
                            val activeSuper = findActiveSupervisor(chain, membersMap, uid)
                            if (activeSuper != uid) {
                                supervisorUid = activeSuper
                            }
                        }

                        if (supervisorUid == null) {
                            for (member in membersSnap.children) {
                                val key = member.key ?: continue
                                if (key != uid) {
                                    val status = member.child("status").value as? String
                                    if (status != "travel") {
                                        supervisorUid = key
                                        break
                                    }
                                }
                            }
                        }

                        if (supervisorUid == null) {
                            for (member in membersSnap.children) {
                                if (member.key != uid) {
                                    supervisorUid = member.key
                                    break
                                }
                            }
                        }

                        if (supervisorUid != null) {
                            val name = membersSnap.child(supervisorUid).child("displayName").value as? String ?: "المسؤول"
                            _supervisorNameFlow.value = name
                            this@AlarmRingingViewModel.supervisorUid = supervisorUid
                            startObservingSupervisorStatus(halqaId, supervisorUid)

                            FirebaseDatabase.getInstance().getReference("users").child(supervisorUid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnap: DataSnapshot) {
                                        val phone = userSnap.child("phone").value as? String 
                                            ?: userSnap.child("phoneNumber").value as? String 
                                            ?: ""
                                        _supervisorPhoneFlow.value = phone
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        emitError(error.message)
                                    }
                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    emitError(error.message)
                }
            })
    }

    fun updateDailyStatus(status: String) {
        val uid = userRepository.getUserId()
        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)

        if (!uid.isNullOrEmpty() && !halqaId.isNullOrEmpty()) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val recordRef = FirebaseDatabase.getInstance()
                .getReference("dailyRecords")
                .child(halqaId)
                .child(currentDate)
                .child(uid)

            val isoDate = getIso8601String(Date())
            val recordMap = mapOf(
                "status" to status,
                "updatedAt" to isoDate,
                "alarmTime" to getIso8601String(Date()),
                "challengeDoneAt" to isoDate
            )
            recordRef.setValue(recordMap)
        }
    }

    private fun getIso8601String(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    override fun onCleared() {
        super.onCleared()
        dailyRecordListener?.let {
            val uid = userRepository.getUserId()
            val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
            val halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
            if (!uid.isNullOrEmpty() && !halqaId.isNullOrEmpty()) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                FirebaseDatabase.getInstance()
                    .getReference("dailyRecords")
                    .child(halqaId)
                    .child(currentDate)
                    .child(uid)
                    .removeEventListener(it)
            }
        }
        supervisorRecordListener?.let {
            val sUid = supervisorUid
            val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
            val halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
            if (!sUid.isNullOrEmpty() && !halqaId.isNullOrEmpty()) {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                FirebaseDatabase.getInstance()
                    .getReference("dailyRecords")
                    .child(halqaId)
                    .child(currentDate)
                    .child(sUid)
                    .removeEventListener(it)
            }
        }
    }
}
