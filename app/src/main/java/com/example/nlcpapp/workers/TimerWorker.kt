package com.example.nlcpapp.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nlcpapp.MainActivity
import com.example.nlcpapp.R

class TimerWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val sessionId = inputData.getString("session_id") ?: return Result.failure()
        val endTime = inputData.getLong("end_time", 0)
        
        val currentTime = System.currentTimeMillis()
        val remainingTime = endTime - currentTime
        
        if (remainingTime > 0) {
            TimerScheduler.scheduleTimer(applicationContext, sessionId, endTime)
            return Result.retry()
        }
        
        sendNotification(sessionId)
        
        val prefs = applicationContext.getSharedPreferences("observation_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notification_sent", true).apply()
        
        return Result.success()
    }
    
    private fun sendNotification(sessionId: String) {
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
            
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "observation")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Время добавить наблюдение!")
            .setContentText("Прошло 30 минут. Зафиксируйте данные о серебристых облаках.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(sessionId.hashCode(), notification)
    }
}
