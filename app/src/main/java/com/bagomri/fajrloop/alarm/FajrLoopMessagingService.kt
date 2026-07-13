package com.bagomri.fajrloop.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bagomri.fajrloop.R
import com.bagomri.fajrloop.auth.FcmTokenManager
import com.bagomri.fajrloop.ui.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FajrLoopMessagingService — خدمة استقبال رسائل الـ FCM السحابية ومعالجتها فورا بالخلفية
 */
class FajrLoopMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FajrLoopMessaging"
        private const val FCM_CHANNEL_ID = "fajrloop_fcm_channel"
        private const val NOTIFICATION_ID_PANIC = 2001
        private const val NOTIFICATION_ID_CONFIRM = 2002
        private const val NOTIFICATION_ID_CHAT = 2003
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "onNewToken: $token")
        FcmTokenManager.registerToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "onMessageReceived: data=${remoteMessage.data}")

        val data = remoteMessage.data
        val type = data["type"] ?: return
        val title = data["title"] ?: "تنبيه من حلقة الفجر"
        val body = data["body"] ?: ""

        createNotificationChannel()

        when (type) {
            "emergency_panic" -> {
                sendPanicNotification(title, body)
                launchMainActivity()
            }
            "challenge_done" -> {
                sendConfirmationNotification(title, body)
            }
            "wake_confirmed" -> {
                sendWakeConfirmedNotification(title, body)
                stopRingingLocally()
            }
            "chat_message" -> {
                sendChatNotification(title, body)
            }
        }
    }

    private fun sendPanicNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_PANIC, notification)
    }

    private fun sendConfirmationNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_CONFIRM, notification)
    }

    private fun sendWakeConfirmedNotification(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_CONFIRM, notification)
    }

    private fun sendChatNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_CHAT, notification)
    }

    private fun stopRingingLocally() {
        try {
            val stopIntent = Intent(this, AlarmSoundService::class.java).apply {
                action = AlarmSoundService.ACTION_STOP_ALARM
            }
            startService(stopIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop alarm locally from FCM service", e)
        }
    }

    private fun launchMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to auto-launch MainActivity from panic FCM", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(FCM_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    FCM_CHANNEL_ID,
                    "إشعارات حلقة الفجر",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "تنبيهات الاستغاثة وتأكيد الاستيقاظ والدردشة"
                    enableLights(true)
                    lightColor = Color.GREEN
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}
