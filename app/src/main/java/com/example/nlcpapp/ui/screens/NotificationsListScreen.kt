package com.example.nlcpapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nlcpapp.ui.components.BlueButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsListScreen(onBackToMenu: () -> Unit) {
    // Заглушка для списка уведомлений
    val notifications = listOf("уведомление 1", "уведомление 2", "уведомление 3")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Уведомления", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
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
            // Кнопка Очистить
            Button(
                onClick = { /* Очистить уведомления */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("Очистить", color = MaterialTheme.colorScheme.onSurface)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notification ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(notification, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
