package com.bagomri.fajrloop.ui.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.data.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class AlarmRingingViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    private val alarmRepository = AlarmRepository(application)

    private val _isChallengeSolved = MutableLiveData(false)
    val isChallengeSolved: LiveData<Boolean> = _isChallengeSolved

    private val _isAlarmDismissed = MutableLiveData(false)
    val isAlarmDismissed: LiveData<Boolean> = _isAlarmDismissed

    private val _isPanicActive = MutableLiveData(false)
    val isPanicActive: LiveData<Boolean> = _isPanicActive

    private val _supervisorName = MutableLiveData("المسؤول")
    val supervisorName: LiveData<String> = _supervisorName

    private val _supervisorPhone = MutableLiveData("")
    val supervisorPhone: LiveData<String> = _supervisorPhone

    private val _dismissFinished = MutableLiveData(false)
    val dismissFinished: LiveData<Boolean> = _dismissFinished

    private val _snoozeCountLeft = MutableLiveData(2)
    val snoozeCountLeft: LiveData<Int> = _snoozeCountLeft

    private var dailyRecordListener: ValueEventListener? = null
    private var supervisorRecordListener: ValueEventListener? = null
    private var supervisorUid: String? = null

    // تحديات
    private var scrambledWords = listOf(
        Pair("ج ر ف", "فجر"),
        Pair("ة ا ل ص", "صلاة"),
        Pair("د ج س م", "مسجد"),
        Pair("n ا م ي إ", "إيمان"),
        Pair("n آ ر ق", "قرآن"),
        Pair("ة ك م", "مكة"),
        Pair("ة ن ي د م", "مدينة")
    )

    fun onChallengePassed() {
        _isChallengeSolved.value = true
        updateDailyStatus("challenge_done")

        // تحقق مما إذا كان المشرف (المسؤول) مستيقظاً بالفعل لحسم التأكيد التلقائي
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
        val isSolved = _isChallengeSolved.value == true
        if (isSolved && _isAlarmDismissed.value != true) {
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
        _isPanicActive.value = true
        updateDailyStatus("panic")
    }

    fun dismissAlarm(status: String) {
        _isAlarmDismissed.value = true
        updateDailyStatus(status)
        _dismissFinished.value = true
    }

    fun triggerSnooze(halqaId: String) {
        val currentLeft = _snoozeCountLeft.value ?: 2
        if (currentLeft <= 0) return

        val newLeft = currentLeft - 1
        _snoozeCountLeft.value = newLeft

        val uid = userRepository.getUserId() ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val recordRef = FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .child(uid)

        // Increment snoozeCount in Firebase
        val countRef = recordRef.child("snoozeCount")
        val currentSnoozes = 2 - currentLeft
        countRef.setValue(currentSnoozes + 1)

        // Schedule snooze alarm 5 minutes later
        val snoozeTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000L
        com.bagomri.fajrloop.alarm.AlarmScheduler.scheduleAlarm(getApplication(), snoozeTimeMillis, "غفوة صلاة الفجر")

        // Save new snooze alarm time in preferences
        val prefs = getApplication<Application>().getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        prefs.edit().putLong(AlarmPreferences.KEY_ALARM_TIME_MILLIS, snoozeTimeMillis).apply()

        // Terminate active ringing activity
        _isAlarmDismissed.value = true
        _dismissFinished.value = true
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
                        _snoozeCountLeft.value = (2 - count.toInt()).coerceAtLeast(0)
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
                                _supervisorName.value = member.child("displayName").value as? String ?: "المسؤول"
                                break
                            }
                        }
                        
                        if (supervisorUid == null) {
                            for (member in membersSnap.children) {
                                if (member.key != uid) {
                                    supervisorUid = member.key
                                    _supervisorName.value = member.child("displayName").value as? String ?: "المسؤول"
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
                                        _supervisorPhone.value = phone
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
                "alarmTime" to getIso8601String(Date()), // can be overridden by triggerTime if passed
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
