package com.example.nlcpapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
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
fun MenuScreen(
    onNavigateToCameras: () -> Unit,
    onNavigateToDatabase: () -> Unit,
    onNavigateToObservation: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToReference: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Меню", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Уведомления")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            val menuItems = listOf(
                "Камеры" to onNavigateToCameras,
                "База данных" to onNavigateToDatabase,
                "Проверка СО" to onNavigateToObservation,
                "Сообщить об обнаружении" to onNavigateToReport,
                "Внести наблюдение" to onNavigateToObservation,
                "Поддержка" to onNavigateToSupport
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                items(menuItems) { (title, action) ->
                    BlueButton(
                        text = title,
                        onClick = action,
                        modifier = Modifier.height(64.dp)
                    )
                }
                
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BlueButton(
                        text = "Что такое СО?",
                        onClick = onNavigateToReference,
                        modifier = Modifier.height(64.dp)
                    )
                }
            }
        }
    }
}
