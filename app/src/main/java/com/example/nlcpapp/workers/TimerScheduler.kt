package com.example.nlcpapp.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.nlcpapp.receivers.TimerAlarmReceiver

object TimerScheduler {
    
    fun scheduleTimer(
        context: Context,
        sessionId: String,
        endTime: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            putExtra("session_id", sessionId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Используем setExactAndAllowWhileIdle для точного срабатывания даже в Doze mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                endTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                endTime,
                pendingIntent
            )
        }
    }
    
    fun cancelTimer(context: Context, sessionId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            putExtra("session_id", sessionId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
}
