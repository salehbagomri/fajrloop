package com.bagomri.fajrloop.ui.main

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.alarm.AlarmScheduler
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.databinding.ActivityMainBinding
import com.bagomri.fajrloop.ui.auth.LoginActivity
import com.bagomri.fajrloop.ui.permissions.PermissionSetupActivity
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * MainActivity — الشاشة الرئيسية لحلقة الفجر
 *
 * تعرض:
 * - رأس الصفحة: السلام عليكم + اسم المستخدم + صورته + أيقونة الإعدادات
 * - بطاقة عداد الفجر التنازلي (وقت الفجر + الشروق)
 * - ملخص اليوم (مستيقظين X/Y)
 * - تنبيه استيقاظ صديق (للمسؤول فقط)
 * - شبكة الإجراءات السريعة (4 أزرار)
 * - قسم الحلقة الدائرية مع الأعضاء
 * - المحتوى الروحي (آية/حديث)
 * - أدوات اختبار المنبه (للتطوير)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    // خصائص إدارة الحلقة
    private var halqaObserver: com.google.firebase.database.ValueEventListener? = null
    private var currentActiveHalqaId: String? = null
    private var isCurrentUserAdmin: Boolean = false

    private var dailyRecordsObserver: com.google.firebase.database.ValueEventListener? = null
    private var dailyRecordsRef: com.google.firebase.database.DatabaseReference? = null
    private var lastMembersSnap: com.google.firebase.database.DataSnapshot? = null
    private var lastChain: List<String> = emptyList()

    // عداد الفجر
    private val countdownHandler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null

    // تبويب المحتوى الروحي (0=آية, 1=حديث)
    private var currentSpiritualTab = 0

    // بيانات المحتوى الروحي من content_strings.md
    private val ayat = listOf(
        Pair("أَقِمِ الصَّلَاةَ لِدُلُوكِ الشَّمْسِ إِلَىٰ غَسَقِ اللَّيْلِ وَقُرْآنَ الْفَجْرِ ۖ إِنَّ قُرْآنَ الْفَجْرِ كَانَ مَشْهُودًا", "سورة الإسراء: 78"),
        Pair("وَسَبِّحْ بِحَمْدِ رَبِّكَ قَبْلَ طُلُوعِ الشَّمْسِ وَقَبْلَ غُرُوبِهَا", "سورة طه: 130"),
        Pair("وَالْفَجْرِ * وَلَيَالٍ عَشْرٍ", "سورة الفجر: 1-2")
    )

    private val ahadith = listOf(
        Pair("رَكْعَتَا الْفَجْرِ خَيْرٌ مِنَ الدُّنْيَا وَمَا فِيهَا.", "صحيح مسلم"),
        Pair("مَنْ صَلَّى الصُّبْحَ فَهُوَ فِي ذِمَّةِ اللَّهِ.", "صحيح مسلم"),
        Pair("لَنْ يَلِجَ النَّارَ أَحَدٌ صَلَّى قَبْلَ طُلُوعِ الشَّمْسِ وَقَبْلَ غُرُوبِهَا.", "صحيح مسلم")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!AuthManager.isUserSignedIn()) {
            navigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // تخصيص لون خلفية وحدود كرت إحصائيات المستيقظين باللون الأخضر الشفاف وتقليل زواياه لـ 12dp
        binding.cardAwakeBadge.setCornerRadiusDp(12f)
        binding.cardAwakeBadge.setCustomBgAndBorder(
            Color.argb(30, 0x2E, 0xCC, 0x71), // خلفية خضراء بـ 12% شفافية
            Color.argb(102, 0x2E, 0xCC, 0x71) // حدود خضراء بـ 40% شفافية
        )

        prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)

        checkAndRequestPermissions()
        updateUserProfileUI()
        setupSpiritualContent()
        setupQuickActionsUI()
        setupHalqaUI()
        startFajrCountdown()
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownRunnable?.let { countdownHandler.removeCallbacks(it) }
        halqaObserver?.let {
            com.bagomri.fajrloop.data.HalqaManager.removeObserver(it)
        }
        stopObservingDailyRecords()
    }

    // ==================== عداد الفجر التنازلي ====================

    /**
     * يبدأ عداد يتجدد كل ثانية ويحسب الوقت المتبقي على صلاة الفجر.
     * يستخدم وقت الفجر المحفوظ محلياً (أو وقتاً تجريبياً للآن).
     */
    private fun startFajrCountdown() {
        // حساب وقت الفجر المقدر لليوم (الساعة 3:56 صباحاً كقيمة افتراضية لمكة)
        val fajrCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 56)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sunriseCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 5)
            set(Calendar.MINUTE, 18)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // إذا مضى وقت الفجر، انتقل للغد
        if (fajrCal.timeInMillis < System.currentTimeMillis()) {
            fajrCal.add(Calendar.DAY_OF_YEAR, 1)
            sunriseCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val arLocale = Locale.forLanguageTag("ar")
        val fajrTimeStr = SimpleDateFormat("hh:mm a", arLocale).format(fajrCal.time)
        val sunriseTimeStr = SimpleDateFormat("hh:mm a", arLocale).format(sunriseCal.time)
        binding.textFajrTime.text = fajrTimeStr
        binding.textSunriseTime.text = sunriseTimeStr


        countdownRunnable = object : Runnable {
            override fun run() {
                val remaining = fajrCal.timeInMillis - System.currentTimeMillis()
                if (remaining > 0) {
                    val hours   = TimeUnit.MILLISECONDS.toHours(remaining)
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60
                    binding.textFajrCountdown.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(remaining)

                    // ── لون العداد حسب الوقت المتبقي (design_tokens.md: FajrCountdownWidget) ──
                    val textColor = when {
                        totalMinutes >= 60 -> "#2ECC71"   // successGreen — وقت كافٍ
                        totalMinutes in 15..59 -> "#FFD700" // accentGold — اقتراب
                        else -> "#E74C3C"                  // dangerRed  — أقل من 15 دقيقة
                    }
                    binding.textFajrCountdown.setTextColor(Color.parseColor(textColor))

                    // ── لون حد بطاقة الفجر الديناميكي ──
                    val glassCard = binding.cardFajrCountdown as? com.bagomri.fajrloop.ui.widget.GlassCardView
                    when {
                        totalMinutes < 5  -> {
                            // أقل من 5 دقائق: حد أحمر 50% + نبض scale
                            glassCard?.setBorderColor(Color.argb(128, 0xE7, 0x4C, 0x3C))
                            glassCard?.startPulse()
                        }
                        totalMinutes < 15 -> {
                            // أقل من 15 دقيقة: حد أحمر 50% بدون نبض
                            glassCard?.setBorderColor(Color.argb(128, 0xE7, 0x4C, 0x3C))
                            glassCard?.stopPulse()
                        }
                        totalMinutes < 60 -> {
                            // أقل من 60 دقيقة: حد ذهبي
                            glassCard?.setBorderColor(Color.argb(100, 0xFF, 0xD7, 0x00))
                            glassCard?.stopPulse()
                        }
                        else -> {
                            // وقت كافٍ: حد افتراضي
                            glassCard?.resetBorderColor()
                            glassCard?.stopPulse()
                        }
                    }
                } else {
                    binding.textFajrCountdown.text = "00:00:00"
                    binding.textFajrCountdown.setTextColor(Color.parseColor("#E74C3C"))
                }
                countdownHandler.postDelayed(this, 1000)
            }
        }
        countdownHandler.post(countdownRunnable!!)
    }

    // ==================== المحتوى الروحي ====================

    private fun setupSpiritualContent() {
        val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        showSpiritualTab(0, dayIndex)

        binding.btnTabAya.setOnClickListener {
            currentSpiritualTab = 0
            showSpiritualTab(0, dayIndex)
        }
        binding.btnTabHadith.setOnClickListener {
            currentSpiritualTab = 1
            showSpiritualTab(1, dayIndex)
        }
    }

    private fun showSpiritualTab(tab: Int, dayIndex: Int) {
        if (tab == 0) {
            val item = ayat[dayIndex % ayat.size]
            binding.imageSpiritualIcon.setImageResource(R.drawable.ic_mosque)
            binding.textSpiritualContent.text = "» ${item.first} «"
            binding.textSpiritualSource.text = item.second

            // تحديث مظهر التبويبات (ذهبي شفاف 10% وبوردر 30%)
            binding.btnTabAya.apply {
                setTextColor(Color.parseColor("#FFD700"))
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.argb(26, 0xFF, 0xD7, 0x00))
                strokeColor = android.content.res.ColorStateList.valueOf(Color.argb(77, 0xFF, 0xD7, 0x00))
                strokeWidth = dpToPx(1)
            }
            binding.btnTabHadith.apply {
                setTextColor(Color.parseColor("#B0B0C5"))
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
                strokeColor = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
                strokeWidth = 0
            }
        } else {
            val item = ahadith[dayIndex % ahadith.size]
            binding.imageSpiritualIcon.setImageResource(R.drawable.ic_chat)
            binding.textSpiritualContent.text = "« ${item.first} »"
            binding.textSpiritualSource.text = item.second

            binding.btnTabAya.apply {
                setTextColor(Color.parseColor("#B0B0C5"))
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
                strokeColor = android.content.res.ColorStateList.valueOf(Color.TRANSPARENT)
                strokeWidth = 0
            }
            binding.btnTabHadith.apply {
                setTextColor(Color.parseColor("#FFD700"))
                backgroundTintList = android.content.res.ColorStateList.valueOf(Color.argb(26, 0xFF, 0xD7, 0x00))
                strokeColor = android.content.res.ColorStateList.valueOf(Color.argb(77, 0xFF, 0xD7, 0x00))
                strokeWidth = dpToPx(1)
            }
        }
    }

    private fun setupQuickActionsUI() {
        // داخل حلقة
        binding.cardActionHalqaDetails.setOnClickListener {
            showHalqaDetailsBottomSheet()
        }
        binding.cardActionChat.setOnClickListener {
            startActivity(Intent(this, com.bagomri.fajrloop.ui.chat.ChatActivity::class.java))
        }
        binding.cardActionStats.setOnClickListener {
            startActivity(Intent(this, com.bagomri.fajrloop.ui.stats.StatsActivity::class.java))
        }
        binding.cardActionInvite.setOnClickListener {
            shareInviteCode()
        }

        // أزرار في بطاقة لا توجد حلقة (card_no_halqa)
        binding.btnCreateHalqa.setOnClickListener { showCreateHalqaDialog() }
        binding.btnJoinHalqa.setOnClickListener { showJoinHalqaDialog() }

        // خارج حلقة
        binding.cardActionCreateHalqa.setOnClickListener {
            showCreateHalqaDialog()
        }
        binding.cardActionJoinHalqa.setOnClickListener {
            showJoinHalqaDialog()
        }
        binding.cardActionStatsAlone.setOnClickListener {
            startActivity(Intent(this, com.bagomri.fajrloop.ui.stats.StatsActivity::class.java))
        }
    }

    private fun shareInviteCode() {
        val halqaId = currentActiveHalqaId ?: run {
            showToast("يرجى الانضمام لحلقة أولاً")
            return
        }
        com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("halqas").child(halqaId)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val name = snapshot.child("name").value as? String ?: "حلقة الفجر"
                    val code = snapshot.child("inviteCode").value as? String ?: ""
                    val shareText = "🌙 انضم لحلقة الفجر!\nاسم الحلقة: $name\nكود الدعوة: $code\nحمّل التطبيق لتستيقظ للفجر معي!"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    startActivity(Intent.createChooser(shareIntent, "مشاركة الكود"))
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
    }

    private fun showCreateHalqaDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = "مثال: حلقة الفجر المباركة"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#6B6B8A"))
            gravity = Gravity.END
            setPadding(32, 24, 32, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("إنشاء حلقة فجر")
            .setMessage("أدخل اسماً يعبر عن حلقتكم.")
            .setView(editText)
            .setPositiveButton("إنشاء وتوليد كود الدعوة 🚀") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.length < 3) {
                    showToast("يجب أن يكون اسم الحلقة 3 أحرف على الأقل")
                    return@setPositiveButton
                }
                com.bagomri.fajrloop.data.HalqaManager.createHalqa(name) { success, result ->
                    if (success) showToast("تم إنشاء الحلقة بنجاح! 🎉")
                    else showToast("خطأ: $result")
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun showJoinHalqaDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = "مثال: FJR-A8B9"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.parseColor("#6B6B8A"))
            gravity = Gravity.CENTER
            setPadding(32, 24, 32, 24)
        }
        AlertDialog.Builder(this)
            .setTitle("انضم لصديقك 🔑")
            .setMessage("أدخل كود الدعوة المكون من 4 رموز (مثال: FJR-A8B9)")
            .setView(editText)
            .setPositiveButton("تحقق وانضم للحلقة 🤝") { _, _ ->
                val code = editText.text.toString().trim()
                if (code.isEmpty()) {
                    showToast("يرجى إدخال كود الدعوة")
                    return@setPositiveButton
                }
                com.bagomri.fajrloop.data.HalqaManager.joinHalqa(code) { success, result ->
                    if (success) showToast("تم الانضمام بنجاح! 🎉")
                    else showToast("خطأ: $result")
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    // ==================== صلاحيات المنبه ====================

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                binding.cardPermissionWarning.visibility = View.VISIBLE
            } else {
                binding.cardPermissionWarning.visibility = View.GONE
            }
        } else {
            binding.cardPermissionWarning.visibility = View.GONE
        }

        binding.btnCheckPermissions.setOnClickListener {
            startActivity(Intent(this, PermissionSetupActivity::class.java))
        }
    }



    // ==================== الملف الشخصي ====================

    private fun updateUserProfileUI() {
        val user = AuthManager.currentUser
        binding.textUserName.text = user?.displayName ?: "المستخدم"

        // تحميل صورة المستخدم من Google
        val photoUrl = user?.photoUrl
        if (photoUrl != null) {
            Glide.with(this)
                .load(photoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
                .into(binding.imageUserAvatar)
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, com.bagomri.fajrloop.ui.settings.SettingsActivity::class.java))
        }
    }

    // ==================== إدارة الحلقة ====================

    private fun stopObservingDailyRecords() {
        dailyRecordsObserver?.let { dailyRecordsRef?.removeEventListener(it) }
        dailyRecordsObserver = null
        dailyRecordsRef = null
    }

    private fun setupHalqaUI() {
        halqaObserver = com.bagomri.fajrloop.data.HalqaManager.observeUserHalqa { snapshot ->
            if (snapshot == null || !snapshot.exists()) {
                currentActiveHalqaId = null
                prefs.edit().putString("current_halqa_id", null).apply()
                isCurrentUserAdmin = false
                // حالة خارج حلقة
                binding.layoutHasHalqa.visibility = View.GONE
                binding.cardNoHalqa.visibility = View.VISIBLE
                binding.layoutQuickActionsInHalqa.visibility = View.GONE
                binding.layoutQuickActionsNoHalqa.visibility = View.VISIBLE
                binding.cardTodaySummary.visibility = View.GONE
                binding.cardFriendWakeAlert.visibility = View.GONE
                stopObservingDailyRecords()
            } else {
                currentActiveHalqaId = snapshot.key
                prefs.edit().putString("current_halqa_id", currentActiveHalqaId).apply()
                // حالة داخل حلقة
                binding.layoutHasHalqa.visibility = View.VISIBLE
                binding.cardNoHalqa.visibility = View.GONE
                binding.layoutQuickActionsInHalqa.visibility = View.VISIBLE
                binding.layoutQuickActionsNoHalqa.visibility = View.GONE
                binding.cardTodaySummary.visibility = View.VISIBLE

                val name = snapshot.child("name").value as? String ?: "حلقة"
                binding.textHalqaName.text = "حلقة: $name"

                val chain = (snapshot.child("chain").value as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                lastChain = chain

                val membersSnap = snapshot.child("members")
                lastMembersSnap = membersSnap

                val currentUid = AuthManager.getUserId()
                isCurrentUserAdmin = membersSnap.child(currentUid ?: "").child("role").value as? String == "admin"

                // عرض/إخفاء زر إعادة الترتيب للمسؤول
                binding.btnReorderChain.visibility = if (isCurrentUserAdmin) View.VISIBLE else View.GONE

                startObservingDailyRecords(currentActiveHalqaId!!)
            }
        }
    }

    private fun startObservingDailyRecords(halqaId: String) {
        stopObservingDailyRecords()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        dailyRecordsRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)

        dailyRecordsObserver = dailyRecordsRef?.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                lastMembersSnap?.let { mSnap ->
                    populateHorizontalLoopChain(lastChain, mSnap, snapshot)
                    updateTodaySummary(lastChain, snapshot)
                    checkFriendWakeAlert(lastChain, mSnap, snapshot)
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    private fun updateTodaySummary(chain: List<String>, recordsSnap: com.google.firebase.database.DataSnapshot) {
        val total = chain.size
        val awake = chain.count { uid ->
            val status = recordsSnap.child(uid).child("status").value as? String
            status == "awake"
        }
        binding.textAwakeCount.text = "$awake / $total"
        if (awake == total && total > 0) {
            binding.textTodaySummary.text = "ما شاء الله! استيقظت الحلقة بالكامل 🎉"
        } else {
            binding.textTodaySummary.text = "استيقظ $awake من أصل $total أعضاء حتى الآن."
        }
    }

    private fun checkFriendWakeAlert(
        chain: List<String>,
        membersSnap: com.google.firebase.database.DataSnapshot,
        recordsSnap: com.google.firebase.database.DataSnapshot
    ) {
        val currentUid = AuthManager.getUserId() ?: return
        // البحث عن صديق حل التحدي ونحن مسؤولون عن إيقاظه
        for (uid in chain) {
            val memberSnap = membersSnap.child(uid)
            val responsibleForUserId = memberSnap.child("responsibleForUserId").value as? String
            val status = recordsSnap.child(uid).child("status").value as? String
            if (status == "challenge_done" && responsibleForUserId == currentUid) {
                val firstName = (memberSnap.child("displayName").value as? String ?: "صديقك").split(" ").first()
                binding.cardFriendWakeAlert.visibility = View.VISIBLE
                binding.textFriendWakeMessage.text = "صديقك $firstName حل تحدي الاستيقاظ وبانتظار تأكيدك لإيقاف منبهه."
                binding.btnConfirmFriendWake.text = "تأكيد استيقاظ $firstName وإيقاف منبهه"
                binding.btnConfirmFriendWake.setOnClickListener {
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    currentActiveHalqaId?.let { hId ->
                        com.google.firebase.database.FirebaseDatabase.getInstance()
                            .getReference("dailyRecords")
                            .child(hId)
                            .child(currentDate)
                            .child(uid)
                            .updateChildren(mapOf(
                                "status" to "awake",
                                "confirmedBy" to currentUid
                            ))
                            .addOnSuccessListener {
                                showToast("تم إيقاف منبه صديقك بنجاح. كتب الله أجرك! 🟢")
                                binding.cardFriendWakeAlert.visibility = View.GONE
                            }
                            .addOnFailureListener { e ->
                                showToast("فشل تأكيد الاستيقاظ: ${e.message}")
                            }
                    }
                }
                return
            }
        }
        binding.cardFriendWakeAlert.visibility = View.GONE
    }

    private fun populateHorizontalLoopChain(
        chain: List<String>,
        membersSnap: com.google.firebase.database.DataSnapshot,
        recordsSnap: com.google.firebase.database.DataSnapshot
    ) {
        binding.layoutLoopChain.removeAllViews()
        val n = chain.size
        val currentUid = AuthManager.getUserId() ?: ""

        for (i in 0 until n) {
            val mId = chain[i]
            val mSnap = membersSnap.child(mId)
            if (!mSnap.exists()) continue

            val displayName = mSnap.child("displayName").value as? String ?: "عضو"
            val photoUrl = mSnap.child("photoUrl").value as? String

            var status = "pending"
            val profileStatus = mSnap.child("status").value as? String
            if (profileStatus == "travel" || profileStatus == "traveling") {
                status = "travel"
            } else if (recordsSnap.child(mId).exists()) {
                status = recordsSnap.child(mId).child("status").value as? String ?: "pending"
            }

            // نفخ عنصر العضو الأفقي
            val itemView = layoutInflater.inflate(R.layout.item_loop_member, binding.layoutLoopChain, false)
            val imgAvatar = itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.image_loop_avatar)
            val imgStatus = itemView.findViewById<android.widget.ImageView>(R.id.image_loop_status_icon)
            val txtName = itemView.findViewById<android.widget.TextView>(R.id.text_loop_member_name)
            val txtStatus = itemView.findViewById<android.widget.TextView>(R.id.text_loop_member_status)

            // تخصيص الألوان والشارات حسب الحالة اليومية
            val statusColor: String
            val statusText: String
            val statusIconRes: Int

            when (status) {
                "travel" -> {
                    statusColor = "#3498DB" // أزرق سفر
                    statusText = "مسافر ✈️"
                    statusIconRes = R.drawable.ic_travel
                }
                "awake" -> {
                    statusColor = "#2ECC71" // أخضر
                    statusText = "مستيقظ"
                    statusIconRes = R.drawable.ic_circle_check
                }
                "challenge_done" -> {
                    statusColor = "#FFD700" // ذهبي
                    statusText = "حل التحدي"
                    statusIconRes = R.drawable.ic_circle_warning
                }
                "panic" -> {
                    statusColor = "#E74C3C" // أحمر
                    statusText = "استغاثة"
                    statusIconRes = R.drawable.ic_circle_warning
                }
                "ringing" -> {
                    statusColor = "#B57CFF" // بنفسجي
                    statusText = "يرن المنبه"
                    statusIconRes = R.drawable.ic_alarm_notification
                }
                "missed" -> {
                    statusColor = "#6B6B8A" // رمادي
                    statusText = "فاته الفجر"
                    statusIconRes = R.drawable.ic_alarm_off
                }
                else -> {
                    statusColor = "#B0B0C5" // رمادي فاتح
                    statusText = "نائم"
                    statusIconRes = R.drawable.ic_circle_warning
                }
            }

            txtName.text = displayName.split(" ").first()
            txtStatus.text = statusText
            txtStatus.setTextColor(Color.parseColor(statusColor))
            imgAvatar.borderColor = Color.parseColor(statusColor)

            // تحميل الصورة
            if (!photoUrl.isNullOrEmpty()) {
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(imgAvatar)
            } else {
                imgAvatar.setImageResource(R.drawable.ic_default_avatar)
            }

            // تعيين أيقونة الحالة وتلوينها
            imgStatus.setImageResource(statusIconRes)
            imgStatus.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(statusColor))

            // تمييز اسم المستخدم الحالي
            if (mId == currentUid) {
                txtName.setTextColor(Color.parseColor("#FFD700"))
            } else {
                txtName.setTextColor(Color.WHITE)
            }

            binding.layoutLoopChain.addView(itemView)

            // إذا لم يكن العضو الأخير، نضيف سهماً لليسار
            if (i < n - 1) {
                val arrowView = android.widget.ImageView(this).apply {
                    setImageResource(R.drawable.ic_loop_arrow)
                    layoutParams = LinearLayout.LayoutParams(
                        dpToPx(20),
                        dpToPx(20)
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        marginStart = dpToPx(6)
                        marginEnd = dpToPx(6)
                    }
                }
                binding.layoutLoopChain.addView(arrowView)
            }
        }
    }

    private fun showHalqaDetailsBottomSheet() {
        val halqaId = currentActiveHalqaId ?: return
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val sheetView = layoutInflater.inflate(R.layout.layout_halqa_details_bottom_sheet, null)
        dialog.setContentView(sheetView)

        val listContainer = sheetView.findViewById<LinearLayout>(R.id.list_members_container_sheet)
        val btnLeave = sheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_leave_halqa_sheet)

        btnLeave.setOnClickListener {
            dialog.dismiss()
            AlertDialog.Builder(this)
                .setTitle("تأكيد المغادرة")
                .setMessage("هل أنت متأكد من رغبتك في مغادرة هذه الحلقة؟ سيتم إعادة ترتيب مسؤوليات الاستيقاظ بين بقية الأعضاء تلقائياً.")
                .setPositiveButton("مغادرة") { _, _ ->
                    com.bagomri.fajrloop.data.HalqaManager.leaveHalqa { success, result ->
                        if (success) showToast("لقد غادرت الحلقة بنجاح 🚪")
                        else showToast("فشل مغادرة الحلقة: $result")
                    }
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        // قراءة البيانات من Firebase وتعبئة القائمة
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    lastMembersSnap?.let { mSnap ->
                        populateVerticalMembersSheetList(listContainer, lastChain, mSnap, snapshot, dialog)
                    }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })

        dialog.show()
    }

    private fun populateVerticalMembersSheetList(
        container: LinearLayout,
        chain: List<String>,
        membersSnap: com.google.firebase.database.DataSnapshot,
        recordsSnap: com.google.firebase.database.DataSnapshot,
        dialog: com.google.android.material.bottomsheet.BottomSheetDialog
    ) {
        container.removeAllViews()
        val n = chain.size
        val currentUid = AuthManager.getUserId() ?: ""

        for (i in 0 until n) {
            val mId = chain[i]
            val mSnap = membersSnap.child(mId)
            if (!mSnap.exists()) continue

            val displayName = mSnap.child("displayName").value as? String ?: "عضو"
            val role = mSnap.child("role").value as? String ?: "member"
            val responsibleForUserId = mSnap.child("responsibleForUserId").value as? String ?: ""

            val targetIndex = (i - 1 + n) % n
            val targetUid = chain[targetIndex]
            val targetName = membersSnap.child(targetUid).child("displayName").value as? String ?: "عضو"

            var status = "pending"
            val profileStatus = mSnap.child("status").value as? String
            if (profileStatus == "travel" || profileStatus == "traveling") {
                status = "travel"
            } else if (recordsSnap.child(mId).exists()) {
                status = recordsSnap.child(mId).child("status").value as? String ?: "pending"
            }

            val itemView = layoutInflater.inflate(R.layout.item_halqa_member, container, false)

            val textPosition = itemView.findViewById<android.widget.TextView>(R.id.text_member_position)
            val textName = itemView.findViewById<android.widget.TextView>(R.id.text_member_name)
            val textDesc = itemView.findViewById<android.widget.TextView>(R.id.text_member_desc)
            val badgeAdmin = itemView.findViewById<android.widget.TextView>(R.id.badge_admin)
            val layoutReorder = itemView.findViewById<View>(R.id.layout_reorder_buttons)
            val btnUp = itemView.findViewById<android.widget.ImageButton>(R.id.btn_move_up)
            val btnDown = itemView.findViewById<android.widget.ImageButton>(R.id.btn_move_down)

            val statusEmoji = when (status) {
                "travel" -> "✈️ مسافر"
                "challenge_done" -> "⏳ حل التحدي — بانتظار صديقه"
                "awake" -> "✅ مستيقظ ومؤكد"
                "panic" -> "🚨 نداء استغاثة"
                "ringing" -> "🔔 يرن المنبه"
                "missed" -> "❌ فاته الفجر"
                else -> "💤 نائم"
            }

            if (mId == currentUid) {
                textName.setTextColor(Color.parseColor("#FFD700"))
            } else {
                textName.setTextColor(Color.WHITE)
            }

            textPosition.text = (i + 1).toString()
            textName.text = displayName
            textDesc.text = "يوقظ: $targetName\n$statusEmoji"

            badgeAdmin.visibility = if (role == "admin") View.VISIBLE else View.GONE

            if (status == "challenge_done" && responsibleForUserId == currentUid) {
                val btnConfirm = com.google.android.material.button.MaterialButton(this).apply {
                    text = "تأكيد استيقاظ ${displayName.split(" ").first()} ✓"
                    textSize = 11f
                    setTextColor(Color.parseColor("#07071B"))
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700"))
                    cornerRadius = dpToPx(8)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(36)
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        marginStart = dpToPx(8)
                    }
                    setOnClickListener {
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        currentActiveHalqaId?.let { hId ->
                            com.google.firebase.database.FirebaseDatabase.getInstance()
                                .getReference("dailyRecords")
                                .child(hId).child(currentDate).child(mId).child("status")
                                .setValue("awake")
                                .addOnSuccessListener {
                                    showToast("تم تأكيد استيقاظ $displayName. كتب الله أجرك! 🟢")
                                    dialog.dismiss()
                                }
                        }
                    }
                }
                (layoutReorder.parent as? ViewGroup)?.addView(btnConfirm)
            } else if (status == "panic") {
                val btnCall = com.google.android.material.button.MaterialButton(this).apply {
                    text = "اتصل به الآن 📞"
                    textSize = 11f
                    setTextColor(Color.WHITE)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E74C3C"))
                    cornerRadius = dpToPx(8)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dpToPx(36)
                    ).apply {
                        gravity = Gravity.CENTER_VERTICAL
                        marginStart = dpToPx(8)
                    }
                    setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL)) }
                }
                (layoutReorder.parent as? ViewGroup)?.addView(btnCall)
            }

            if (isCurrentUserAdmin && n > 1) {
                layoutReorder.visibility = View.VISIBLE
                btnUp.setOnClickListener {
                    if (i > 0) {
                        val newChain = chain.toMutableList().also { it[i] = it[i-1].also { _ -> it[i-1] = it[i] } }
                        currentActiveHalqaId?.let { hId ->
                            com.bagomri.fajrloop.data.HalqaManager.reorderChain(hId, newChain) { success, error ->
                                if (success) {
                                    dialog.dismiss()
                                    showHalqaDetailsBottomSheet()
                                } else {
                                    showToast("فشل الترتيب: $error")
                                }
                            }
                        }
                    } else showToast("العضو في أعلى السلسلة بالفعل")
                }
                btnDown.setOnClickListener {
                    if (i < n - 1) {
                        val newChain = chain.toMutableList().also { it[i] = it[i+1].also { _ -> it[i+1] = it[i] } }
                        currentActiveHalqaId?.let { hId ->
                            com.bagomri.fajrloop.data.HalqaManager.reorderChain(hId, newChain) { success, error ->
                                if (success) {
                                    dialog.dismiss()
                                    showHalqaDetailsBottomSheet()
                                } else {
                                    showToast("فشل الترتيب: $error")
                                }
                            }
                        }
                    } else showToast("العضو في أسفل السلسلة بالفعل")
                }
            } else {
                layoutReorder.visibility = View.GONE
            }

            container.addView(itemView)
        }
    }

    // ==================== مساعدات ====================

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
