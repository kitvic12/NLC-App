package com.example.nlcpapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.nlcpapp.MainActivity
import com.example.nlcpapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://antcloud.ddns.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title ?: "Уведомление"
        val body = remoteMessage.notification?.body ?: ""
        val imageUrl = remoteMessage.data["image_url"] ?: ""

        Log.d("FCM", "Title: $title, Body: $body, Image: $imageUrl")

        showNotification(title, body, imageUrl)
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "New FCM token: $token")
        sendTokenToServer(token)
    }

    private fun showNotification(title: String, message: String, imageUrl: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (imageUrl.isNotEmpty()) {
                putExtra("image_url", imageUrl)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "nlc_notifications"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NLC Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendTokenToServer(token: String) {
        Log.d("FCM", "Sending token to server: $token")
        
        val request = FcmTokenRequest(token)
        val call = apiService.sendFcmToken(request)
        
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("FCM", "Token sent successfully")
                } else {
                    Log.e("FCM", "Failed to send token: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("FCM", "Error sending token: ${t.message}")
            }
        })
    }
}
