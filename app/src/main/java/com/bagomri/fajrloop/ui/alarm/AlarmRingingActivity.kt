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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.alarm.AlarmPreferences
import com.bagomri.fajrloop.alarm.AlarmSoundService
import com.bagomri.fajrloop.auth.AuthManager
import com.bagomri.fajrloop.databinding.ActivityAlarmRingingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.TimeZone
import kotlin.math.absoluteValue

/**
 * AlarmRingingActivity — شاشة رنين المنبه
 *
 * تضم تحديات الاستيقاظ (المرحلة 5) ومنطق التصعيد والمزامنة الدائرية (المرحلة 6).
 */
class AlarmRingingActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_TRIGGER_TIME = "extra_trigger_time"
        private const val TAG = "AlarmRingingActivity"
    }

    private lateinit var binding: ActivityAlarmRingingBinding
    private var alarmLabel = "صلاة الفجر"
    private var triggerTime = 0L

    // خصائص التحديات
    private var challengeType = "math"
    private var challengeDifficulty = "medium"
    private var isChallengeSolved = false
    private var isAlarmDismissed = false
    private var isVolumeEnforced = true
    private var isLaunchingDialer = false

    private val homeButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {
                val reason = intent.getStringExtra("reason")
                if (reason != null && (reason == "homekey" || reason == "recentapps")) {
                    android.util.Log.d(TAG, "Home or Recents button pressed! reason=$reason")
                    if (!isAlarmDismissed) {
                        // إعادة قفل المنبه وجر الشاشة للأمام فوراً
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

    private val dialerWatchdog = object : Runnable {
        override fun run() {
            if (isLaunchingDialer && !isAlarmDismissed) {
                val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val isCallActive = audioManager.mode == AudioManager.MODE_IN_CALL || 
                                   audioManager.mode == AudioManager.MODE_IN_COMMUNICATION
                
                if (!isCallActive) {
                    isLaunchingDialer = false
                    val relaunchIntent = Intent(this@AlarmRingingActivity, AlarmRingingActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(relaunchIntent)
                } else {
                    handler.postDelayed(this, 1500)
                }
            }
        }
    }

    // مستمع السجل اليومي السحابي (المرحلة 6)
    private var dailyRecordListener: ValueEventListener? = null
    private var dailyRecordRef: DatabaseReference? = null

    // 1. تحدي الحساب
    private var mathAnswer = 0
    private var mathSolvedCount = 0
    private val mathTotalRequired = 3

    // 2. تحدي الكلمات
    private val scrambledWords = listOf(
        Pair("ج ر ف", "فجر"),
        Pair("ة ا ل ص", "صلاة"),
        Pair("د ج س م", "مسجد"),
        Pair("ن ا م ي إ", "إيمان"),
        Pair("ن آ ر ق", "قرآن"),
        Pair("ة ك م", "مكة"),
        Pair("ة ن ي د م", "مدينة")
    )
    private var correctWord = ""

    // 3. تحدي الهز
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
                        onChallengePassed()
                    }
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyLockScreenFlags()
        super.onCreate(savedInstanceState)

        binding = ActivityAlarmRingingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmLabel = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "صلاة الفجر"
        triggerTime = intent.getLongExtra(EXTRA_TRIGGER_TIME, System.currentTimeMillis())

        // رفع صوت المنبه للحد الأقصى ومنع التغيير
        forceMaxAlarmVolume()

        // حظر الإيماءات وزر الرجوع بالكامل في هواتف أندرويد الحديثة والقديمة
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // لا تفعل شيئاً لحجب زر الرجوع وإيماءات السحب
            }
        })

        // فرض الصوت الأقصى بانتظام كل 500 ملي ثانية
        handler.post(volumeEnforcer)

        // تسجيل مستقبل زر الهوم والتطبيقات الأخيرة
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
        startAnimations()
        setupChallenge()

        // البدء بمراقبة حالة السجل السحابي للمزامنة الدائرية
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val halqaId = prefs.getString("current_halqa_id", null)
        if (!halqaId.isNullOrEmpty()) {
            // إعادة ضبط الحالة السحابية للمنبه للتأكد من عدم تخطي المنبه تلقائياً بسبب اختبار سابق اليوم
            updateDailyStatus("pending")
            startObservingDailyRecord(halqaId)
            loadPartnerDetails(halqaId)
        }

        // المستمعين للتركيز للتمرير التلقائي للأعلى حتى لا تختفي الحقول خلف الكيبورد
        binding.inputMathAnswer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.postDelayed({
                    binding.scrollView.smoothScrollTo(0, binding.cardChallengePanel.top + binding.inputMathAnswer.top)
                }, 200)
            }
        }
        binding.inputWordAnswer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.postDelayed({
                    binding.scrollView.smoothScrollTo(0, binding.cardChallengePanel.top + binding.inputWordAnswer.top)
                }, 200)
            }
        }
        binding.inputTotpCode.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.scrollView.postDelayed({
                    binding.scrollView.smoothScrollTo(0, binding.cardChallengePanel.top + binding.inputTotpCode.top)
                }, 200)
            }
        }
    }

    /**
     * رفع صوت المنبه للحد الأقصى حتى لا يستطيع المستخدم تجاهله.
     */
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
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
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

            // زر الاستغاثة SOS
            btnSos.setOnClickListener {
                triggerEmergencySos()
            }

            btnSubmitTotp.setOnClickListener {
                verifyTotpCode()
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
                    generateWordPuzzle()
                    btnSubmitWord.setOnClickListener {
                        val input = inputWordAnswer.text.toString().trim()
                        if (input.equals(correctWord, ignoreCase = true)) {
                            onChallengePassed()
                        } else {
                            showToast("❌ الكلمة غير صحيحة، حاول مجدداً")
                        }
                    }
                }
                else -> { // math
                    layoutChallengeMath.visibility = View.VISIBLE
                    textChallengeTitle.text = "تحدي الرياضيات 🧮"
                    textChallengeSubtitle.text = "حل 3 مسائل حسابية متتالية لإيقاظ عقلك"
                    generateMathQuestion()
                    btnSubmitMath.setOnClickListener {
                        val inputStr = inputMathAnswer.text.toString().trim()
                        val inputVal = inputStr.toIntOrNull()
                        if (inputVal == mathAnswer) {
                            mathSolvedCount++
                            if (mathSolvedCount >= mathTotalRequired) {
                                onChallengePassed()
                            } else {
                                inputMathAnswer.setText("")
                                textChallengeSubtitle.text = "أحسنت! حل المسألة ${mathSolvedCount + 1} من $mathTotalRequired"
                                generateMathQuestion()
                            }
                        } else {
                            showToast("❌ إجابة خاطئة! ركز وحاول مجدداً")
                        }
                    }
                }
            }
        }
    }

    private fun generateMathQuestion() {
        val random = Random()
        val a: Int
        val b: Int
        val op: String
        when (challengeDifficulty) {
            "easy" -> {
                a = random.nextInt(15) + 1
                b = random.nextInt(15) + 1
                op = "+"
                mathAnswer = a + b
            }
            "hard" -> {
                a = random.nextInt(8) + 2
                b = random.nextInt(9) + 11
                op = "*"
                mathAnswer = a * b
            }
            else -> { // medium
                a = random.nextInt(50) + 10
                b = random.nextInt(40) + 10
                op = if (random.nextBoolean()) "+" else "-"
                mathAnswer = if (op == "+") a + b else a - b
            }
        }
        binding.textMathQuestion.text = "$a $op $b = ?"
    }

    private fun generateWordPuzzle() {
        val random = Random()
        val pair = scrambledWords[random.nextInt(scrambledWords.size)]
        correctWord = pair.second
        binding.textScrambledLetters.text = pair.first
    }

    private fun registerShakeSensor() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun unregisterShakeSensor() {
        sensorManager?.unregisterListener(sensorEventListener)
    }

    /**
     * عند حل التحدي بنجاح (المرحلة 6):
     * 1. نقوم بتخفيض صوت المنبه محلياً (Soften).
     * 2. نغير الاهتزاز لنبض خفيف.
     * 3. نحدث الحالة سحابياً إلى challenge_done.
     * 4. نعرض واجهة انتظار تأكيد الزميل المسؤول.
     */
    private fun onChallengePassed() {
        isChallengeSolved = true
        showToast("🎉 تم تجاوز التحدي بنجاح!")

        // 1. تخفيف الرنين محلياً
        startService(Intent(this, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_SOFTEN_ALARM
        })

        // إيقاف مفرض الصوت الأقصى
        isVolumeEnforced = false
        handler.removeCallbacks(volumeEnforcer)

        // 2. تحديث الحالة سحابياً إلى challenge_done
        updateDailyStatus("challenge_done")

        // 3. إظهار واجهة تأكيد الاستيقاظ (اتصال + TOTP + استغاثة)
        binding.apply {
            layoutChallengeMath.visibility = View.GONE
            layoutChallengeShake.visibility = View.GONE
            layoutChallengeWord.visibility = View.GONE
            layoutWaitingConfirmation.visibility = View.VISIBLE
        }
    }

    /**
     * إرسال نداء استغاثة سحابي عاجل وتخفيف الصوت محلياً
     */
    private fun triggerEmergencySos() {
        // 1. تحديث الحالة سحابياً إلى panic (لتطلق السيرفرات إشعارات FCM عالية الأولوية)
        updateDailyStatus("panic")

        // 2. تخفيف المنبه محلياً لتمكين المستخدم من سماع المكالمات الهاتفية من أصدقائه
        startService(Intent(this, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_SOFTEN_ALARM
        })

        // إيقاف مفرض الصوت الأقصى
        isVolumeEnforced = false
        handler.removeCallbacks(volumeEnforcer)

        // 3. تحويل الشاشة إلى حالة انتظار الدعم
        binding.apply {
            layoutChallengeMath.visibility = View.GONE
            layoutChallengeShake.visibility = View.GONE
            layoutChallengeWord.visibility = View.GONE
            
            layoutWaitingConfirmation.visibility = View.VISIBLE
            textWaitingDesc.text = "🚨 نداء الاستغاثة نشط! يرجى الانتظار، زملائك في الحلقة يحاولون الاتصال بك الآن لمساعدتك على الاستيقاظ."
            btnSos.visibility = View.GONE
        }

        showToast("🚨 تم إرسال نداء استغاثة عاجل لأعضاء الحلقة")
    }

    /**
     * مراقبة السجل السحابي للاستيقاظ - يغلق المنبه فوراً إذا كتب الزميل المسؤول "awake"
     */
    private fun startObservingDailyRecord(halqaId: String) {
        val uid = AuthManager.getUserId() ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        dailyRecordRef = FirebaseDatabase.getInstance()
            .getReference("dailyRecords")
            .child(halqaId)
            .child(currentDate)
            .child(uid)

        dailyRecordListener = dailyRecordRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val status = snapshot.child("status").value as? String
                    if (status == "awake" && isChallengeSolved) {
                        showToast("✅ تم تأكيد استيقاظك سحابياً من زميلك المسؤول!")
                        dismissAlarm(status = "awake")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e(TAG, "Database listener cancelled", error.toException())
            }
        })
    }

    /**
     * جلب بيانات المسؤول للاتصال به
     */
    private fun loadPartnerDetails(halqaId: String) {
        val uid = AuthManager.getUserId() ?: return
        FirebaseDatabase.getInstance().getReference("halqas").child(halqaId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val membersSnap = snapshot.child("members")
                        
                        var supervisorUid: String? = null
                        var supervisorName = "المسؤول"
                        
                        // البحث عن العضو المسؤول عن إيقاظ المستخدم الحالي
                        for (member in membersSnap.children) {
                            val respFor = member.child("responsibleForUserId").value as? String
                            if (respFor == uid && member.key != uid) {
                                supervisorUid = member.key
                                supervisorName = member.child("displayName").value as? String ?: "المسؤول"
                                break
                            }
                        }
                        
                        // إذا لم يتم العثور على مسؤول مباشر، نأخذ أول عضو آخر في الحلقة
                        if (supervisorUid == null) {
                            for (member in membersSnap.children) {
                                if (member.key != uid) {
                                    supervisorUid = member.key
                                    supervisorName = member.child("displayName").value as? String ?: "المسؤول"
                                    break
                                }
                            }
                        }
                        
                        if (supervisorUid != null) {
                            binding.textWaitingDesc.text = "بانتظار تأكيد استيقاظك من زميلك المسؤول: $supervisorName"
                            
                            // جلب رقم الهاتف من ملف تعريف المستخدم المسؤول إن وجد
                            FirebaseDatabase.getInstance().getReference("users").child(supervisorUid)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(userSnap: DataSnapshot) {
                                        val phone = userSnap.child("phone").value as? String 
                                            ?: userSnap.child("phoneNumber").value as? String 
                                            ?: ""
                                        binding.btnCallPartner.setOnClickListener {
                                            launchDialer(phone)
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        binding.btnCallPartner.setOnClickListener {
                                            launchDialer("")
                                        }
                                    }
                                })
                        } else {
                            binding.textWaitingDesc.text = "بانتظار تأكيد استيقاظك من زميلك المسؤول..."
                            binding.btnCallPartner.setOnClickListener {
                                launchDialer("")
                            }
                        }
                    } else {
                        binding.btnCallPartner.setOnClickListener {
                            launchDialer("")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.btnCallPartner.setOnClickListener {
                        launchDialer("")
                    }
                }
            })
    }

    private fun launchDialer(phoneNumber: String) {
        try {
            isLaunchingDialer = true
            val dialIntent = if (phoneNumber.isNotEmpty()) {
                Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:$phoneNumber"))
            } else {
                Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:"))
            }
            dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && keyguardManager.isKeyguardLocked) {
                // فك قفل الشاشة برمجياً لتمكين لوحة الاتصال من الظهور فوق شاشة القفل
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

            // بدء فحص حارس المكالمات للتأكد من خروج المستخدم من لوحة الاتصال أو انتهاء مكالمته
            handler.removeCallbacks(dialerWatchdog)
            handler.postDelayed(dialerWatchdog, 4000)
        } catch (e: Exception) {
            isLaunchingDialer = false
            showToast("❌ تعذر فتح تطبيق الاتصال")
        }
    }

    private fun updateDailyStatus(status: String) {
        val uid = AuthManager.getUserId()
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
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
                "alarmTime" to getIso8601String(Date(triggerTime)),
                "challengeDoneAt" to isoDate
            )

            recordRef.setValue(recordMap)
                .addOnSuccessListener {
                    android.util.Log.d(TAG, "Successfully updated status: $status")
                }
                .addOnFailureListener {
                    android.util.Log.e(TAG, "Failed to update status", it)
                }
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

    private fun dismissAlarm(status: String) {
        isAlarmDismissed = true
        handler.removeCallbacks(volumeEnforcer)

        // إيقاف خدمة الصوت والرنين تماماً
        val stopIntent = Intent(this, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_STOP_ALARM
        }
        startService(stopIntent)

        // تحديث قاعدة البيانات
        updateDailyStatus(status)

        // إغلاق الشاشة
        finish()
    }

    private fun getIso8601String(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
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

        // توليد الرمز المتوقع لليوم الحالي (نفس الطريقة المستخدمة في BackupCodeActivity)
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val seed = (dateStr + halqaId).hashCode().absoluteValue
        val expectedCode = (seed % 900000) + 100000 // رقم من 6 خانات

        val userCodeVal = userInput.toIntOrNull()
        if (userCodeVal == expectedCode) {
            showToast("✅ رمز الطوارئ صحيح! تم إلغاء قفل المنبه.")
            dismissAlarm(status = "awake")
        } else {
            showToast("❌ رمز الطوارئ غير صحيح، حاول مجدداً")
        }
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
        handler.removeCallbacks(dialerWatchdog)
        if (challengeType == "shake" && !isChallengeSolved) {
            registerShakeSensor()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterShakeSensor()
    }

    /**
     * عند محاولة المستخدم الخروج بزر Home — نعيد فتح الشاشة فوراً.
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isAlarmDismissed && !isLaunchingDialer) {
            val relaunchIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(relaunchIntent)
        }
    }

    override fun onStop() {
        super.onStop()
        // إذا أُغلقت الشاشة بشكل غير شرعي (مثلاً من Recent Apps)، نعيد فتحها
        if (!isAlarmDismissed && !isLaunchingDialer && !isFinishing) {
            val relaunchIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(relaunchIntent)
        }
    }

    /**
     * حظر أزرار الصوت لمنع المستخدم من كتم الصوت باستخدام dispatchKeyEvent.
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!isAlarmDismissed) {
            val keyCode = event.keyCode
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                if (isVolumeEnforced) {
                    forceMaxAlarmVolume()
                }
                return true // استهلاك الحدث تماماً لمنع تغيير الصوت أو عرض مؤشر النظام
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(dialerWatchdog)
        dailyRecordListener?.let {
            dailyRecordRef?.removeEventListener(it)
        }
        try {
            unregisterReceiver(homeButtonReceiver)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to unregister homeButtonReceiver", e)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // تجاهل الرجوع لمنع التخطي
    }
}
