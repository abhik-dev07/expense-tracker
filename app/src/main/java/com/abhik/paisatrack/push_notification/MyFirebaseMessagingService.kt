package com.abhik.paisatrack.push_notification

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.abhik.paisatrack.MainActivity
import com.abhik.paisatrack.R
import com.abhik.paisatrack.data.AuthManager
import com.abhik.paisatrack.data.network.ApiClient
import com.abhik.paisatrack.data.network.UpdatePushTokenRequest
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = AuthManager.getUserId(applicationContext)
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    ApiClient.api.updatePushToken(
                        UpdatePushTokenRequest(userId = userId, pushToken = token)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Show notification either from the notification block or data payload
        val title = message.notification?.title ?: message.data["title"] ?: "Paisa-Track Alert"
        val body = message.notification?.body ?: message.data["body"] ?: "Check your latest budget transactions."

        showNotification(title, body)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "paisa_track_alerts_v3"
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("title", title)
            putExtra("body", message)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Reference the custom raw sound file
        val soundUri = Uri.parse("android.resource://" + packageName + "/raw/notification")

        val notificationIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            R.drawable.ic_notification
        } else {
            R.mipmap.ic_launcher
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(notificationIcon)
            .setColor(ContextCompat.getColor(this, R.color.splash_background))
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 200, 0))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ requires a NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Paisa Track Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for budget and expense push notifications"
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 0)
            }

            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}