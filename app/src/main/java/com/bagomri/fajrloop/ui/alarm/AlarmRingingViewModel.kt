package com.bagomri.fajrloop.ui.alarm

import android.app.Application
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
import kotlin.math.absoluteValue

class AlarmRingingViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()

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
        val halqaId = prefs.getString("current_halqa_id", null)
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
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val seed = (dateStr + halqaId).hashCode().absoluteValue
        val expectedCode = (seed % 900000) + 100000

        val userCodeVal = userInput.replace(" ", "").trim().toIntOrNull()
        return userCodeVal == expectedCode
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

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun loadPartnerDetails(halqaId: String) {
        val uid = userRepository.getUserId() ?: return
        FirebaseDatabase.getInstance().getReference("halqas").child(halqaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val membersSnap = snapshot.child("members")
                        var supervisorUid: String? = null
                        
                        for (member in membersSnap.children) {
                            val respFor = member.child("responsibleForUserId").value as? String
                            if (respFor == uid && member.key != uid) {
                                supervisorUid = member.key
                                val name = member.child("displayName").value as? String ?: "المسؤول"
                                _supervisorNameFlow.value = name
                                break
                            }
                        }
                        
                        if (supervisorUid == null) {
                            for (member in membersSnap.children) {
                                if (member.key != uid) {
                                    supervisorUid = member.key
                                    val name = member.child("displayName").value as? String ?: "المسؤول"
                                    _supervisorNameFlow.value = name
                                    break
                                }
                            }
                        }
                        
                        if (supervisorUid != null) {
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
                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateDailyStatus(status: String) {
        val uid = userRepository.getUserId()
        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val halqaId = prefs.getString("current_halqa_id", null)

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
            val halqaId = prefs.getString("current_halqa_id", null)
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
            val halqaId = prefs.getString("current_halqa_id", null)
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
