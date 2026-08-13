package com.bagomri.fajrloop.ui.stats

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

data class LeaderboardItem(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val photoUrl: String,
    val streak: Int,
    val rescues: Int
)

data class Achievement(
    val id: String,
    val title: String,
    val desc: String,
    val emoji: String,
    val colorCode: String,
    val acquiredDate: String?
)

data class WeeklyBarData(
    val dayName: String,
    val heightPercent: Float,
    val status: String
)

data class StatsUiState(
    val isLoading: Boolean = true,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalFajr: Int = 0,
    val totalRescues: Int = 0,
    val fastestMember: String = "لا حلقة",
    val topRescuer: String = "لا حلقة",
    val weeklyChart: List<WeeklyBarData> = emptyList(),
    val dayStatusMap: Map<Int, String> = emptyMap(),
    val currentDayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val leaderboard: List<LeaderboardItem> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    private var halqaId: String? = null
    private var currentUid = ""

    init {
        refreshStats()
    }

    fun refreshStats() {
        val application = getApplication<Application>()
        val prefs = application.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
        currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (currentUid.isEmpty()) {
            setupPlaceholderStats()
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        val rootRef = FirebaseDatabase.getInstance().reference

        if (!halqaId.isNullOrEmpty()) {
            rootRef.child("dailyRecords").child(halqaId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(halqaRecordsSnap: DataSnapshot) {
                    rootRef.child("halqas").child(halqaId!!).child("members")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(membersSnap: DataSnapshot) {
                                processStats(halqaRecordsSnap, membersSnap)
                            }
                            override fun onCancelled(error: DatabaseError) {
                                processStats(halqaRecordsSnap, null)
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("StatsViewModel", "Failed to query halqa daily records: ${error.message}")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            })
        } else {
            setupPlaceholderStats()
        }
    }

    private fun processStats(halqaRecordsSnap: DataSnapshot, membersSnap: DataSnapshot?) {
        val userCheckinDates = mutableSetOf<String>()
        var rescuesCount = 0

        for (dateSnap in halqaRecordsSnap.children) {
            val dateStr = dateSnap.key ?: continue

            val userRecord = dateSnap.child(currentUid)
            if (userRecord.exists()) {
                val status = userRecord.child("status").value as? String
                if (status == "awake" || status == "challenge_done" || status == "travel") {
                    userCheckinDates.add(dateStr)
                }
            }

            for (memberRecord in dateSnap.children) {
                val confirmedBy = memberRecord.child("confirmedBy").value as? String
                if (confirmedBy == currentUid) {
                    rescuesCount++
                }
            }
        }

        val currentStreak = calculateCurrentStreak(userCheckinDates)
        val longestStreak = calculateLongestStreak(userCheckinDates)

        val weeklyChart = buildWeeklyChart(halqaRecordsSnap)
        val dayStatusMap = buildMonthlyCalendar(halqaRecordsSnap)
        val (leaderboardList, fastestMember, topRescuer) = buildLeaderboard(halqaRecordsSnap, membersSnap)
        val achievementsList = buildAchievements(currentStreak, longestStreak, rescuesCount, userCheckinDates.size)

        _uiState.value = StatsUiState(
            isLoading = false,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalFajr = userCheckinDates.size,
            totalRescues = rescuesCount,
            fastestMember = fastestMember,
            topRescuer = topRescuer,
            weeklyChart = weeklyChart,
            dayStatusMap = dayStatusMap,
            leaderboard = leaderboardList,
            achievements = achievementsList
        )
    }

    private fun calculateCurrentStreak(dates: Set<String>): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        var streak = 0
        while (true) {
            val dateStr = sdf.format(cal.time)
            if (dates.contains(dateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                if (streak == 0) {
                    cal.add(Calendar.DAY_OF_MONTH, -1)
                    val yesterdayStr = sdf.format(cal.time)
                    if (dates.contains(yesterdayStr)) {
                        streak++
                        cal.add(Calendar.DAY_OF_MONTH, -1)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sortedDates = dates.mapNotNull {
            runCatching { sdf.parse(it) }.getOrNull()
        }.sorted()

        if (sortedDates.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val diff = (sortedDates[i].time - sortedDates[i - 1].time) / (1000 * 60 * 60 * 24)
            if (diff == 1L) {
                currentStreak++
            } else if (diff > 1L) {
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
        }
        return maxOf(maxStreak, currentStreak)
    }

    private fun buildWeeklyChart(recordsSnap: DataSnapshot): List<WeeklyBarData> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayFormat = SimpleDateFormat("E", Locale("ar"))
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -6)

        val list = mutableListOf<WeeklyBarData>()

        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            val dayName = dayFormat.format(cal.time)

            val record = recordsSnap.child(dateStr).child(currentUid)
            var heightPercent = 0.05f
            var statusStr = "missed"

            if (record.exists()) {
                val status = record.child("status").value as? String
                when (status) {
                    "awake" -> {
                        heightPercent = 1.0f
                        statusStr = "awake"
                    }
                    "travel" -> {
                        heightPercent = 1.0f
                        statusStr = "travel"
                    }
                    "challenge_done" -> {
                        heightPercent = 0.7f
                        statusStr = "challenge_done"
                    }
                    "ringing" -> {
                        heightPercent = 0.4f
                        statusStr = "ringing"
                    }
                    else -> {
                        heightPercent = 0.05f
                        statusStr = "missed"
                    }
                }
            }

            list.add(WeeklyBarData(dayName, heightPercent, statusStr))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return list
    }

    private fun buildMonthlyCalendar(recordsSnap: DataSnapshot): Map<Int, String> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val map = mutableMapOf<Int, String>()

        val tempCal = Calendar.getInstance()
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..maxDays) {
            tempCal.set(Calendar.YEAR, currentYear)
            tempCal.set(Calendar.MONTH, currentMonth)
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = sdf.format(tempCal.time)

            val record = recordsSnap.child(dateStr).child(currentUid)
            if (record.exists()) {
                val status = record.child("status").value as? String
                if (status != null) {
                    map[day] = status
                }
            }
        }

        return map
    }

    private fun buildLeaderboard(recordsSnap: DataSnapshot, membersSnap: DataSnapshot?): Triple<List<LeaderboardItem>, String, String> {
        val tempMap = mutableMapOf<String, Int>()
        val rescuesMap = mutableMapOf<String, Int>()

        for (dateSnap in recordsSnap.children) {
            for (memberRecord in dateSnap.children) {
                val mId = memberRecord.key ?: continue
                val status = memberRecord.child("status").value as? String
                if (status == "awake" || status == "challenge_done" || status == "travel") {
                    tempMap[mId] = (tempMap[mId] ?: 0) + 1
                }

                val confirmedBy = memberRecord.child("confirmedBy").value as? String
                if (!confirmedBy.isNullOrEmpty()) {
                    rescuesMap[confirmedBy] = (rescuesMap[confirmedBy] ?: 0) + 1
                }
            }
        }

        val tempList = mutableListOf<LeaderboardItem>()
        if (membersSnap != null) {
            for (mSnap in membersSnap.children) {
                val mId = mSnap.key ?: continue
                val name = mSnap.child("displayName").value as? String ?: "عضو"
                val photo = mSnap.child("photoUrl").value as? String ?: ""
                val activeDays = tempMap[mId] ?: 0
                val rescues = rescuesMap[mId] ?: 0

                tempList.add(LeaderboardItem(0, mId, name, photo, activeDays, rescues))
            }
        }

        tempList.sortByDescending { it.streak }

        val leaderboardList = tempList.mapIndexed { index, item ->
            item.copy(rank = index + 1)
        }

        val fastestMember = if (leaderboardList.isNotEmpty()) leaderboardList.first().displayName else "لا يوجد"
        val topRescuerItem = leaderboardList.maxByOrNull { it.rescues }
        val topRescuer = if (topRescuerItem != null && topRescuerItem.rescues > 0) topRescuerItem.displayName else "لا أحد"

        return Triple(leaderboardList, fastestMember, topRescuer)
    }

    private fun buildAchievements(currentStreak: Int, longestStreak: Int, rescues: Int, totalFajr: Int): List<Achievement> {
        val currentDateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())

        return listOf(
            Achievement(
                "week_streak", "وسام الأسبوع 🌟",
                "إكمال سلسلة التزام 7 أيام متتالية",
                "🌟", "#2ECC71",
                if (longestStreak >= 7) currentDateStr else null
            ),
            Achievement(
                "month_streak", "بطل الشهر 🏆",
                "إكمال سلسلة التزام 30 يوماً متتالية",
                "🏆", "#FFD700",
                if (longestStreak >= 30) currentDateStr else null
            ),
            Achievement(
                "rescue_10", "البطل المنقذ 🦸",
                "تأكيد استيقاظ الأصدقاء 10 مرات",
                "🦸", "#4A1A6B",
                if (rescues >= 10) currentDateStr else null
            ),
            Achievement(
                "early_bird", "الطائر المبكر 🌅",
                "حل التحدي بنجاح في أول دقيقة من الرنين",
                "🌅", "#2ECC71",
                if (totalFajr >= 5) currentDateStr else null
            ),
            Achievement(
                "guardian", "الحارس الوفي 🛡️",
                "الالتزام بالدائرة كمسؤول أول نشط",
                "🛡️", "#4A1A6B",
                if (totalFajr >= 15) currentDateStr else null
            ),
            Achievement(
                "warrior", "محارب الفجر ⚔️",
                "الالتزام الكامل بصلاة الفجر 60 يوماً",
                "⚔️", "#FFD700",
                if (totalFajr >= 60) currentDateStr else null
            )
        )
    }

    private fun setupPlaceholderStats() {
        _uiState.value = StatsUiState(
            isLoading = false,
            achievements = buildAchievements(0, 0, 0, 0)
        )
    }
}
