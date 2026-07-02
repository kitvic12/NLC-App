package com.example.nlcpapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nlcpapp.data.api.models.ObservationSummary
import com.example.nlcpapp.viewmodel.ObservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen(
    viewModel: ObservationViewModel = viewModel(),
    onObservationClick: (String, String, String) -> Unit,
    onNewObservationClick: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("База данных наблюдений") },
                actions = {
                    IconButton(onClick = { viewModel.refreshFromApi() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Обновить"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewObservationClick,
                containerColor = Color(0xFF2563EB)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Начать наблюдение"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2563EB)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Всего дат наблюдений: ${viewModel.totalDates}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                viewModel.errorMessage != null -> {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                viewModel.observationSummaries.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Нет данных. Нажмите кнопку обновления.")
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.observationSummaries) { summary ->
                            ObservationSummaryCard(
                                summary = summary,
                                onClick = {
                                    onObservationClick(summary.date, summary.user, summary.place)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ObservationSummaryCard(
    summary: ObservationSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = summary.date,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Наблюдатель: ${summary.user}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Место: ${summary.place}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
