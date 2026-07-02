package com.example.nlcpapp.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nlcpapp.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    var isDarkTheme by remember { mutableStateOf(PreferencesManager.isDarkTheme(context)) }
    var notificationsEnabled by remember { mutableStateOf(PreferencesManager.isNotificationsEnabled(context)) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Icon(Icons.Filled.Close, contentDescription = "Закрыть")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Тема
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Тема", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (isDarkTheme) "темная" else "светлая",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { 
                        isDarkTheme = it
                        PreferencesManager.setDarkTheme(context, it)
                        // Пересоздаем Activity для применения темы
                        activity?.recreate()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Уведомления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Уведомления", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text(
                        if (notificationsEnabled) "вкл" else "выкл",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { 
                        notificationsEnabled = it
                        PreferencesManager.setNotificationsEnabled(context, it)
                    }
                )
            }
        }
    }
}
