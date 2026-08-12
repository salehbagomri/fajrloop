package com.bagomri.fajrloop.ui.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import java.util.Locale
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.data.*
import com.bagomri.fajrloop.ui.adhkar.AdhkarScreen
import com.bagomri.fajrloop.ui.auth.LoginScreen
import com.bagomri.fajrloop.ui.auth.LoginViewModel
import com.bagomri.fajrloop.ui.chat.ChatScreen
import com.bagomri.fajrloop.ui.chat.ChatViewModel
import com.bagomri.fajrloop.ui.main.HomeScreen
import com.bagomri.fajrloop.ui.main.MainViewModel
import com.bagomri.fajrloop.ui.onboarding.OnboardingScreen
import com.bagomri.fajrloop.ui.main.components.HalqaMemberItem
import com.bagomri.fajrloop.ui.main.dialogs.*
import com.bagomri.fajrloop.ui.main.sheets.HalqaDetailsSheet
import com.bagomri.fajrloop.ui.permissions.PermissionItemData
import com.bagomri.fajrloop.ui.permissions.PermissionScreen
import com.bagomri.fajrloop.ui.settings.*
import com.bagomri.fajrloop.ui.stats.StatsScreen
import com.bagomri.fajrloop.ui.stats.StatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.absoluteValue

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object PermissionSetup : Screen("permission_setup")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object TravelMode : Screen("travel_mode")
    object Guide : Screen("guide")
    object BackupCode : Screen("backup_code")
    object Stats : Screen("stats")
    object Chat : Screen("chat")
    object MorningAdhkar : Screen("morning_adhkar")
}

@Composable
fun FajrLoopNavGraph(
    navController: NavHostController,
    startDestination: String,
    onFallbackLegacyLogin: (Intent) -> Unit,
    permissionsList: List<PermissionItemData>,
    allPermissionsGranted: Boolean,
    onRefreshPermissions: () -> Unit,
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    statsViewModel: StatsViewModel,
    loginViewModel: LoginViewModel,
    chatViewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(AlarmPreferences.KEY_ONBOARDING_COMPLETED, true).apply()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val isLoading by loginViewModel.isLoadingFlow.collectAsState()
            val loginSuccess by loginViewModel.loginSuccessFlow.collectAsState()
            val errorMessage by loginViewModel.errorMessageFlow.collectAsState()

            LaunchedEffect(loginSuccess) {
                if (loginSuccess) {
                    mainViewModel.refreshUserData()
                    loginViewModel.resetLoginState()
                    val targetRoute = if (allPermissionsGranted) Screen.Home.route else Screen.PermissionSetup.route
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            LaunchedEffect(errorMessage) {
                errorMessage?.let { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }

            LoginScreen(
                onGoogleSignInClick = {
                    loginViewModel.startGoogleSignInFlow(context, onFallbackLegacyLogin)
                },
                isLoading = isLoading
            )
        }

        composable(Screen.PermissionSetup.route) {
            LaunchedEffect(Unit) {
                onRefreshPermissions()
            }

            PermissionScreen(
                permissions = permissionsList,
                allGranted = allPermissionsGranted,
                onDoneClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.PermissionSetup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val userProfile by mainViewModel.userProfileFlow.collectAsState()
            val halqaId by mainViewModel.halqaIdFlow.collectAsState()
            val halqaName by mainViewModel.halqaNameFlow.collectAsState()
            val loopMembers by mainViewModel.loopMembersFlow.collectAsState()
            val isAdmin by mainViewModel.isCurrentUserAdminFlow.collectAsState()
            val friendWakeAlert by mainViewModel.friendWakeAlertFlow.collectAsState()
            val fajrTimeStr by mainViewModel.fajrTimeStrFlow.collectAsState()
            val sunriseTimeStr by mainViewModel.sunriseTimeStrFlow.collectAsState()
            val countdownText by mainViewModel.countdownTextFlow.collectAsState()
            val countdownColor by mainViewModel.countdownColorFlow.collectAsState()
            val countdownBorderMode by mainViewModel.countdownCardBorderModeFlow.collectAsState()

            var showHalqaDetailsSheet by remember { mutableStateOf(false) }
            var showCreateHalqaDialog by remember { mutableStateOf(false) }
            var showJoinHalqaDialog by remember { mutableStateOf(false) }
            var showInviteCodeDialog by remember { mutableStateOf(false) }
            var showLeaveHalqaDialog by remember { mutableStateOf(false) }

            HomeScreen(
                userName = userProfile?.displayName ?: "",
                userPhotoUrl = userProfile?.photoUrl ?: "",
                isInHalqa = !halqaId.isNullOrEmpty(),
                fajrTimeStr = fajrTimeStr,
                sunriseTimeStr = sunriseTimeStr,
                countdownText = countdownText,
                countdownColorHex = countdownColor,
                countdownBorderMode = countdownBorderMode,
                friendWakeAlert = friendWakeAlert,
                hasPermissionWarning = !allPermissionsGranted,
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onHalqaDetailsClick = {
                    if (!halqaId.isNullOrEmpty()) {
                        showHalqaDetailsSheet = true
                    } else {
                        Toast.makeText(context, "⚠️ لست في حلقة حالياً", Toast.LENGTH_SHORT).show()
                    }
                },
                onChatClick = {
                    if (!halqaId.isNullOrEmpty()) {
                        navController.navigate(Screen.Chat.route)
                    } else {
                        Toast.makeText(context, "⚠️ يجب الانضمام لحلقة أولاً للدردشة", Toast.LENGTH_SHORT).show()
                    }
                },
                onStatsClick = { navController.navigate(Screen.Stats.route) },
                onInviteClick = {
                    if (!halqaId.isNullOrEmpty()) {
                        showInviteCodeDialog = true
                    } else {
                        Toast.makeText(context, "⚠️ قم بإنشاء حلقة أولاً للحصول على كود الدعوة", Toast.LENGTH_SHORT).show()
                    }
                },
                onCreateHalqaClick = { showCreateHalqaDialog = true },
                onJoinHalqaClick = { showJoinHalqaDialog = true },
                onConfirmFriendWake = { friendUid ->
                    mainViewModel.confirmFriendWake(friendUid) { success, error ->
                        if (success) {
                            Toast.makeText(context, "تم إيقاف منبه صديقك بنجاح. كتب الله أجرك! 🟢", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "فشل تأكيد الاستيقاظ: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFixPermissionsClick = { navController.navigate(Screen.PermissionSetup.route) }
            )

            // Bottom Sheets & Dialogs
            if (showHalqaDetailsSheet) {
                val sheetMembers = loopMembers.mapIndexed { idx, m ->
                    HalqaMemberItem(
                        uid = m.userId,
                        displayName = m.displayName,
                        photoUrl = m.photoUrl,
                        role = m.role,
                        responsibleForUserId = m.responsibleForUserId,
                        targetName = m.targetName,
                        status = m.status,
                        isCurrentUser = m.isCurrentUser,
                        position = idx + 1
                    )
                }
                HalqaDetailsSheet(
                    halqaName = halqaName,
                    members = sheetMembers,
                    isAdmin = isAdmin,
                    onDismiss = { showHalqaDetailsSheet = false },
                    onLeaveClick = {
                        showHalqaDetailsSheet = false
                        showLeaveHalqaDialog = true
                    },
                    onTestAlarmClick = {
                        showHalqaDetailsSheet = false
                        mainViewModel.triggerTestLoopAlarm { success, err ->
                            if (success) {
                                Toast.makeText(context, "🧪 تم إرسال إشارة الاختبار! سيرن منبه جميع أعضاء الحلقة بعد 60 ثانية ⏰", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "فشل بدء الاختبار: $err", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onConfirmWake = { friendUid ->
                        mainViewModel.confirmFriendWake(friendUid) { success, error ->
                            if (success) {
                                Toast.makeText(context, "تم تأكيد الاستيقاظ!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "خطأ: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onCallClick = {},
                    onMoveUp = { fromIndex ->
                        if (fromIndex > 0) {
                            mainViewModel.reorderMember(fromIndex, fromIndex - 1) { success, err ->
                                if (!success && err != null) {
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onMoveDown = { fromIndex ->
                        if (fromIndex < sheetMembers.size - 1) {
                            mainViewModel.reorderMember(fromIndex, fromIndex + 1) { success, err ->
                                if (!success && err != null) {
                                    Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onRemoveMember = { targetUid, name ->
                        mainViewModel.removeMemberFromHalqa(targetUid) { success, err ->
                            if (success) {
                                Toast.makeText(context, "تم حذف $name من الحلقة", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            if (showCreateHalqaDialog) {
                CreateHalqaDialog(
                    onDismiss = { showCreateHalqaDialog = false },
                    onConfirm = { name ->
                        showCreateHalqaDialog = false
                        Toast.makeText(context, "جاري إنشاء حلقة «$name»...", Toast.LENGTH_SHORT).show()
                        mainViewModel.createHalqa(name) { success, result ->
                            if (success) {
                                Toast.makeText(context, "تم إنشاء حلقة «$name» بنجاح! 🥳", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "فشل إنشاء الحلقة: $result", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }

            if (showJoinHalqaDialog) {
                JoinHalqaDialog(
                    onDismiss = { showJoinHalqaDialog = false },
                    onConfirm = { code ->
                        showJoinHalqaDialog = false
                        Toast.makeText(context, "جاري الانضمام إلى الحلقة...", Toast.LENGTH_SHORT).show()
                        mainViewModel.joinHalqa(code) { success, result ->
                            if (success) {
                                Toast.makeText(context, "تم الانضمام للحلقة بنجاح! 🥳", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "فشل الانضمام: $result", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            }

            val inviteCode by mainViewModel.inviteCodeFlow.collectAsState()

            if (showInviteCodeDialog) {
                InviteCodeDialog(
                    halqaName = halqaName.ifEmpty { "حلقة الفجر" },
                    inviteCode = inviteCode.ifEmpty { halqaId ?: "" },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Invite Code", inviteCode.ifEmpty { halqaId ?: "" })
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ كود الدعوة بنجاح! 📋", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        val codeToShare = inviteCode.ifEmpty { halqaId ?: "" }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "انضم معي إلى حلقة «$halqaName» في تطبيق حلقة الفجر! 🌅\nكود الدعوة الخاص بنا هو: $codeToShare")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة كود الدعوة"))
                    },
                    onDismiss = { showInviteCodeDialog = false }
                )
            }

            if (showLeaveHalqaDialog) {
                LeaveHalqaDialog(
                    onDismiss = { showLeaveHalqaDialog = false },
                    onConfirm = {
                        showLeaveHalqaDialog = false
                        mainViewModel.leaveHalqa { success, error ->
                            if (success) {
                                Toast.makeText(context, "تمت مغادرة الحلقة بنجاح 🚪", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "فشلت مغادرة الحلقة: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }

        composable(Screen.Settings.route) {
            val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)

            var userCity by remember { mutableStateOf(prefs.getString(AlarmPreferences.KEY_USER_CITY, "مكة المكرمة") ?: "مكة المكرمة") }
            var calcMethod by remember { mutableStateOf(prefs.getString(AlarmPreferences.KEY_PRAYER_CALC_METHOD, "جامعة أم القرى (مكة المكرمة)") ?: "جامعة أم القرى (مكة المكرمة)") }

            var alarmTimingType by remember { mutableStateOf(prefs.getString(AlarmPreferences.KEY_ALARM_TIMING_TYPE, "with") ?: "with") }
            var alarmTimingOffset by remember { mutableIntStateOf(prefs.getInt(AlarmPreferences.KEY_ALARM_TIMING_OFFSET_MINUTES, 0)) }
            var alarmTimingDesc by remember { mutableStateOf(prefs.getString(AlarmPreferences.KEY_ALARM_TIMING_DESC, "مع أذان الفجر بالضبط") ?: "مع أذان الفجر بالضبط") }

            val savedChallengeType = prefs.getString(AlarmPreferences.KEY_CHALLENGE_TYPE, "math") ?: "math"
            val savedChallengeDiff = prefs.getString(AlarmPreferences.KEY_CHALLENGE_DIFFICULTY, "medium") ?: "medium"
            fun formatChallenge(t: String, d: String): String {
                val tName = when(t) {
                    "word" -> "ترتيب كلمة"
                    "shake" -> "رج الهاتف"
                    else -> "معادلة حسابية"
                }
                val dName = when(d) {
                    "easy" -> "سهل"
                    "hard" -> "صعب"
                    else -> "متوسط"
                }
                return "$tName - $dName"
            }
            var challengeType by remember { mutableStateOf(savedChallengeType) }
            var challengeDifficulty by remember { mutableStateOf(savedChallengeDiff) }
            var challengeText by remember { mutableStateOf(formatChallenge(savedChallengeType, savedChallengeDiff)) }

            val savedSoundCode = prefs.getString(AlarmPreferences.KEY_ALARM_SOUND_CHOICE, "default") ?: "default"
            fun formatSoundName(code: String): String {
                return when(code) {
                    "afasy" -> "الأذان بصوت الشيخ مشاري العفاسي"
                    "abdulbasit" -> "الأذان بصوت الشيخ عبدالباسط عبدالصمد"
                    "islamic" -> "نغمة إسلامية هادئة"
                    else -> "نغمة النظام الافتراضية"
                }
            }
            var alarmSoundCode by remember { mutableStateOf(savedSoundCode) }
            var alarmSoundText by remember { mutableStateOf(formatSoundName(savedSoundCode)) }

            var travelModeStatus by remember { mutableStateOf(com.bagomri.fajrloop.alarm.TravelModeManager.getTravelModeStatusText(context)) }

            var isVibrateEnabled by remember { mutableStateOf(prefs.getBoolean(AlarmPreferences.KEY_VIBRATE_ON_ALARM, true)) }
            var isAdhkarEnabled by remember { mutableStateOf(prefs.getBoolean(AlarmPreferences.KEY_SHOW_ADHKAR_AFTER_ALARM, true)) }
            var isDuaEnabled by remember { mutableStateOf(prefs.getBoolean(AlarmPreferences.KEY_DAILY_DUA_NOTIFICATION, true)) }

            fun performLocationFetch() {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                                   locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                if (!isGpsEnabled) {
                    Toast.makeText(context, "يرجى تشغيل خيار موقع GPS لتحديد مدينتك تلقائياً", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                } else {
                    try {
                        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                        if (location != null) {
                            var detectedName = "موقعي الحالي"
                            try {
                                val geocoder = Geocoder(context, Locale("ar"))
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    detectedName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "موقعي الحالي"
                                }
                            } catch (e: Exception) {
                                detectedName = "موقعي الحالي"
                            }

                            userCity = detectedName
                            prefs.edit()
                                .putString(AlarmPreferences.KEY_USER_CITY, detectedName)
                                .putFloat(AlarmPreferences.KEY_USER_LATITUDE_FLOAT, location.latitude.toFloat())
                                .putFloat(AlarmPreferences.KEY_USER_LONGITUDE_FLOAT, location.longitude.toFloat())
                                .apply()

                            val uid = AuthManager.getUserId()
                            if (uid != null) {
                                UserRepository().updateUserLocation(
                                    uid,
                                    UserLocation(latitude = location.latitude, longitude = location.longitude, cityName = detectedName)
                                ) {}
                            }

                            Toast.makeText(context, "تم تحديد موقعك بنجاح: $detectedName", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "جاري تحديد إحداثيات GPS، يرجى المحاولة بعد لحظات", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "حدث خطأ أثناء تحديد الموقع: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                if (isGranted) {
                    performLocationFetch()
                } else {
                    Toast.makeText(context, "يلزم منح صلاحية الموقع لتحديد مدينتك تلقائياً", Toast.LENGTH_SHORT).show()
                }
            }

            SettingsScreen(
                userCity = userCity,
                calcMethod = calcMethod,
                alarmTimingDesc = alarmTimingDesc,
                alarmTimingType = alarmTimingType,
                alarmTimingOffset = alarmTimingOffset,
                challengeText = challengeText,
                challengeType = challengeType,
                challengeDifficulty = challengeDifficulty,
                alarmSoundText = alarmSoundText,
                alarmSoundCode = alarmSoundCode,
                travelModeStatus = travelModeStatus,
                isVibrateEnabled = isVibrateEnabled,
                isAdhkarEnabled = isAdhkarEnabled,
                isDuaEnabled = isDuaEnabled,
                onVibrateChange = { checked ->
                    isVibrateEnabled = checked
                    prefs.edit().putBoolean(AlarmPreferences.KEY_VIBRATE_ON_ALARM, checked).apply()
                },
                onAdhkarChange = { checked ->
                    isAdhkarEnabled = checked
                    prefs.edit().putBoolean(AlarmPreferences.KEY_SHOW_ADHKAR_AFTER_ALARM, checked).apply()
                },
                onDuaChange = { checked ->
                    isDuaEnabled = checked
                    prefs.edit().putBoolean(AlarmPreferences.KEY_DAILY_DUA_NOTIFICATION, checked).apply()
                },
                onLocationClick = {
                    val hasFinePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    val hasCoarsePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

                    if (!hasFinePermission && !hasCoarsePermission) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        performLocationFetch()
                    }
                },
                onTravelModeClick = { navController.navigate(Screen.TravelMode.route) },
                onBackupCodeClick = { navController.navigate(Screen.BackupCode.route) },

                onSaveCalcMethod = { method ->
                    calcMethod = method
                    prefs.edit().putString(AlarmPreferences.KEY_PRAYER_CALC_METHOD, method).apply()
                    Toast.makeText(context, "تم حفظ طريقة الحساب بنجاح", Toast.LENGTH_SHORT).show()
                },
                onSaveAlarmTiming = { type, offset, desc ->
                    alarmTimingType = type
                    alarmTimingOffset = offset
                    alarmTimingDesc = desc
                    prefs.edit()
                        .putString(AlarmPreferences.KEY_ALARM_TIMING_TYPE, type)
                        .putInt(AlarmPreferences.KEY_ALARM_TIMING_OFFSET_MINUTES, offset)
                        .putString(AlarmPreferences.KEY_ALARM_TIMING_DESC, desc)
                        .apply()
                    Toast.makeText(context, "تم حفظ توقيت المنبه بنجاح", Toast.LENGTH_SHORT).show()
                },
                onSaveChallenge = { type, diff ->
                    challengeType = type
                    challengeDifficulty = diff
                    challengeText = formatChallenge(type, diff)
                    prefs.edit()
                        .putString(AlarmPreferences.KEY_CHALLENGE_TYPE, type)
                        .putString(AlarmPreferences.KEY_CHALLENGE_DIFFICULTY, diff)
                        .apply()
                    Toast.makeText(context, "تم حفظ تحدي الاستيقاظ بنجاح", Toast.LENGTH_SHORT).show()
                },
                onSaveAlarmSound = { code, title ->
                    alarmSoundCode = code
                    alarmSoundText = title
                    prefs.edit().putString(AlarmPreferences.KEY_ALARM_SOUND_CHOICE, code).apply()
                    Toast.makeText(context, "تم حفظ نغمة المنبه: $title", Toast.LENGTH_SHORT).show()
                },
                onPermissionsManageClick = {
                    navController.navigate(Screen.PermissionSetup.route)
                },
                onTestAlarmClick = {
                    com.bagomri.fajrloop.alarm.AlarmScheduler.scheduleTestAlarm(context, 10)
                    Toast.makeText(context, "🧪 تم جدولة منبه تجريبي بعد 10 ثوانٍ!", Toast.LENGTH_SHORT).show()
                },
                onGuideClick = { navController.navigate(Screen.Guide.route) },
                onPrivacyClick = {
                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://salehbagomri.github.io/fajrloop-privacy/"))
                    context.startActivity(intent)
                },
                onLogoutClick = {
                    AuthManager.signOut()
                    loginViewModel.resetLoginState()
                    mainViewModel.clearUserDataOnLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TravelMode.route) {
            val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            val isEnabled = com.bagomri.fajrloop.alarm.TravelModeManager.isTravelModeActive(context)
            val type = prefs.getString(AlarmPreferences.KEY_TRAVEL_MODE_TYPE, "indefinite") ?: "indefinite"
            val until = prefs.getString(AlarmPreferences.KEY_TRAVEL_MODE_UNTIL, "حتى الإلغاء اليدوي") ?: "حتى الإلغاء اليدوي"

            TravelModeScreen(
                initialEnabled = isEnabled,
                initialType = type,
                initialUntil = until,
                onSaveTravelMode = { enabled, t, untilText, untilTimestamp ->
                    com.bagomri.fajrloop.alarm.TravelModeManager.setTravelMode(
                        context = context,
                        enabled = enabled,
                        type = t,
                        untilText = untilText,
                        untilTimestamp = untilTimestamp
                    )
                    val msg = if (enabled) "✈️ تم تفعيل وضع السفر وإيقاف المنبه" else "🔔 تم إيقاف وضع السفر واستعادة المنبه"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Guide.route) {
            GuideScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.BackupCode.route) {
            val prefs = context.getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
            val halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val seed = ((dateStr + (halqaId ?: "")).hashCode()).absoluteValue
            val totpCode = ((seed % 900000) + 100000).toString()

            BackupCodeScreen(
                halqaId = halqaId,
                totpCode = totpCode,
                isAlarmEnabled = true,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            val statsUiState by statsViewModel.uiState.collectAsState()
            val currentUid = AuthManager.getUserId() ?: ""

            StatsScreen(
                state = statsUiState,
                currentUid = currentUid,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Chat.route) {
            val activeHalqaId by mainViewModel.halqaIdFlow.collectAsState()
            val messages by chatViewModel.messagesFlow.collectAsState()
            val halqaName by chatViewModel.halqaNameFlow.collectAsState()

            LaunchedEffect(activeHalqaId) {
                chatViewModel.startListening(activeHalqaId)
            }

            ChatScreen(
                title = halqaName,
                messages = messages,
                currentUid = chatViewModel.currentUid,
                onSendMessage = { text, type -> chatViewModel.sendMessage(text, type) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.MorningAdhkar.route) {
            AdhkarScreen(
                onFinish = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
