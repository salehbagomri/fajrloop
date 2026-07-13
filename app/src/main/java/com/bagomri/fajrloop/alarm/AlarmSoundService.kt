package com.bagomri.fajrloop.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.ui.alarm.AlarmRingingActivity

/**
 * AlarmSoundService — الخدمة الأمامية المسؤولة عن رنين المنبه
 *
 * تضمن استمرار الرنين حتى لو:
 * - أُغلق التطبيق من قائمة التطبيقات الأخيرة
 * - دخل الجهاز في وضع Doze Mode
 * - أُغلقت الشاشة أو قُفل الجهاز
 *
 * تحتفظ بـ WakeLock خاص بها بعد إطلاقها من AlarmReceiver.
 */
class AlarmSoundService : Service() {

    companion object {
        private const val TAG = "AlarmSoundService"

        const val ACTION_START_ALARM = "com.bagomri.fajrloop.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM  = "com.bagomri.fajrloop.ACTION_STOP_ALARM"
        const val ACTION_SOFTEN_ALARM = "com.bagomri.fajrloop.ACTION_SOFTEN_ALARM"

        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_TRIGGER_TIME = "extra_trigger_time"

        const val NOTIFICATION_CHANNEL_ALARM = "fajrloop_alarm_channel"
        const val NOTIFICATION_ID_ALARM = 101

        // حالة الصوت: كامل أو مخفف (بعد حل التحدي)
        var isAlarmRunning = false
            private set
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isSoftened = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireServiceWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")

        return when (intent?.action) {
            ACTION_START_ALARM -> {
                val label = intent.getStringExtra(EXTRA_ALARM_LABEL) ?: "صلاة الفجر"
                val triggerTime = intent.getLongExtra(EXTRA_TRIGGER_TIME, System.currentTimeMillis())
                startAlarm(label, triggerTime)
                START_STICKY
            }
            ACTION_STOP_ALARM -> {
                stopAlarm()
                START_NOT_STICKY
            }
            ACTION_SOFTEN_ALARM -> {
                softenAlarm()
                START_STICKY
            }
            else -> {
                Log.w(TAG, "Unknown action: ${intent?.action}")
                START_NOT_STICKY
            }
        }
    }

    private fun startAlarm(label: String, triggerTime: Long) {
        if (isAlarmRunning) {
            Log.d(TAG, "Alarm already running, ignoring duplicate start")
            return
        }

        isAlarmRunning = true
        isSoftened = false

        // رفع مستوى الصوت للحد الأقصى
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )

        // تشغيل نغمة المنبه
        startRingtone()

        // بدء الاهتزاز المستمر
        startVibration(continuous = true)

        // إطلاق شاشة رنين المنبه
        launchRingingScreen(label, triggerTime)

        // الانتقال لوضع الخدمة الأمامية مع إشعار
        val notification = buildAlarmNotification(label, triggerTime)
        startForeground(NOTIFICATION_ID_ALARM, notification)

        // تحرير WakeLock الخاص بـ AlarmReceiver بعد أن الخدمة استلمت السيطرة
        AlarmReceiver.releaseWakeLock()

        Log.d(TAG, "✅ Alarm started: $label")
    }

    /** تخفيف الصوت بعد حل تحدي الاستيقاظ (Phase 2 onwards) */
    private fun softenAlarm() {
        if (isSoftened) return
        isSoftened = true

        mediaPlayer?.let { player ->
            // تخفيض الصوت لمستوى متوسط واضح ومريح
            player.setVolume(0.35f, 0.35f)
        }

        // تغيير الاهتزاز لنمط نبضي خفيف
        stopVibration()
        startVibration(continuous = false)

        Log.d(TAG, "🔅 Alarm softened")
    }

    private fun stopAlarm() {
        Log.d(TAG, "🛑 Stopping alarm")

        isAlarmRunning = false
        isSoftened = false

        // إزالة علم التفعيل النشط وإلغاء الـ Watchdog لمنع إعادة التشغيل تلقائياً
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("alarm_active_ringing", false).apply()
        AlarmReceiver.cancelWatchdog(this)

        stopRingtone()
        stopVibration()
        releaseServiceWakeLock()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startRingtone() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmSoundService, alarmUri)
                isLooping = true
                setVolume(1.0f, 1.0f)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ringtone", e)
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    private fun startVibration(continuous: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (continuous) {
            // نمط الاهتزاز المستمر: يرن 1.5 ثانية، يتوقف 0.5 ثانية
            val pattern = longArrayOf(0, 1500, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } else {
            // نمط اهتزاز خفيف (بعد حل التحدي): نبض كل 3 ثوان
            val pattern = longArrayOf(0, 200, 2800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    /**
     * فتح شاشة رنين المنبه — تتخطى قفل الشاشة تلقائياً
     */
    private fun launchRingingScreen(label: String, triggerTime: Long) {
        val screenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
            putExtra(AlarmRingingActivity.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmRingingActivity.EXTRA_TRIGGER_TIME, triggerTime)
        }
        startActivity(screenIntent)
    }

    /**
     * بناء إشعار الخدمة الأمامية (إلزامي لـ ForegroundService)
     */
    private fun buildAlarmNotification(label: String, triggerTime: Long): Notification {
        // Intent لفتح شاشة الرنين عند الضغط على الإشعار
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra(AlarmRingingActivity.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmRingingActivity.EXTRA_TRIGGER_TIME, triggerTime)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_alarm_notification)
            .setContentTitle("🌅 $label")
            .setContentText("المنبه يرن — انقر للاستيقاظ")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ALARM,
            "منبه الفجر",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "قناة رنين منبه صلاة الفجر"
            setSound(null, null) // الصوت يُدار بواسطة MediaPlayer مباشرة
            enableVibration(false) // الاهتزاز يُدار برمجياً
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun acquireServiceWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FajrLoop::AlarmServiceWakeLock"
        ).also {
            it.setReferenceCounted(false)
            it.acquire() // بدون timeout — الخدمة ستحرره عند الإيقاف
        }
    }

    private fun releaseServiceWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: Application task swiped away!")

        // إذا كان المنبه نشطاً، أعد إطلاق شاشة الرنين وجدولة الـ Watchdog للتحقق الفوري
        val prefs = getSharedPreferences(AlarmPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val isAlarmActive = prefs.getBoolean("alarm_active_ringing", false)
        if (isAlarmActive) {
            launchRingingScreen("صلاة الفجر", System.currentTimeMillis())
            AlarmReceiver.scheduleWatchdog(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        Log.d(TAG, "AlarmSoundService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

