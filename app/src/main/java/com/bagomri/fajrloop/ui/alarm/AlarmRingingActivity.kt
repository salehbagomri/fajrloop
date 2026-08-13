package com.bagomri.fajrloop.ui.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.alarm.AlarmSoundService
import com.bagomri.fajrloop.ui.theme.FajrLoopTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AlarmRingingActivity — شاشة رنين المنبه الإلزامية (Jetpack Compose with LockScreen Enforcer)
 */
class AlarmRingingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_TRIGGER_TIME = "extra_trigger_time"
        private const val TAG = "AlarmRingingActivity"
    }

    private val viewModel: AlarmRingingViewModel by viewModels()

    private var alarmLabel = "صلاة الفجر"
    private var triggerTime = 0L

    private var challengeType = "math"
    private var challengeDifficulty = "easy"
    private var isAlarmDismissed = false
    private var isVolumeEnforced = true
    private var isLaunchingDialer = false
    private var isSnoozed = false
    private var startRingingTime = 0L
    private var adhkarLaunched = false

    // StateFlows for Compose UI
    private val mathQuestionFlow = MutableStateFlow("")
    private var mathAnswer = 0
    private var mathSolvedCount = 0
    private val mathTotalRequired = 3

    private val scrambledWordFlow = MutableStateFlow("")
    private var correctWord = ""

    private val shakeCountFlow = MutableStateFlow(0)
    private val shakeRequired = 30
    private var lastShakeTime = 0L
    private val shakeThreshold = 13.0f

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null

    private val homeButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                if (reason != null && (reason == "homekey" || reason == "recentapps")) {
                    if (!isAlarmDismissed) {
                        isLaunchingDialer = false
                        val relaunchIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        }
                        context.startActivity(relaunchIntent)
                    }
                }
            }
        }
    }

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val volumeEnforcer = object : Runnable {
        override fun run() {
            if (!isAlarmDismissed && isVolumeEnforced) {
                forceMaxAlarmVolume()
                handler.postDelayed(this, 500)
            }
        }
    }

    private var dialerLaunchTime = 0L
    private var wasInActiveCall = false
    private val DIALER_GRACE_PERIOD_MS = 3000L
    private val WATCHDOG_INTERVAL_MS = 2000L

    private val dialerWatchdog = object : Runnable {
        override fun run() {
            if (isAlarmDismissed) return

            if (isLaunchingDialer) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val isCallActive = audioManager.mode == AudioManager.MODE_IN_CALL ||
                                   audioManager.mode == AudioManager.MODE_IN_COMMUNICATION

                val elapsed = System.currentTimeMillis() - dialerLaunchTime

                when {
                    isCallActive -> {
                        wasInActiveCall = true
                        handler.postDelayed(this, WATCHDOG_INTERVAL_MS)
                    }
                    wasInActiveCall -> {
                        isLaunchingDialer = false
                        wasInActiveCall = false
                        bringActivityToFront()
                    }
                    elapsed > DIALER_GRACE_PERIOD_MS -> {
                        isLaunchingDialer = false
                        bringActivityToFront()
                    }
                    else -> {
                        handler.postDelayed(this, WATCHDOG_INTERVAL_MS)
                    }
                }
            }
        }
    }

    private fun bringActivityToFront() {
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.moveTaskToFront(taskId, android.app.ActivityManager.MOVE_TASK_WITH_HOME)
        } catch (e: Exception) {
            val relaunchIntent = Intent(this@AlarmRingingActivity, AlarmRingingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(relaunchIntent)
        }
    }

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            if (magnitude > shakeThreshold) {
                val now = System.currentTimeMillis()
                if (now - lastShakeTime > 250) {
                    lastShakeTime = now
                    val newCount = shakeCountFlow.value + 1
                    shakeCountFlow.value = newCount
                    if (newCount >= shakeRequired) {
                        unregisterShakeSensor()
                        viewModel.onChallengePassed()
                    }
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLockScreenFlags()
        @Suppress("DEPRECATION")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onCreate(savedInstanceState)
        startRingingTime = System.currentTimeMillis()

        alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "صلاة الفجر"
        triggerTime = intent.getLongExtra(EXTRA_TRIGGER_TIME, System.currentTimeMillis())

        forceMaxAlarmVolume()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })

        handler.post(volumeEnforcer)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(homeButtonReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS), Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(homeButtonReceiver, IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to register homeButtonReceiver", e)
        }

        loadChallengeSettings()
        setupChallenge()
        setupObservers()

        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val halqaId = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
        if (!halqaId.isNullOrEmpty()) {
            viewModel.updateDailyStatus("pending")
            viewModel.startObservingDailyRecord(halqaId)
            viewModel.loadPartnerDetails(halqaId)
        }

        setContent {
            FajrLoopTheme {
                val isSolved by viewModel.isChallengeSolvedFlow.collectAsState()
                val snoozeCount by viewModel.snoozeCountLeftFlow.collectAsState()
                val supervisorName by viewModel.supervisorNameFlow.collectAsState()

                val mathQuestion by mathQuestionFlow.collectAsState()
                val scrambledWord by scrambledWordFlow.collectAsState()
                val shakeCount by shakeCountFlow.collectAsState()

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeStr = timeFormat.format(Date(triggerTime))

                val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

                LaunchedEffect(Unit) {
                    viewModel.errorFlow.collect { errorMessage ->
                        snackbarHostState.showSnackbar(
                            message = errorMessage,
                            duration = androidx.compose.material3.SnackbarDuration.Short
                        )
                    }
                }

                AlarmRingingScreen(
                    alarmLabel = alarmLabel,
                    alarmTimeFormatted = timeStr,
                    challengeType = challengeType,
                    challengeDifficulty = challengeDifficulty,
                    mathQuestion = mathQuestion,
                    scrambledWord = scrambledWord,
                    shakeCount = shakeCount,
                    shakeRequired = shakeRequired,
                    isChallengeSolved = isSolved,
                    snoozeCountLeft = snoozeCount,
                    supervisorName = supervisorName,
                    snackbarHostState = snackbarHostState,
                    onMathSubmit = { inputVal ->
                        if (inputVal == mathAnswer) {
                            mathSolvedCount++
                            if (mathSolvedCount >= mathTotalRequired) {
                                viewModel.onChallengePassed()
                            } else {
                                setupMathQuestion()
                            }
                        } else {
                            showToast("❌ إجابة خاطئة! ركز وحاول مجدداً")
                        }
                    },
                    onWordSubmit = { inputWord ->
                        if (inputWord.equals(correctWord, ignoreCase = true)) {
                            viewModel.onChallengePassed()
                        } else {
                            showToast("❌ الكلمة غير صحيحة، حاول مجدداً")
                        }
                    },
                    onPledgeSubmit = { pledgeText ->
                        if (viewModel.validatePledgeText(pledgeText)) {
                            viewModel.dismissAlarm("awake")
                        } else {
                            showToast("✍️ يرجى كتابة عبارة التعهد كاملة لتأكيد القيام")
                        }
                    },
                    onSnoozeClick = {
                        val currentHalqa = prefs.getString(AlarmPreferences.KEY_CURRENT_HALQA_ID, null)
                        if (!currentHalqa.isNullOrEmpty()) {
                            isSnoozed = true
                            viewModel.triggerSnooze(currentHalqa)
                        } else {
                            showToast("حدث خطأ في تحديد الحلقة")
                        }
                    }
                )
            }
        }
    }

    private fun forceMaxAlarmVolume() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to set max alarm volume", e)
        }
    }

    private fun applyLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    private fun loadChallengeSettings() {
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        challengeType = prefs.getString(AlarmPreferences.KEY_CHALLENGE_TYPE, "math") ?: "math"
        challengeDifficulty = prefs.getString(AlarmPreferences.KEY_CHALLENGE_DIFFICULTY, "easy") ?: "easy"
    }

    private fun setupChallenge() {
        when (challengeType) {
            "shake" -> {
                shakeCountFlow.value = 0
                registerShakeSensor()
            }
            "word" -> {
                val puzzle = viewModel.generateWordPuzzle()
                correctWord = puzzle.second
                scrambledWordFlow.value = puzzle.first
            }
            else -> {
                setupMathQuestion()
            }
        }
    }

    private fun setupMathQuestion() {
        val question = viewModel.generateMathQuestion(challengeDifficulty)
        mathAnswer = question.second
        mathQuestionFlow.value = question.first
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.isChallengeSolvedFlow.collect { solved ->
                if (solved) {
                    showToast("🎉 تم تجاوز التحدي بنجاح!")
                    try {
                        val duration = (System.currentTimeMillis() - startRingingTime) / 1000
                        com.bagomri.fajrloop.data.AnalyticsHelper.logChallengeSolved(challengeType, "normal", duration)
                    } catch (e: Exception) {
                        android.util.Log.e(TAG, "Failed to log challenge_solved", e)
                    }
                    startService(Intent(this@AlarmRingingActivity, AlarmSoundService::class.java).apply {
                        action = AlarmSoundService.ACTION_SOFTEN_ALARM
                    })
                    isVolumeEnforced = false
                    handler.removeCallbacks(volumeEnforcer)
                }
            }
        }



        lifecycleScope.launch {
            viewModel.dismissFinishedFlow.collect { finished ->
                if (finished && !isFinishing && !adhkarLaunched) {
                    adhkarLaunched = true
                    isAlarmDismissed = true
                    handler.removeCallbacks(volumeEnforcer)
                    startService(Intent(this@AlarmRingingActivity, AlarmSoundService::class.java).apply {
                        action = AlarmSoundService.ACTION_STOP_ALARM
                    })

                    if (!isSnoozed) {
                        try {
                            val duration = (System.currentTimeMillis() - startRingingTime) / 1000
                            com.bagomri.fajrloop.data.AnalyticsHelper.logWakeConfirmed(duration)
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Failed to log wake_confirmed", e)
                        }

                        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                        val showAdhkar = prefs.getBoolean(AlarmPreferences.KEY_SHOW_ADHKAR_AFTER_ALARM, true)
                        if (showAdhkar) {
                            val mainIntent = Intent(this@AlarmRingingActivity, com.bagomri.fajrloop.ui.main.MainActivity::class.java).apply {
                                putExtra("navigate_to", "morning_adhkar")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            startActivity(mainIntent)
                        }
                    }

                    finish()
                }
            }
        }
    }

    private fun registerShakeSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unregisterShakeSensor() {
        sensorManager?.unregisterListener(sensorEventListener)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        isLaunchingDialer = false
        wasInActiveCall = false
        handler.removeCallbacks(dialerWatchdog)
        if (challengeType == "shake" && !viewModel.isChallengeSolvedFlow.value) {
            registerShakeSensor()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterShakeSensor()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isAlarmDismissed && !isLaunchingDialer) {
            val relaunchIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(relaunchIntent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && !isAlarmDismissed && !isLaunchingDialer) {
            val relaunchIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(relaunchIntent)
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isAlarmDismissed && !isLaunchingDialer && !isFinishing) {
            bringActivityToFront()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!isAlarmDismissed) {
            val keyCode = event.keyCode
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                if (isVolumeEnforced) {
                    forceMaxAlarmVolume()
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(dialerWatchdog)
        try {
            unregisterReceiver(homeButtonReceiver)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to unregister homeButtonReceiver", e)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}
}
