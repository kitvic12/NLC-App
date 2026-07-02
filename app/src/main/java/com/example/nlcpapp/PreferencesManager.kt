package com.example.nlcpapp

import android.content.Context

object PreferencesManager {
    
    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("first_launch", true)
    }
    
    fun setFirstLaunchCompleted(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("first_launch", false).apply()
    }
    
    fun isNotificationsScreenShown(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("notifications_screen_shown", false)
    }
    
    fun setNotificationsScreenShown(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_screen_shown", true).apply()
    }
    
    fun isNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("notifications_enabled", true)
    }
    
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }
    
    fun isDarkTheme(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("dark_theme", false)
    }
    
    fun setDarkTheme(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_theme", enabled).apply()
    }
}
