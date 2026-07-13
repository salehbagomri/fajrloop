package com.bagomri.fajrloop.ui.stats

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.databinding.ActivityStatsBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private var halqaId: String? = null
    private var currentUid = ""
    
    private val dayStatusMap = mutableMapOf<Int, String>()
    private val leaderboardList = mutableListOf<LeaderboardItem>()
    private val achievementsList = mutableListOf<Achievement>()
    
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private lateinit var achievementsAdapter: AchievementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. بيانات المستخدم والحلقة
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        halqaId = prefs.getString("current_halqa_id", null)
        currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // زر الرجوع
        binding.btnBack.setOnClickListener { finish() }

        // زر المشاركة للتقرير الشهري
        binding.btnShareReport.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                val currentStreak = binding.textCurrentStreak.text.toString()
                val totalFajr = binding.textTotalFajr.text.toString()
                val totalRescues = binding.textTotalRescues.text.toString()
                putExtra(
                    Intent.EXTRA_TEXT,
                    "تقرير التزامي بصلاة الفجر في تطبيق حلقة الفجر! 🌅🕋\nسلسلتي الحالية: $currentStreak\nإجمالي الأيام: $totalFajr\nنقاط حماية الحلقة (الإنقاذ): $totalRescues\nانضم إلينا وحافظ على فجرك!"
                )
            }
            startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير الفجر"))
        }

        // 2. إعداد التبويبات والتبديل بين الصفحات
        setupTabs()

        // 3. تهيئة القوائم والـ Adapters
        leaderboardAdapter = LeaderboardAdapter(leaderboardList, currentUid)
        binding.recyclerLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.recyclerLeaderboard.adapter = leaderboardAdapter

        achievementsAdapter = AchievementsAdapter(achievementsList)
        binding.recyclerAchievements.layoutManager = LinearLayoutManager(this)
        binding.recyclerAchievements.adapter = achievementsAdapter

        // 4. تحميل وتحليل البيانات سحابياً
        if (!halqaId.isNullOrEmpty()) {
            fetchStatsAndRecords()
        } else {
            setupPlaceholderStats()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.scrollMyActivity.visibility = View.VISIBLE
                        binding.layoutLeaderboard.visibility = View.GONE
                        binding.recyclerAchievements.visibility = View.GONE
                    }
                    1 -> {
                        binding.scrollMyActivity.visibility = View.GONE
                        binding.layoutLeaderboard.visibility = View.VISIBLE
                        binding.recyclerAchievements.visibility = View.GONE
                    }
                    2 -> {
                        binding.scrollMyActivity.visibility = View.GONE
                        binding.layoutLeaderboard.visibility = View.GONE
                        binding.recyclerAchievements.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun fetchStatsAndRecords() {
        val rootRef = FirebaseDatabase.getInstance().reference
        
        // جلب سجلات الأيام بالكامل لتخريج الإحصائيات
        rootRef.child("dailyRecords").child(halqaId!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(recordsSnap: DataSnapshot) {
                    // جلب قائمة الأعضاء لحساب الصدارة
                    rootRef.child("halqas").child(halqaId!!).child("members")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(membersSnap: DataSnapshot) {
                                processStats(recordsSnap, membersSnap)
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun processStats(recordsSnap: DataSnapshot, membersSnap: DataSnapshot) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        // 1. استخراج تواريخ السجلات وحالة المستخدم الحالي
        val userCheckinDates = mutableSetOf<String>()
        val allDatesList = mutableListOf<String>()
        var rescuesCount = 0

        // تحليل جميع السجلات السحابية
        for (dateSnap in recordsSnap.children) {
            val dateStr = dateSnap.key ?: continue
            allDatesList.add(dateStr)

            val userRecord = dateSnap.child(currentUid)
            if (userRecord.exists()) {
                val status = userRecord.child("status").value as? String
                if (status == "awake" || status == "challenge_done" || status == "travel") {
                    userCheckinDates.add(dateStr)
                }
            }

            // حساب عدد عمليات الإنقاذ
            for (memberRecord in dateSnap.children) {
                val confirmedBy = memberRecord.child("confirmedBy").value as? String
                if (confirmedBy == currentUid) {
                    rescuesCount++
                }
            }
        }

        // ترتيب التواريخ
        allDatesList.sort()
        userCheckinDates.sorted()

        // 2. حساب السلسلة الحالية وأطول سلسلة
        val currentStreak = calculateCurrentStreak(userCheckinDates)
        val longestStreak = calculateLongestStreak(userCheckinDates)

        binding.textCurrentStreak.text = "$currentStreak أيام"
        binding.textLongestStreak.text = "$longestStreak يوم"
        binding.textTotalFajr.text = "${userCheckinDates.size} يوم"
        binding.textTotalRescues.text = "$rescuesCount 🦸"

        // 3. ملء المخطط الأسبوعي للـ 7 أيام الأخيرة
        populateWeeklyChart(recordsSnap)

        // 4. ملء تقويم الشهر الحالي
        populateMonthlyCalendar(recordsSnap)

        // 5. ملء لوحة الصدارة
        populateLeaderboard(recordsSnap, membersSnap)

        // 6. تهيئة وحساب الأوسمة المكتسبة
        populateAchievements(currentStreak, longestStreak, rescuesCount, userCheckinDates.size)
    }

    private fun calculateCurrentStreak(dates: Set<String>): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        
        var streak = 0
        // نبدأ من اليوم الحالي ونعود للخلف
        while (true) {
            val dateStr = sdf.format(cal.time)
            if (dates.contains(dateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                // إذا كان اليوم الحالي غير موجود، نتحقق من البارحة، إذا كان البارحة موجوداً نواصل من البارحة
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
        val sortedDates = dates.map { sdf.parse(it)!! }.sorted()
        
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

    private fun populateWeeklyChart(recordsSnap: DataSnapshot) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayFormat = SimpleDateFormat("E", Locale("ar"))
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -6) // العودة 6 أيام للخلف لنمثل 7 أيام

        val barViews = arrayOf(
            binding.barDay1, binding.barDay2, binding.barDay3,
            binding.barDay4, binding.barDay5, binding.barDay6, binding.barDay7
        )

        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            val dayName = dayFormat.format(cal.time)

            val record = recordsSnap.child(dateStr).child(currentUid)
            var heightPercent = 0.05f // حد أدنى للرسم البصري للغياب
            var barColor = Color.parseColor("#E74C3C") // افتراضاً غائب

            if (record.exists()) {
                val status = record.child("status").value as? String
                val confirmedBy = record.child("confirmedBy").value as? String
                
                when (status) {
                    "awake" -> {
                        heightPercent = 1.0f
                        barColor = if (!confirmedBy.isNullOrEmpty()) Color.parseColor("#2ECC71") else Color.parseColor("#2ECC71")
                    }
                    "travel" -> {
                        heightPercent = 1.0f
                        barColor = Color.parseColor("#3498DB") // أزرق سفر
                    }
                    "challenge_done" -> {
                        heightPercent = 0.7f
                        barColor = Color.parseColor("#FFD700") // ذهبي حل التحدي
                    }
                    "ringing" -> {
                        heightPercent = 0.4f
                        barColor = Color.parseColor("#B57CFF")
                    }
                    else -> {
                        heightPercent = 0.05f
                        barColor = Color.parseColor("#E74C3C")
                    }
                }
            }

            // تطبيق القيم على العمود
            val barView = barViews[i]
            val textLabel = barView.textDayLabel
            val viewColor = barView.viewBarColor

            textLabel.text = dayName
            viewColor.setBackgroundColor(barColor)

            // تعديل ارتفاع العمود الملون ديناميكياً
            viewColor.post {
                val parentHeight = (viewColor.parent as View).height
                val params = viewColor.layoutParams
                params.height = (parentHeight * heightPercent).toInt().coerceAtLeast(1)
                viewColor.layoutParams = params
            }

            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    private fun populateMonthlyCalendar(recordsSnap: DataSnapshot) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)

        dayStatusMap.clear()

        // ملء التواريخ للشهر الحالي
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
                    dayStatusMap[day] = status
                }
            }
        }

        binding.gridCalendar.adapter = CalendarAdapter(this, dayStatusMap, currentDay)
    }

    private fun populateLeaderboard(recordsSnap: DataSnapshot, membersSnap: DataSnapshot) {
        leaderboardList.clear()

        val tempMap = mutableMapOf<String, Int>() // Uid -> ActiveDays
        val rescuesMap = mutableMapOf<String, Int>() // Uid -> Rescues

        // احتساب الأيام النشطة والإنقاذ لكل عضو
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

        // بناء قائمة الصدارة
        var index = 1
        val tempList = mutableListOf<LeaderboardItem>()
        for (mSnap in membersSnap.children) {
            val mId = mSnap.key ?: continue
            val name = mSnap.child("displayName").value as? String ?: "عضو"
            val photo = mSnap.child("photoUrl").value as? String ?: ""
            val activeDays = tempMap[mId] ?: 0
            val rescues = rescuesMap[mId] ?: 0

            tempList.add(LeaderboardItem(0, mId, name, photo, activeDays, rescues))
        }

        // الترتيب تنازلياً حسب التزام الأيام الفعلي
        tempList.sortByDescending { it.streak }

        // تعيين الرتب
        for (i in tempList.indices) {
            val item = tempList[i]
            leaderboardList.add(item.copy(rank = i + 1))
        }

        leaderboardAdapter.notifyDataSetChanged()

        // تعيين شارات التميز بالرأس
        if (leaderboardList.isNotEmpty()) {
            binding.textFastestMember.text = leaderboardList.first().displayName
            val topRescuer = leaderboardList.maxByOrNull { it.rescues }
            binding.textTopRescuer.text = if (topRescuer != null && topRescuer.rescues > 0) topRescuer.displayName else "لا أحد"
        }
    }

    private fun populateAchievements(currentStreak: Int, longestStreak: Int, rescues: Int, totalFajr: Int) {
        achievementsList.clear()

        val currentDateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())

        // 1. وسام إكمال سلسلة 7 أيام
        achievementsList.add(
            Achievement(
                "week_streak", "وسام الأسبوع 🌟",
                "إكمال سلسلة التزام 7 أيام متتالية",
                "🌟", "#2ECC71",
                if (longestStreak >= 7) currentDateStr else null
            )
        )

        // 2. وسام إكمال سلسلة 30 يوماً
        achievementsList.add(
            Achievement(
                "month_streak", "بطل الشهر 🏆",
                "إكمال سلسلة التزام 30 يوماً متتالية",
                "🏆", "#FFD700",
                if (longestStreak >= 30) currentDateStr else null
            )
        )

        // 3. وسام البطل (إنقاذ الأصدقاء 10 مرات)
        achievementsList.add(
            Achievement(
                "rescue_10", "البطل المنقذ 🦸",
                "تأكيد استيقاظ الأصدقاء 10 مرات",
                "🦸", "#4A1A6B",
                if (rescues >= 10) currentDateStr else null
            )
        )

        // 4. وسام الحل المبكر
        achievementsList.add(
            Achievement(
                "early_bird", "الطائر المبكر 🌅",
                "حل التحدي بنجاح في أول دقيقة من الرنين",
                "🌅", "#2ECC71",
                if (totalFajr >= 5) currentDateStr else null // شروط مبسطة
            )
        )

        // 5. وسام الحارس
        achievementsList.add(
            Achievement(
                "guardian", "الحارس الوفي 🛡️",
                "الالتزام بالدائرة كمسؤول أول نشط",
                "🛡️", "#4A1A6B",
                if (totalFajr >= 15) currentDateStr else null
            )
        )

        // 6. وسام محارب الفجر
        achievementsList.add(
            Achievement(
                "warrior", "محارب الفجر ⚔️",
                "الالتزام الكامل بصلاة الفجر 60 يوماً",
                "⚔️", "#FFD700",
                if (totalFajr >= 60) currentDateStr else null
            )
        )

        achievementsAdapter.notifyDataSetChanged()
    }

    private fun setupPlaceholderStats() {
        binding.textCurrentStreak.text = "0 أيام"
        binding.textLongestStreak.text = "0 يوم"
        binding.textTotalFajr.text = "0 يوم"
        binding.textTotalRescues.text = "0 🦸"
        
        binding.textFastestMember.text = "لا حلقة"
        binding.textTopRescuer.text = "لا حلقة"

        binding.gridCalendar.adapter = CalendarAdapter(this, emptyMap(), 1)
        populateAchievements(0, 0, 0, 0)
    }
}
