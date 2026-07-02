package com.example.nlcpapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nlcpapp.data.api.models.Observation
import com.example.nlcpapp.data.api.models.ObservationSummary
import com.example.nlcpapp.data.repository.ObservationRepository
import kotlinx.coroutines.launch

class ObservationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ObservationRepository(application)
    
    var observationSummaries by mutableStateOf<List<ObservationSummary>>(emptyList())
        private set
    
    var selectedObservations by mutableStateOf<List<Observation>>(emptyList())
        private set
    
    var selectedDate by mutableStateOf("")
        private set
    
    var selectedUser by mutableStateOf("")
        private set
    
    var selectedPlace by mutableStateOf("")
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var totalDates by mutableStateOf(0)
        private set
    
    var brightnessData by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set
    
    var intensityData by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set
    
    var weatherData by mutableStateOf<List<Pair<String, Int>>>(emptyList())
        private set
    
    init {
        loadObservations()
        refreshFromApi()
    }
    
    fun loadObservations() {
        viewModelScope.launch {
            try {
                repository.getAllObservations().collect { obs ->
                    Log.d("ViewModel", "Loaded ${obs.size} observations from DB")
                    val summaries = mutableListOf<ObservationSummary>()
                    val grouped = obs.groupBy { it.date }
                    
                    grouped.forEach { (date, observations) ->
                        val byUserAndPlace = observations.groupBy { "${it.name}|${it.place}" }
                        byUserAndPlace.forEach { (_, userObs) ->
                            val first = userObs.first() 
                            val startTime = userObs.sortedBy { getSortOrder(it.time) }.firstOrNull()?.time ?: ""
                            summaries.add(
                                ObservationSummary(
                                    date = date,
                                    user = first.name,
                                    place = first.place,
                                    startTime = startTime
                                )
                            )
                        }
                    }
                    
                    observationSummaries = summaries.sortedByDescending { parseDate(it.date) }
                    totalDates = grouped.size
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading observations", e)
                errorMessage = "Ошибка загрузки: ${e.message}"
            }
        }
    }
    
    fun refreshFromApi() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val result = repository.fetchObservationsFromApi()
                result.fold(
                    onSuccess = { data ->
                        totalDates = data.size
                        Log.d("ViewModel", "Fetched $totalDates dates from API")
                    },
                    onFailure = { error ->
                        Log.e("ViewModel", "API error", error)
                        errorMessage = "Ошибка API: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                Log.e("ViewModel", "Error refreshing", e)
                errorMessage = "Ошибка: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun selectObservation(date: String, user: String, place: String) {
        viewModelScope.launch {
            try {
                repository.getObservationsByDate(date).collect { observations ->
                    Log.d("ViewModel", "Found ${observations.size} observations for date $date")
                    val filtered = observations.filter { 
                        it.name == user && it.place == place 
                    }
                    Log.d("ViewModel", "Filtered to ${filtered.size} observations for user $user, place $place")
                    
                    selectedObservations = filtered.sortedBy { getSortOrder(it.time) }
                    selectedDate = date
                    selectedUser = user
                    selectedPlace = place
                    
                    prepareChartData(selectedObservations)
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error selecting observation", e)
                errorMessage = "Ошибка: ${e.message}"
            }
        }
    }
    
    private fun getSortOrder(time: String): Int {
        val hour = time.split(":")[0].toIntOrNull() ?: 0
        return if (hour >= 12) hour else hour + 24
    }
    
    private fun prepareChartData(observations: List<Observation>) {
        Log.d("ViewModel", "Preparing chart data for ${observations.size} observations")
        
        brightnessData = observations.map { 
            Log.d("ViewModel", "Time: ${it.time}, Brightness: ${it.brightness}")
            it.time to it.brightness 
        }
        
        intensityData = observations.map { 
            Log.d("ViewModel", "Time: ${it.time}, Intensity: ${it.intensity}")
            it.time to it.intensity 
        }
        
        weatherData = observations.map {
            val value = when (it.weather) {
                "А" -> 5
                "Б" -> 4
                "В" -> 3
                "Г" -> 2
                "Д" -> 1
                else -> 3
            }
            Log.d("ViewModel", "Time: ${it.time}, Weather: ${it.weather} -> $value")
            it.time to value
        }
        
        Log.d("ViewModel", "Brightness data size: ${brightnessData.size}")
        Log.d("ViewModel", "Intensity data size: ${intensityData.size}")
        Log.d("ViewModel", "Weather data size: ${weatherData.size}")
    }
    
    private fun parseDate(dateString: String): java.util.Date {
        return try {
            java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                .parse(dateString) ?: java.util.Date(0)
        } catch (e: Exception) {
            java.util.Date(0)
        }
    }
}
