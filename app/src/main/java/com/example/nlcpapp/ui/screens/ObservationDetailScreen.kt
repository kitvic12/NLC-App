package com.example.nlcpapp.ui.screens
import androidx.compose.material.icons.Icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nlcpapp.data.api.models.Observation
import com.example.nlcpapp.viewmodel.ObservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationDetailScreen(
    date: String,
    user: String,
    place: String,
    viewModel: ObservationViewModel = viewModel(),
    onBackToMenu: () -> Unit
) {
    LaunchedEffect(date, user, place) {
        viewModel.selectObservation(date, user, place)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подробная информация") },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Подробная информация по наблюдению",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "👤 Пользователь: ${viewModel.selectedUser}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📅 Дата: ${viewModel.selectedDate}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📍 Место: ${viewModel.selectedPlace}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Таблица наблюдений",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            
            item {
                if (viewModel.selectedObservations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Время", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text("Погода", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text("Яркость", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text("Интенсивность", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            }
                            
                            HorizontalDivider()
                            
                            viewModel.selectedObservations.forEach { observation ->
                                ObservationRow(observation)
                            }
                        }
                    }
                }
            }
            
            item {
                if (viewModel.brightnessData.isNotEmpty()) {
                    Text("Яркость", style = MaterialTheme.typography.titleLarge)
                    SimpleGraph(data = viewModel.brightnessData)
                }
            }
            
            item {
                if (viewModel.intensityData.isNotEmpty()) {
                    Text("Интенсивность", style = MaterialTheme.typography.titleLarge)
                    SimpleGraph(data = viewModel.intensityData)
                }
            }
            
            item {
                if (viewModel.weatherData.isNotEmpty()) {
                    Text("Погода", style = MaterialTheme.typography.titleLarge)
                    SimpleGraph(data = viewModel.weatherData)
                }
            }
        }
    }
}

@Composable
fun ObservationRow(observation: Observation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(observation.time, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(observation.weather, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(observation.brightness.toString(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(observation.intensity.toString(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SimpleGraph(data: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        if (data.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет данных")
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                try {
                    val width = size.width
                    val height = size.height
                    val padding = 30f
                    
                    if (width < 100 || height < 100) return@Canvas
                    
                    val chartWidth = width - 2 * padding
                    val chartHeight = height - 2 * padding
                    
                    val maxValue = data.maxOf { it.second }.toFloat()
                    val minValue = data.minOf { it.second }.toFloat()
                    val range = if (maxValue == minValue) 1f else maxValue - minValue
                    
                    // Оси
                    drawLine(
                        color = Color.Black,
                        start = Offset(padding, padding),
                        end = Offset(padding, height - padding),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(padding, height - padding),
                        end = Offset(width - padding, height - padding),
                        strokeWidth = 2f
                    )
                    
                    // Линия и точки
                    if (data.size > 1) {
                        val path = Path()
                        val stepX = chartWidth / (data.size - 1).toFloat()
                        
                        data.forEachIndexed { index, (_, value) ->
                            val x = padding + index * stepX
                            val y = height - padding - ((value - minValue) / range) * chartHeight
                            
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        drawPath(
                            path = path,
                            color = Color(0xFF2196F3),
                            style = Stroke(width = 3f)
                        )
                        
                        data.forEachIndexed { index, (_, value) ->
                            val x = padding + index * stepX
                            val y = height - padding - ((value - minValue) / range) * chartHeight
                            
                            drawCircle(
                                color = Color(0xFFFF6B6B),
                                radius = 8f,
                                center = Offset(x, y)
                            )
                        }
                    } else if (data.size == 1) {
                        val x = width / 2
                        val y = height / 2
                        
                        drawCircle(
                            color = Color(0xFFFF6B6B),
                            radius = 8f,
                            center = Offset(x, y)
                        )
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибки рисования
                }
            }
        }
    }
}
