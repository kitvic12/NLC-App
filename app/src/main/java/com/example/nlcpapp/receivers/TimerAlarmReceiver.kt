package com.example.nlcpapp.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.nlcpapp.MainActivity
import com.example.nlcpapp.R

class TimerAlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra("session_id") ?: return
        
        sendNotification(context, sessionId)
        
        val prefs = context.getSharedPreferences("observation_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notification_sent", true).apply()
    }
    
    private fun sendNotification(context: Context, sessionId: String) {
        val channelId = "observation_timer_channel"
        val channelName = "Таймер наблюдений"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о завершении таймера наблюдений"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "observation")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            sessionId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Время добавить наблюдение!")
            .setContentText("Прошло 30 минут. Зафиксируйте данные о серебристых облаках.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(sessionId.hashCode(), notification)
    }
}
