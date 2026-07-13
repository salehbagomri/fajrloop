package com.bagomri.fajrloop.ui.main

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bagomri.fajrloop.ui.BaseActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.databinding.ActivityMainBinding
import android.app.NotificationManager
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import com.bagomri.fajrloop.ui.auth.LoginActivity
import com.bagomri.fajrloop.ui.permissions.PermissionSetupActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * MainActivity — الشاشة الرئيسية لحلقة الفجر (MVVM Refactored)
 */
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var currentActiveHalqaId: String? = null
    private var isCurrentUserAdmin: Boolean = false

    // تبويب المحتوى الروحي (0=آية, 1=حديث)
    private var currentSpiritualTab = 0

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
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val onboardingPrefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val onboardingCompleted = onboardingPrefs.getBoolean("onboarding_completed", false)
        if (!onboardingCompleted) {
            startActivity(Intent(this, com.bagomri.fajrloop.ui.onboarding.OnboardingActivity::class.java))
            finish()
            return
        }

        if (!AuthManager.isUserSignedIn()) {
            navigateToLogin()
            return
        }

        // تحقق من الصلاحيات الحرجة قبل فتح الشاشة الرئيسية
        if (!hasAllCriticalPermissions()) {
            startActivity(Intent(this, PermissionSetupActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // تخصيص لون خلفية وحدود كرت إحصائيات المستيقظين باللون الأخضر الشفاف وتقليل زواياه لـ 12dp
        binding.cardAwakeBadge.setCornerRadiusDp(12f)
        binding.cardAwakeBadge.setCustomBgAndBorder(
            Color.argb(30, 0x2E, 0xCC, 0x71),
            Color.argb(102, 0x2E, 0xCC, 0x71)
        )

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // تسجيل رمز الـ FCM للحصول على إشعارات الاستغاثة والدردشة
        com.bagomri.fajrloop.auth.FcmTokenManager.registerToken()

        setupObservers()
        checkAndRequestPermissions()
        setupSpiritualContent()
        setupQuickActionsUI()
    }

    override fun onResume() {
        super.onResume()
        checkAndRequestPermissions()
        viewModel.startFajrCountdown()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { profile ->
            if (profile != null) {
                binding.textUserName.text = profile.displayName
                if (profile.photoUrl.isNotEmpty()) {
                    Glide.with(this)
                        .load(profile.photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_default_avatar)
                        .into(binding.imageUserAvatar)
                } else {
                    binding.imageUserAvatar.setImageResource(R.drawable.ic_default_avatar)
                }
            }
        }

        viewModel.halqaId.observe(this) { hId ->
            currentActiveHalqaId = hId
            if (hId == null) {
                binding.layoutHasHalqa.visibility = View.GONE
                binding.cardNoHalqa.visibility = View.VISIBLE
                binding.layoutQuickActionsInHalqa.visibility = View.GONE
                binding.layoutQuickActionsNoHalqa.visibility = View.VISIBLE
                binding.cardTodaySummary.visibility = View.GONE
                binding.cardFriendWakeAlert.visibility = View.GONE
            } else {
                binding.layoutHasHalqa.visibility = View.VISIBLE
                binding.cardNoHalqa.visibility = View.GONE
                binding.layoutQuickActionsInHalqa.visibility = View.VISIBLE
                binding.layoutQuickActionsNoHalqa.visibility = View.GONE
                binding.cardTodaySummary.visibility = View.VISIBLE
            }
        }

        viewModel.halqaName.observe(this) { name ->
            binding.textHalqaName.text = "حلقة: $name"
        }

        viewModel.isCurrentUserAdmin.observe(this) { isAdmin ->
            isCurrentUserAdmin = isAdmin
            binding.btnReorderChain.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }

        viewModel.loopMembers.observe(this) { members ->
            populateHorizontalLoopChain(members)
        }

        viewModel.todaySummaryText.observe(this) { summary ->
            binding.textTodaySummary.text = summary
        }

        viewModel.awakeCountText.observe(this) { count ->
            binding.textAwakeCount.text = count
        }

        viewModel.friendWakeAlert.observe(this) { alert ->
            if (alert != null) {
                binding.cardFriendWakeAlert.visibility = View.VISIBLE
                binding.textFriendWakeMessage.text = alert.message
                binding.btnConfirmFriendWake.text = "تأكيد استيقاظ ${alert.displayName} وإيقاف منبهه"
                binding.btnConfirmFriendWake.setOnClickListener {
                    viewModel.confirmFriendWake(alert.uid) { success, error ->
                        if (success) {
                            showToast("تم إيقاف منبه صديقك بنجاح. كتب الله أجرك! 🟢")
                        } else {
                            showToast("فشل تأكيد الاستيقاظ: $error")
                        }
                    }
                }
            } else {
                binding.cardFriendWakeAlert.visibility = View.GONE
            }
        }

        viewModel.fajrTimeStr.observe(this) { time ->
            binding.textFajrTime.text = time
        }

        viewModel.sunriseTimeStr.observe(this) { time ->
            binding.textSunriseTime.text = time
        }

        viewModel.countdownText.observe(this) { text ->
            binding.textFajrCountdown.text = text
        }

        viewModel.countdownColor.observe(this) { color ->
            binding.textFajrCountdown.setTextColor(Color.parseColor(color))
        }

        viewModel.countdownCardBorderMode.observe(this) { mode ->
            val glassCard = binding.cardFajrCountdown as? com.bagomri.fajrloop.ui.widget.GlassCardView
            when (mode) {
                3 -> {
                    glassCard?.setBorderColor(Color.argb(128, 0xE7, 0x4C, 0x3C))
                    glassCard?.startPulse()
                }
                2 -> {
                    glassCard?.setBorderColor(Color.argb(128, 0xE7, 0x4C, 0x3C))
                    glassCard?.stopPulse()
                }
                1 -> {
                    glassCard?.setBorderColor(Color.argb(100, 0xFF, 0xD7, 0x00))
                    glassCard?.stopPulse()
                }
                else -> {
                    glassCard?.resetBorderColor()
                    glassCard?.stopPulse()
                }
            }
        }
    }

    private fun populateHorizontalLoopChain(members: List<LoopMemberItem>) {
        binding.layoutLoopChain.removeAllViews()
        val n = members.size
        val currentUid = AuthManager.getUserId() ?: ""

        for (i in 0 until n) {
            val item = members[i]
            val statusColor: String
            val statusText: String
            val statusIconRes: Int

            when (item.status) {
                "travel" -> {
                    statusColor = "#3498DB"
                    statusText = "مسافر ✈️"
                    statusIconRes = R.drawable.ic_travel
                }
                "awake" -> {
                    statusColor = "#2ECC71"
                    statusText = "مستيقظ"
                    statusIconRes = R.drawable.ic_circle_check
                }
                "challenge_done" -> {
                    statusColor = "#FFD700"
                    statusText = "حل التحدي"
                    statusIconRes = R.drawable.ic_circle_warning
                }
                "panic" -> {
                    statusColor = "#E74C3C"
                    statusText = "استغاثة"
                    statusIconRes = R.drawable.ic_circle_warning
                }
                "ringing" -> {
                    statusColor = "#B57CFF"
                    statusText = "يرن المنبه"
                    statusIconRes = R.drawable.ic_alarm_notification
                }
                "missed" -> {
                    statusColor = "#6B6B8A"
                    statusText = "فاته الفجر"
                    statusIconRes = R.drawable.ic_alarm_off
                }
                else -> {
                    statusColor = "#B0B0C5"
                    statusText = "نائم"
                    statusIconRes = R.drawable.ic_circle_warning
                }
            }

            val itemView = layoutInflater.inflate(R.layout.item_loop_member, binding.layoutLoopChain, false)
            val imgAvatar = itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.image_loop_avatar)
            val imgStatus = itemView.findViewById<android.widget.ImageView>(R.id.image_loop_status_icon)
            val txtName = itemView.findViewById<android.widget.TextView>(R.id.text_loop_member_name)
            val txtStatus = itemView.findViewById<android.widget.TextView>(R.id.text_loop_member_status)

            txtName.text = item.displayName.split(" ").first()
            txtStatus.text = statusText
            txtStatus.setTextColor(Color.parseColor(statusColor))
            imgAvatar.borderColor = Color.parseColor(statusColor)

            if (item.photoUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(item.photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(imgAvatar)
            } else {
                imgAvatar.setImageResource(R.drawable.ic_default_avatar)
            }

            imgStatus.setImageResource(statusIconRes)
            imgStatus.imageTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(statusColor))

            if (item.userId == currentUid) {
                txtName.setTextColor(Color.parseColor("#FFD700"))
            } else {
                txtName.setTextColor(Color.WHITE)
            }

            binding.layoutLoopChain.addView(itemView)

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
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, com.bagomri.fajrloop.ui.settings.SettingsActivity::class.java))
        }
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

        binding.btnCreateHalqa.setOnClickListener { showCreateHalqaDialog() }
        binding.btnJoinHalqa.setOnClickListener { showJoinHalqaDialog() }

        binding.cardActionCreateHalqa.setOnClickListener { showCreateHalqaDialog() }
        binding.cardActionJoinHalqa.setOnClickListener { showJoinHalqaDialog() }
        binding.cardActionStatsAlone.setOnClickListener {
            startActivity(Intent(this, com.bagomri.fajrloop.ui.stats.StatsActivity::class.java))
        }
        binding.btnReorderChain.setOnClickListener {
            showHalqaDetailsBottomSheet()
        }
    }

    private fun shareInviteCode() {
        val halqaId = currentActiveHalqaId ?: run {
            showToast("يرجى الانضمام لحلقة أولاً")
            return
        }
        FirebaseDatabase.getInstance()
            .getReference("halqas").child(halqaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value as? String ?: "حلقة الفجر"
                    val code = snapshot.child("inviteCode").value as? String ?: ""
                    val shareText = "🌙 انضم لحلقة الفجر!\nاسم الحلقة: $name\nكود الدعوة: $code\nحمّل التطبيق لتستيقظ للفجر معي!"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    startActivity(Intent.createChooser(shareIntent, "مشاركة الكود"))
                }
                override fun onCancelled(error: DatabaseError) {}
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
                    if (success) {
                        showToast("تم إنشاء الحلقة بنجاح! 🎉")
                        com.bagomri.fajrloop.data.AnalyticsHelper.logHalqaCreated()
                    } else {
                        showToast("خطأ: $result")
                    }
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
                    if (success) {
                        showToast("تم الانضمام بنجاح! 🎉")
                        com.bagomri.fajrloop.data.AnalyticsHelper.logHalqaJoined()
                    } else {
                        showToast("خطأ: $result")
                    }
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

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

    private fun showHalqaDetailsBottomSheet() {
        val halqaId = currentActiveHalqaId ?: return
        val dialog = BottomSheetDialog(this)
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

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    FirebaseDatabase.getInstance().getReference("halqas").child(halqaId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(halqaSnap: DataSnapshot) {
                                val chain = (halqaSnap.child("chain").value as? List<*>)
                                    ?.filterIsInstance<String>() ?: emptyList()
                                val membersSnap = halqaSnap.child("members")
                                populateVerticalMembersSheetList(listContainer, chain, membersSnap, snapshot, dialog)
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        dialog.show()
    }

    private fun populateVerticalMembersSheetList(
        container: LinearLayout,
        chain: List<String>,
        membersSnap: DataSnapshot,
        recordsSnap: DataSnapshot,
        dialog: BottomSheetDialog
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

            val textPosition = itemView.findViewById<TextView>(R.id.text_member_position)
            val textName = itemView.findViewById<TextView>(R.id.text_member_name)
            val textDesc = itemView.findViewById<TextView>(R.id.text_member_desc)
            val badgeAdmin = itemView.findViewById<TextView>(R.id.badge_admin)
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
                            FirebaseDatabase.getInstance()
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun hasAllCriticalPermissions(): Boolean {
        // 1. الإشعارات (Android 13+)
        val notifGranted = NotificationManagerCompat.from(this).areNotificationsEnabled()

        // 2. المنبه الدقيق (Android 12+)
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else true

        // 3. تجاهل تحسين البطارية
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val batteryGranted = pm.isIgnoringBatteryOptimizations(packageName)

        // 4. الظهور فوق قفل الشاشة Full Screen Intent (Android 14+)
        val fullScreenGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.canUseFullScreenIntent()
        } else true

        return notifGranted && exactAlarmGranted && batteryGranted && fullScreenGranted
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
