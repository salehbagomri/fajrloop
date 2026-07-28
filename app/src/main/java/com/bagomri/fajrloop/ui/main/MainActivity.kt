package com.bagomri.fajrloop.ui.main

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.app.NotificationManagerCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.auth.FcmTokenManager
import com.bagomri.fajrloop.data.AnalyticsHelper
import com.bagomri.fajrloop.data.HalqaManager
import com.bagomri.fajrloop.ui.BaseActivity
import com.bagomri.fajrloop.ui.auth.LoginActivity
import com.bagomri.fajrloop.ui.chat.ChatActivity
import com.bagomri.fajrloop.ui.main.components.HalqaMemberItem
import com.bagomri.fajrloop.ui.main.dialogs.CreateHalqaDialog
import com.bagomri.fajrloop.ui.main.dialogs.InviteCodeDialog
import com.bagomri.fajrloop.ui.main.dialogs.JoinHalqaDialog
import com.bagomri.fajrloop.ui.main.dialogs.LeaveHalqaDialog
import com.bagomri.fajrloop.ui.main.sheets.HalqaDetailsSheet
import com.bagomri.fajrloop.ui.onboarding.OnboardingActivity
import com.bagomri.fajrloop.ui.permissions.PermissionSetupActivity
import com.bagomri.fajrloop.ui.settings.SettingsActivity
import com.bagomri.fajrloop.ui.stats.StatsActivity
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val showCreateHalqaDialogState = MutableStateFlow(false)
    private val showJoinHalqaDialogState = MutableStateFlow(false)
    private val showInviteDialogState = MutableStateFlow(false)
    private val showLeaveHalqaDialogState = MutableStateFlow(false)
    private val showHalqaDetailsSheetState = MutableStateFlow(false)
    private val showRemoveMemberDialogState = MutableStateFlow<Pair<String, String>?>(null)

    private val inviteCodeState = MutableStateFlow("")
    private val halqaNameState = MutableStateFlow("حلقة الفجر")
    private val halqaMembersState = MutableStateFlow<List<HalqaMemberItem>>(emptyList())
    private val hasPermissionWarningState = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val onboardingPrefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val onboardingCompleted = onboardingPrefs.getBoolean("onboarding_completed", false)
        if (!onboardingCompleted) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        if (!AuthManager.isUserSignedIn()) {
            navigateToLogin()
            return
        }

        if (!hasAllCriticalPermissions()) {
            startActivity(Intent(this, PermissionSetupActivity::class.java))
            finish()
            return
        }

        FcmTokenManager.registerToken()

        setContent {
            FajrLoopTheme {
                val userProfile by viewModel.userProfileFlow.collectAsState()
                val halqaId by viewModel.halqaIdFlow.collectAsState()
                val isCurrentUserAdmin by viewModel.isCurrentUserAdminFlow.collectAsState()
                val friendWakeAlert by viewModel.friendWakeAlertFlow.collectAsState()
                val fajrTimeStr by viewModel.fajrTimeStrFlow.collectAsState()
                val sunriseTimeStr by viewModel.sunriseTimeStrFlow.collectAsState()
                val countdownText by viewModel.countdownTextFlow.collectAsState()
                val countdownColor by viewModel.countdownColorFlow.collectAsState()
                val countdownBorderMode by viewModel.countdownCardBorderModeFlow.collectAsState()

                val showCreateHalqa by showCreateHalqaDialogState.collectAsState()
                val showJoinHalqa by showJoinHalqaDialogState.collectAsState()
                val showInvite by showInviteDialogState.collectAsState()
                val showLeave by showLeaveHalqaDialogState.collectAsState()
                val showSheet by showHalqaDetailsSheetState.collectAsState()
                val removeTarget by showRemoveMemberDialogState.collectAsState()

                val inviteCode by inviteCodeState.collectAsState()
                val halqaName by halqaNameState.collectAsState()
                val membersList by halqaMembersState.collectAsState()
                val hasPermissionWarning by hasPermissionWarningState.collectAsState()

                HomeScreen(
                    userName = userProfile?.displayName ?: "",
                    userPhotoUrl = userProfile?.photoUrl ?: "",
                    isInHalqa = halqaId != null,
                    fajrTimeStr = fajrTimeStr,
                    sunriseTimeStr = sunriseTimeStr,
                    countdownText = countdownText,
                    countdownColorHex = countdownColor,
                    countdownBorderMode = countdownBorderMode,
                    friendWakeAlert = friendWakeAlert,
                    hasPermissionWarning = hasPermissionWarning,
                    onSettingsClick = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    onHalqaDetailsClick = { fetchAndShowHalqaDetails(halqaId) },
                    onChatClick = { startActivity(Intent(this, ChatActivity::class.java)) },
                    onStatsClick = { startActivity(Intent(this, StatsActivity::class.java)) },
                    onInviteClick = { fetchAndShowInviteDialog(halqaId) },
                    onCreateHalqaClick = { showCreateHalqaDialogState.value = true },
                    onJoinHalqaClick = { showJoinHalqaDialogState.value = true },
                    onConfirmFriendWake = { friendUid ->
                        viewModel.confirmFriendWake(friendUid) { success, error ->
                            if (success) {
                                showToast("تم إيقاف منبه صديقك بنجاح. كتب الله أجرك! 🟢")
                            } else {
                                showToast("فشل تأكيد الاستيقاظ: $error")
                            }
                        }
                    },
                    onFixPermissionsClick = { startActivity(Intent(this, PermissionSetupActivity::class.java)) }
                )

                if (showCreateHalqa) {
                    CreateHalqaDialog(
                        onDismiss = { showCreateHalqaDialogState.value = false },
                        onConfirm = { name ->
                            showCreateHalqaDialogState.value = false
                            HalqaManager.createHalqa(name) { success, result ->
                                if (success) {
                                    showToast("تم إنشاء الحلقة بنجاح! 🎉")
                                    AnalyticsHelper.logHalqaCreated()
                                } else {
                                    showToast("خطأ: $result")
                                }
                            }
                        }
                    )
                }

                if (showJoinHalqa) {
                    JoinHalqaDialog(
                        onDismiss = { showJoinHalqaDialogState.value = false },
                        onConfirm = { code ->
                            showJoinHalqaDialogState.value = false
                            HalqaManager.joinHalqa(code) { success, result ->
                                if (success) {
                                    showToast("تم الانضمام بنجاح! 🎉")
                                    AnalyticsHelper.logHalqaJoined()
                                } else {
                                    showToast("خطأ: $result")
                                }
                            }
                        }
                    )
                }

                if (showInvite) {
                    InviteCodeDialog(
                        halqaName = halqaName,
                        inviteCode = inviteCode,
                        onCopy = {
                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("كود دعوة حلقة الفجر", inviteCode)
                            clipboard.setPrimaryClip(clip)
                            showToast("تم نسخ كود الدعوة بنجاح! 📋")
                        },
                        onShare = {
                            val shareText = "🌙 انضم لحلقة الفجر معي!\nاسم الحلقة: $halqaName\nكود الدعوة: $inviteCode\n\nحمّل تطبيق FajrLoop واستيقظ للفجر معنا!"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            startActivity(Intent.createChooser(shareIntent, "مشاركة كود الدعوة"))
                        },
                        onDismiss = { showInviteDialogState.value = false }
                    )
                }

                if (showSheet) {
                    HalqaDetailsSheet(
                        halqaName = halqaName,
                        members = membersList,
                        isAdmin = isCurrentUserAdmin,
                        onDismiss = { showHalqaDetailsSheetState.value = false },
                        onLeaveClick = {
                            showHalqaDetailsSheetState.value = false
                            showLeaveHalqaDialogState.value = true
                        },
                        onConfirmWake = { memberUid ->
                            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                            halqaId?.let { hId ->
                                FirebaseDatabase.getInstance()
                                    .getReference("dailyRecords")
                                    .child(hId).child(currentDate).child(memberUid).child("status")
                                    .setValue("awake")
                                    .addOnSuccessListener {
                                        showToast("تم تأكيد الاستيقاظ بنجاح! 🟢")
                                        fetchAndShowHalqaDetails(hId)
                                    }
                            }
                        },
                        onCallClick = { startActivity(Intent(Intent.ACTION_DIAL)) },
                        onMoveUp = { memberUid ->
                            val chain = membersList.map { it.uid }
                            val i = chain.indexOf(memberUid)
                            if (i > 0) {
                                val newChain = chain.toMutableList().also { it[i] = it[i-1].also { _ -> it[i-1] = it[i] } }
                                halqaId?.let { hId ->
                                    HalqaManager.reorderChain(hId, newChain) { success, error ->
                                        if (success) fetchAndShowHalqaDetails(hId)
                                        else showToast("فشل الترتيب: $error")
                                    }
                                }
                            }
                        },
                        onMoveDown = { memberUid ->
                            val chain = membersList.map { it.uid }
                            val i = chain.indexOf(memberUid)
                            if (i >= 0 && i < chain.size - 1) {
                                val newChain = chain.toMutableList().also { it[i] = it[i+1].also { _ -> it[i+1] = it[i] } }
                                halqaId?.let { hId ->
                                    HalqaManager.reorderChain(hId, newChain) { success, error ->
                                        if (success) fetchAndShowHalqaDetails(hId)
                                        else showToast("فشل الترتيب: $error")
                                    }
                                }
                            }
                        },
                        onRemoveMember = { memberUid, memberName ->
                            showRemoveMemberDialogState.value = Pair(memberUid, memberName)
                        }
                    )
                }

                if (showLeave) {
                    LeaveHalqaDialog(
                        onDismiss = { showLeaveHalqaDialogState.value = false },
                        onConfirm = {
                            showLeaveHalqaDialogState.value = false
                            HalqaManager.leaveHalqa { success, result ->
                                if (success) showToast("لقد غادرت الحلقة بنجاح 🚪")
                                else showToast("فشل مغادرة الحلقة: $result")
                            }
                        }
                    )
                }

                if (removeTarget != null) {
                    LeaveHalqaDialog(
                        title = "تأكيد حذف العضو",
                        description = "هل أنت متأكد من رغبتك في حذف العضو (${removeTarget!!.second}) من الحلقة؟",
                        confirmText = "حذف العضو 🗑️",
                        onDismiss = { showRemoveMemberDialogState.value = null },
                        onConfirm = {
                            val targetId = removeTarget!!.first
                            val targetName = removeTarget!!.second
                            showRemoveMemberDialogState.value = null
                            halqaId?.let { hId ->
                                HalqaManager.removeMemberFromHalqa(hId, targetId) { success, result ->
                                    if (success) {
                                        showToast("تم حذف العضو ($targetName) من الحلقة بنجاح 🗑️")
                                        fetchAndShowHalqaDetails(hId)
                                    } else {
                                        showToast("فشل حذف العضو: $result")
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (AuthManager.isUserSignedIn() && !hasAllCriticalPermissions()) {
            startActivity(Intent(this, PermissionSetupActivity::class.java))
            finish()
            return
        }
        checkPermissionsAndUpdateWarning()
        viewModel.startFajrCountdown()
    }

    private fun checkPermissionsAndUpdateWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            hasPermissionWarningState.value = !alarmManager.canScheduleExactAlarms()
        } else {
            hasPermissionWarningState.value = false
        }
    }

    private fun fetchAndShowInviteDialog(halqaId: String?) {
        if (halqaId.isNullOrEmpty()) {
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

                    halqaNameState.value = name
                    inviteCodeState.value = code
                    showInviteDialogState.value = true
                }
                override fun onCancelled(error: DatabaseError) {
                    showToast("فشل في تحميل بيانات الحلقة")
                }
            })
    }

    private fun fetchAndShowHalqaDetails(halqaId: String?) {
        if (halqaId.isNullOrEmpty()) return

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val currentUid = AuthManager.getUserId() ?: ""

        FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(recordsSnap: DataSnapshot) {
                    FirebaseDatabase.getInstance().getReference("halqas").child(halqaId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(halqaSnap: DataSnapshot) {
                                val name = halqaSnap.child("name").value as? String ?: "الفجر"
                                halqaNameState.value = name

                                val chain = (halqaSnap.child("chain").value as? List<*>)
                                    ?.filterIsInstance<String>() ?: emptyList()
                                val membersSnap = halqaSnap.child("members")

                                val list = mutableListOf<HalqaMemberItem>()
                                val n = chain.size
                                for (i in 0 until n) {
                                    val mId = chain[i]
                                    val mSnap = membersSnap.child(mId)
                                    if (!mSnap.exists()) continue

                                    val displayName = mSnap.child("displayName").value as? String ?: "عضو"
                                    val photo = mSnap.child("photoUrl").value as? String ?: ""
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

                                    list.add(
                                        HalqaMemberItem(
                                            uid = mId,
                                            displayName = displayName,
                                            photoUrl = photo,
                                            role = role,
                                            responsibleForUserId = responsibleForUserId,
                                            targetName = targetName,
                                            status = status,
                                            isCurrentUser = mId == currentUid,
                                            position = i + 1
                                        )
                                    )
                                }

                                halqaMembersState.value = list
                                showHalqaDetailsSheetState.value = true
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun hasAllCriticalPermissions(): Boolean {
        val notifGranted = NotificationManagerCompat.from(this).areNotificationsEnabled()
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else true
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val batteryGranted = pm.isIgnoringBatteryOptimizations(packageName)
        val fullScreenGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.canUseFullScreenIntent()
        } else true
        val overlaysGranted = android.provider.Settings.canDrawOverlays(this)

        return notifGranted && exactAlarmGranted && batteryGranted && fullScreenGranted && overlaysGranted
    }
}
