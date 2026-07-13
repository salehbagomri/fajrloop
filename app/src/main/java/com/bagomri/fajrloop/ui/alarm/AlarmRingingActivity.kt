package com.bagomri.fajrloop.ui.alarm

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.KeyEvent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import android.content.BroadcastReceiver
import android.content.IntentFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.OnBackPressedCallback
import com.bagomri.fajrloop.ui.BaseActivity
import androidx.lifecycle.ViewModelProvider
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.alarm.AlarmSoundService
import com.bagomri.fajrloop.databinding.ActivityAlarmRingingBinding

/**
 * AlarmRingingActivity — شاشة رنين المنبه (MVVM Refactored)
 */
class AlarmRingingActivity : BaseActivity() {

    companion object {
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_TRIGGER_TIME = "extra_trigger_time"
        private const val TAG = "AlarmRingingActivity"
    }

    private lateinit var binding: ActivityAlarmRingingBinding
    private lateinit var viewModel: AlarmRingingViewModel
    private var alarmLabel = "صلاة الفجر"
    private var triggerTime = 0L

    // خصائص التحديات
    private var challengeType = "math"
    private var challengeDifficulty = "medium"
    private var isAlarmDismissed = false
    private var isVolumeEnforced = true
    private var isLaunchingDialer = false
    private var isSnoozed = false
    private var startRingingTime = 0L
    private var adhkarLaunched = false   // منع تشغيل الأذكار مرتين

    private val homeButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                if (reason != null && (reason == "homekey" || reason == "recentapps")) {
                    android.util.Log.d(TAG, "Home or Recents button pressed! reason=$reason")
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

    /**
     * 🔒 نظام القفل الذكي أثناء المكالمة:
     *
     * المنطق:
     *  1. عند فتح تطبيق الهاتف → فترة سماح 3 ثوانٍ (لفتح الـ Dialer)
     *  2. بعد فترة السماح: هل المكالمة نشطة؟
     *     - نعم → انتظر حتى تنتهي (تحقق كل 2 ثانية)
     *     - لا  → أعد المستخدم فوراً لشاشة التحدي
     *  3. عند انتهاء المكالمة → أعد فوراً
     *
     * النتيجة: المستخدم يستطيع الاتصال فقط، لا يستطيع الهروب لأي تطبيق آخر.
     */
    private var dialerLaunchTime = 0L
    private var wasInActiveCall = false
    private val DIALER_GRACE_PERIOD_MS = 3000L  // 3 ثوانٍ سماح لفتح الـ Dialer
    private val WATCHDOG_INTERVAL_MS = 2000L     // تحقق كل 2 ثانية

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
                        // مكالمة نشطة → سجّل وانتظر انتهاءها
                        wasInActiveCall = true
                        handler.postDelayed(this, WATCHDOG_INTERVAL_MS)
                    }
                    wasInActiveCall -> {
                        // المكالمة انتهت للتو → أعد المستخدم فوراً
                        android.util.Log.d(TAG, "Call ended — bringing user back to alarm screen")
                        isLaunchingDialer = false
                        wasInActiveCall = false
                        bringActivityToFront()
                    }
                    elapsed > DIALER_GRACE_PERIOD_MS -> {
                        // فترة السماح انتهت بدون مكالمة → أعد فوراً
                        android.util.Log.d(TAG, "Dialer grace period expired — bringing user back")
                        isLaunchingDialer = false
                        bringActivityToFront()
                    }
                    else -> {
                        // لا زلنا في فترة السماح → انتظر
                        handler.postDelayed(this, WATCHDOG_INTERVAL_MS)
                    }
                }
            }
        }
    }

    /**
     * يُعيد شاشة المنبه للمقدمة بشكل موثوق
     */
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

    // تحدي الحساب
    private var mathAnswer = 0
    private var mathSolvedCount = 0
    private val mathTotalRequired = 3

    // تحدي الكلمات
    private var correctWord = ""

    // تحدي الهز
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var shakeCount = 0
    private val shakeRequired = 30
    private var lastShakeTime = 0L
    private val shakeThreshold = 13.0f

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
                    shakeCount++
                    binding.textShakeCount.text = "$shakeCount / $shakeRequired"
                    binding.progressShake.progress = shakeCount
                    if (shakeCount >= shakeRequired) {
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
        super.onCreate(savedInstanceState)
        startRingingTime = System.currentTimeMillis()

        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[AlarmRingingViewModel::class.java]

        alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "صلاة الفجر"
        triggerTime = intent.getLongExtra(EXTRA_TRIGGER_TIME, System.currentTimeMillis())

        forceMaxAlarmVolume()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // حظر زر الرجوع
            }
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
        setupUI()
        setupObservers()
        startAnimations()
        setupChallenge()

        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val halqaId = prefs.getString("current_halqa_id", null)
        if (!halqaId.isNullOrEmpty()) {
            viewModel.updateDailyStatus("pending")
            viewModel.startObservingDailyRecord(halqaId)
            viewModel.loadPartnerDetails(halqaId)
        }

        // التمرير الذكي عند فتح لوحة المفاتيح — يظهر الإجابة + زر التحقق معاً
        setupKeyboardAwareScroll()
    }

    /**
     * يكشف ارتفاع لوحة المفاتيح عند فتحها ويُمرّر ScrollView تلقائياً
     * ليظهر حقل الإجابة + زر التحقق فوق اللوحة مباشرة
     */
    private fun setupKeyboardAwareScroll() {
        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.height
            val keyboardHeight = screenHeight - rect.bottom

            if (keyboardHeight > screenHeight * 0.15) {
                // لوحة المفاتيح مفتوحة — نُمرّر لإظهار زر التحقق
                val focusedView = currentFocus
                if (focusedView != null) {
                    // نحسب أين يجب التمرير: الزر المناسب بعد حقل الإجابة
                    val targetView = when (focusedView.id) {
                        R.id.input_math_answer -> binding.btnSubmitMath
                        R.id.input_word_answer -> binding.btnSubmitWord
                        R.id.input_totp_code   -> binding.btnSubmitTotp
                        else -> null
                    }
                    if (targetView != null) {
                        binding.scrollView.postDelayed({
                            // نحسب موضع الزر بالنسبة للـ ScrollView
                            val scrollPos = IntArray(2)
                            targetView.getLocationInWindow(scrollPos)
                            val rootPos = IntArray(2)
                            binding.scrollView.getLocationInWindow(rootPos)
                            // نُمرّر لأن يكون الزر ظاهراً فوق الكيبورد بهامش 32dp
                            val targetY = binding.scrollView.scrollY + (scrollPos[1] - rootPos[1]) - 32
                            binding.scrollView.smoothScrollTo(0, targetY)
                        }, 150)
                    }
                }
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
        challengeDifficulty = prefs.getString(AlarmPreferences.KEY_CHALLENGE_DIFFICULTY, "medium") ?: "medium"
    }

    private fun setupUI() {
        binding.apply {
            textAlarmLabel.text = alarmLabel
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            textAlarmTime.text = timeFormat.format(Date(triggerTime))

            btnSos.setOnClickListener {
                viewModel.triggerEmergencySos()
                com.bagomri.fajrloop.data.AnalyticsHelper.logEmergencyPanic()
            }

            btnSubmitTotp.setOnClickListener {
                verifyTotpCode()
            }

            btnSnooze.setOnClickListener {
                val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
                val halqaId = prefs.getString("current_halqa_id", null)
                if (!halqaId.isNullOrEmpty()) {
                    isSnoozed = true
                    viewModel.triggerSnooze(halqaId)
                } else {
                    showToast("حدث خطأ في تحديد الحلقة")
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.isChallengeSolved.observe(this) { solved ->
            if (solved) {
                showToast("🎉 تم تجاوز التحدي بنجاح!")
                try {
                    val duration = (System.currentTimeMillis() - startRingingTime) / 1000
                    com.bagomri.fajrloop.data.AnalyticsHelper.logChallengeSolved(challengeType ?: "unknown", "normal", duration)
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Failed to log challenge_solved", e)
                }
                startService(Intent(this, AlarmSoundService::class.java).apply {
                    action = AlarmSoundService.ACTION_SOFTEN_ALARM
                })
                isVolumeEnforced = false
                handler.removeCallbacks(volumeEnforcer)

                binding.apply {
                    layoutChallengeMath.visibility = View.GONE
                    layoutChallengeShake.visibility = View.GONE
                    layoutChallengeWord.visibility = View.GONE
                    btnSnooze.visibility = View.GONE
                    layoutWaitingConfirmation.visibility = View.VISIBLE
                }
            }
        }

        viewModel.isPanicActive.observe(this) { panic ->
            if (panic) {
                startService(Intent(this, AlarmSoundService::class.java).apply {
                    action = AlarmSoundService.ACTION_SOFTEN_ALARM
                })
                isVolumeEnforced = false
                handler.removeCallbacks(volumeEnforcer)

                binding.apply {
                    layoutChallengeMath.visibility = View.GONE
                    layoutChallengeShake.visibility = View.GONE
                    layoutChallengeWord.visibility = View.GONE
                    btnSnooze.visibility = View.GONE
                    
                    layoutWaitingConfirmation.visibility = View.VISIBLE
                    textWaitingDesc.text = "🚨 نداء الاستغاثة نشط! يرجى الانتظار، زملائك في الحلقة يحاولون الاتصال بك الآن لمساعدتك على الاستيقاظ."
                    btnSos.visibility = View.GONE
                }
                showToast("🚨 تم إرسال نداء استغاثة عاجل لأعضاء الحلقة")
            }
        }

        viewModel.snoozeCountLeft.observe(this) { count ->
            val isSolved = viewModel.isChallengeSolved.value == true
            val isPanic = viewModel.isPanicActive.value == true
            if (count > 0 && !isSolved && !isPanic) {
                binding.btnSnooze.visibility = View.VISIBLE
                binding.btnSnooze.text = "غفوة (متبقي: $count) ⏰"
            } else {
                binding.btnSnooze.visibility = View.GONE
            }
        }

        viewModel.supervisorName.observe(this) { name ->
            binding.textWaitingDesc.text = "بانتظار تأكيد استيقاظك من زميلك المسؤول: $name"
        }

        viewModel.supervisorPhone.observe(this) { phone ->
            binding.btnCallPartner.setOnClickListener {
                launchDialer(phone)
            }
        }

        viewModel.dismissFinished.observe(this) { finished ->
            if (finished && !isFinishing && !adhkarLaunched) {
                adhkarLaunched = true
                isAlarmDismissed = true
                handler.removeCallbacks(volumeEnforcer)
                startService(Intent(this, AlarmSoundService::class.java).apply {
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
                    val showAdhkar = prefs.getBoolean("show_adhkar_after_alarm", true)
                    if (showAdhkar) {
                        val adhkarIntent = Intent(this, com.bagomri.fajrloop.ui.adhkar.MorningAdhkarActivity::class.java)
                        startActivity(adhkarIntent)
                    }
                }

                finish()
            }
        }
    }

    private fun setupChallenge() {
        binding.apply {
            layoutChallengeMath.visibility = View.GONE
            layoutChallengeShake.visibility = View.GONE
            layoutChallengeWord.visibility = View.GONE
            layoutWaitingConfirmation.visibility = View.GONE

            when (challengeType) {
                "shake" -> {
                    layoutChallengeShake.visibility = View.VISIBLE
                    textChallengeTitle.text = "تحدي هز الهاتف 📱"
                    textChallengeSubtitle.text = "هز الهاتف 30 مرة متتالية بقوة لتنبيه جسمك"
                    textShakeCount.text = "0 / $shakeRequired"
                    progressShake.max = shakeRequired
                    progressShake.progress = 0
                    registerShakeSensor()
                }
                "word" -> {
                    layoutChallengeWord.visibility = View.VISIBLE
                    textChallengeTitle.text = "ترتيب الحروف 🧩"
                    textChallengeSubtitle.text = "أعد كتابة الكلمة بشكل صحيح لتجاوز المنبه"
                    
                    val puzzle = viewModel.generateWordPuzzle()
                    correctWord = puzzle.second
                    textScrambledLetters.text = puzzle.first

                    btnSubmitWord.setOnClickListener {
                        val input = inputWordAnswer.text.toString().trim()
                        if (input.equals(correctWord, ignoreCase = true)) {
                            viewModel.onChallengePassed()
                        } else {
                            showToast("❌ الكلمة غير صحيحة، حاول مجدداً")
                        }
                    }
                }
                else -> { // math
                    layoutChallengeMath.visibility = View.VISIBLE
                    textChallengeTitle.text = "تحدي الرياضيات 🧮"
                    textChallengeSubtitle.text = "حل 3 مسائل حسابية متتالية لإيقاظ عقلك"
                    
                    fun setupMathQuestion() {
                        val question = viewModel.generateMathQuestion(challengeDifficulty)
                        mathAnswer = question.second
                        textMathQuestion.text = question.first
                    }
                    
                    setupMathQuestion()

                    btnSubmitMath.setOnClickListener {
                        val inputStr = inputMathAnswer.text.toString().trim()
                        val inputVal = inputStr.toIntOrNull()
                        if (inputVal == mathAnswer) {
                            mathSolvedCount++
                            if (mathSolvedCount >= mathTotalRequired) {
                                viewModel.onChallengePassed()
                            } else {
                                inputMathAnswer.setText("")
                                textChallengeSubtitle.text = "أحسنت! حل المسألة ${mathSolvedCount + 1} من $mathTotalRequired"
                                setupMathQuestion()
                            }
                        } else {
                            showToast("❌ إجابة خاطئة! ركز وحاول مجدداً")
                        }
                    }
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

    private fun verifyTotpCode() {
        val userInput = binding.inputTotpCode.text.toString().replace(" ", "").trim()
        if (userInput.length < 6) {
            showToast("❌ يرجى إدخال رمز من 6 أرقام")
            return
        }

        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val halqaId = prefs.getString("current_halqa_id", null)
        if (halqaId.isNullOrEmpty()) {
            showToast("❌ خطأ: لم يتم العثور على معرّف الحلقة")
            return
        }

        if (viewModel.verifyTotpCode(userInput, halqaId)) {
            showToast("✅ رمز الطوارئ صحيح! تم إلغاء قفل المنبه.")
            viewModel.dismissAlarm("awake")
        } else {
            showToast("❌ رمز الطوارئ غير صحيح، حاول مجدداً")
        }
    }

    private fun launchDialer(phoneNumber: String) {
        try {
            isLaunchingDialer = true
            dialerLaunchTime = System.currentTimeMillis()
            wasInActiveCall = false

            val dialIntent = if (phoneNumber.isNotEmpty()) {
                Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phoneNumber"))
            } else {
                Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:"))
            }
            dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && keyguardManager.isKeyguardLocked) {
                keyguardManager.requestDismissKeyguard(this, object : android.app.KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        super.onDismissSucceeded()
                        startActivity(dialIntent)
                    }
                    override fun onDismissCancelled() {
                        super.onDismissCancelled()
                        isLaunchingDialer = false
                        showToast("🔓 يرجى إلغاء قفل الشاشة لإجراء المكالمة")
                    }
                    override fun onDismissError() {
                        super.onDismissError()
                        startActivity(dialIntent)
                    }
                })
            } else {
                startActivity(dialIntent)
            }

            // بدء المراقب الذكي — يبدأ بعد ثانية واحدة ويتحقق كل 2 ثانية
            handler.removeCallbacks(dialerWatchdog)
            handler.postDelayed(dialerWatchdog, 1000)
        } catch (e: Exception) {
            isLaunchingDialer = false
            showToast("❌ تعذر فتح تطبيق الاتصال")
        }
    }

    private fun startAnimations() {
        val pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.imageMosque.startAnimation(pulseAnimation)

        val glowAnimation = AnimationUtils.loadAnimation(this, R.anim.glow_pulse)
        binding.viewMosqueGlow.startAnimation(glowAnimation)

        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)
        binding.cardChallengePanel.startAnimation(slideUp)

        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        binding.textAlarmLabel.startAnimation(fadeIn)
        binding.textAlarmTime.startAnimation(fadeIn)
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
        // المستخدم عاد لشاشة المنبه — أوقف مراقبة المكالمة
        isLaunchingDialer = false
        wasInActiveCall = false
        handler.removeCallbacks(dialerWatchdog)
        if (challengeType == "shake" && !viewModel.isChallengeSolved.value!!) {
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
    override fun onBackPressed() {
        // تجاهل الرجوع
    }
}
