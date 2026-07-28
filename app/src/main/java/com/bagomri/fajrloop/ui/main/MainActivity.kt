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
        if (AuthManager.isUserSignedIn() && !hasAllCriticalPermissions()) {
            startActivity(Intent(this, PermissionSetupActivity::class.java))
            finish()
            return
        }
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
                binding.layoutQuickActionsInHalqa.visibility = View.GONE
                binding.layoutQuickActionsNoHalqa.visibility = View.VISIBLE
                binding.cardFriendWakeAlert.visibility = View.GONE
            } else {
                binding.layoutQuickActionsInHalqa.visibility = View.VISIBLE
                binding.layoutQuickActionsNoHalqa.visibility = View.GONE
            }
        }

        viewModel.isCurrentUserAdmin.observe(this) { isAdmin ->
            isCurrentUserAdmin = isAdmin
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
            showInviteDialog()
        }

        binding.cardActionCreateHalqa.setOnClickListener { showCreateHalqaDialog() }
        binding.cardActionJoinHalqa.setOnClickListener { showJoinHalqaDialog() }
        binding.cardActionStatsAlone.setOnClickListener {
            startActivity(Intent(this, com.bagomri.fajrloop.ui.stats.StatsActivity::class.java))
        }
    }

    private fun showInviteDialog() {
        val halqaId = currentActiveHalqaId ?: run {
            showToast("يرجى الانضمام لحلقة أولاً لعرض كود الدعوة")
            return
        }
        FirebaseDatabase.getInstance()
            .getReference("halqas").child(halqaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isFinishing || isDestroyed) return
                    val name = snapshot.child("name").value as? String ?: "حلقة الفجر"
                    val code = snapshot.child("inviteCode").value as? String ?: ""

                    if (code.isEmpty()) {
                        showToast("لم يتوفر كود الدعوة للحلقة")
                        return
                    }

                    val dialog = android.app.Dialog(this@MainActivity)
                    dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.dialog_invite_code)
                    dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))

                    val txtHalqaName = dialog.findViewById<TextView>(R.id.txt_halqa_name)
                    val txtInviteCode = dialog.findViewById<TextView>(R.id.txt_invite_code)
                    val btnQuickCopy = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_quick_copy)
                    val btnClose = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_close)
                    val btnShare = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_share_invite)

                    txtHalqaName?.text = "حلقة: $name"
                    txtInviteCode?.text = code

                    val copyAction = {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("كود دعوة حلقة الفجر", code)
                        clipboard.setPrimaryClip(clip)
                        showToast("تم نسخ كود الدعوة بنجاح! 📋")
                    }

                    btnQuickCopy?.setOnClickListener { copyAction() }
                    txtInviteCode?.setOnClickListener { copyAction() }

                    btnShare?.setOnClickListener {
                        val shareText = "🌙 انضم لحلقة الفجر معي!\nاسم الحلقة: $name\nكود الدعوة: $code\n\nحمّل تطبيق FajrLoop واستيقظ للفجر معنا!"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        startActivity(Intent.createChooser(shareIntent, "مشاركة كود الدعوة"))
                    }

                    btnClose?.setOnClickListener { dialog.dismiss() }

                    dialog.show()
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("فشل في تحميل بيانات الحلقة")
                }
            })
    }

    private fun showCreateHalqaDialog() {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_create_halqa)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val inputName = dialog.findViewById<android.widget.EditText>(R.id.input_halqa_name)
        val btnCancel = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)
        val btnConfirm = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val name = inputName.text.toString().trim()
            if (name.length < 3) {
                showToast("يجب أن يكون اسم الحلقة 3 أحرف على الأقل")
                return@setOnClickListener
            }
            dialog.dismiss()
            com.bagomri.fajrloop.data.HalqaManager.createHalqa(name) { success, result ->
                if (success) {
                    showToast("تم إنشاء الحلقة بنجاح! 🎉")
                    com.bagomri.fajrloop.data.AnalyticsHelper.logHalqaCreated()
                } else {
                    showToast("خطأ: $result")
                }
            }
        }

        dialog.show()
    }

    private fun showJoinHalqaDialog() {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_join_halqa)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val inputCode = dialog.findViewById<android.widget.EditText>(R.id.input_invite_code)
        val btnCancel = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)
        val btnConfirm = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val code = inputCode.text.toString().trim()
            if (code.isEmpty()) {
                showToast("يرجى إدخال كود الدعوة")
                return@setOnClickListener
            }
            dialog.dismiss()
            com.bagomri.fajrloop.data.HalqaManager.joinHalqa(code) { success, result ->
                if (success) {
                    showToast("تم الانضمام بنجاح! 🎉")
                    com.bagomri.fajrloop.data.AnalyticsHelper.logHalqaJoined()
                } else {
                    showToast("خطأ: $result")
                }
            }
        }

        dialog.show()
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
        val txtTitle = sheetView.findViewById<TextView>(R.id.text_sheet_title)
        val btnLeave = sheetView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_leave_halqa_sheet)

        btnLeave.setOnClickListener {
            dialog.dismiss()
            val leaveDialog = android.app.Dialog(this)
            leaveDialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
            leaveDialog.setContentView(R.layout.dialog_leave_halqa)
            leaveDialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            leaveDialog.window?.setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val btnCancelLeave = leaveDialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)
            val btnConfirmLeave = leaveDialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm)

            btnCancelLeave.setOnClickListener {
                leaveDialog.dismiss()
            }

            btnConfirmLeave.setOnClickListener {
                leaveDialog.dismiss()
                com.bagomri.fajrloop.data.HalqaManager.leaveHalqa { success, result ->
                    if (success) showToast("لقد غادرت الحلقة بنجاح 🚪")
                    else showToast("فشل مغادرة الحلقة: $result")
                }
            }

            leaveDialog.show()
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
                                val name = halqaSnap.child("name").value as? String ?: "الفجر"
                                txtTitle?.text = "تفاصيل حلقة \"$name\" 👥"

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
            val btnRemove = itemView.findViewById<android.widget.ImageButton>(R.id.btn_remove_member)

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

                if (mId != currentUid) {
                    btnRemove.visibility = View.VISIBLE
                    btnRemove.setOnClickListener {
                        val confirmDialog = android.app.Dialog(this)
                        confirmDialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
                        confirmDialog.setContentView(R.layout.dialog_leave_halqa)
                        confirmDialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                        confirmDialog.window?.setLayout(
                            (resources.displayMetrics.widthPixels * 0.9).toInt(),
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        val txtTitleDialog = confirmDialog.findViewById<TextView>(R.id.txt_leave_title)
                        val txtMessage = confirmDialog.findViewById<TextView>(R.id.text_leave_desc)
                        val btnCancel = confirmDialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_cancel)
                        val btnConfirm = confirmDialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_confirm)

                        txtTitleDialog?.text = "تأكيد حذف العضو"
                        txtMessage?.text = "هل أنت متأكد من رغبتك في حذف العضو ($displayName) من الحلقة؟"
                        btnConfirm?.text = "حذف العضو 🗑️"

                        btnCancel?.setOnClickListener { confirmDialog.dismiss() }
                        btnConfirm?.setOnClickListener {
                            confirmDialog.dismiss()
                            currentActiveHalqaId?.let { hId ->
                                com.bagomri.fajrloop.data.HalqaManager.removeMemberFromHalqa(hId, mId) { success, result ->
                                    if (success) {
                                        showToast("تم حذف العضو ($displayName) من الحلقة بنجاح 🗑️")
                                        dialog.dismiss()
                                        showHalqaDetailsBottomSheet()
                                    } else {
                                        showToast("فشل حذف العضو: $result")
                                    }
                                }
                            }
                        }
                        confirmDialog.show()
                    }
                } else {
                    btnRemove.visibility = View.GONE
                }

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

        // 5. الظهور فوق التطبيقات الأخرى (System Alert Window)
        val overlaysGranted = android.provider.Settings.canDrawOverlays(this)

        return notifGranted && exactAlarmGranted && batteryGranted && fullScreenGranted && overlaysGranted
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
