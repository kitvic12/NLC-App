package com.example.nlcpapp.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nlcpapp.services.*
import com.example.nlcpapp.utils.Transliterator
import com.example.nlcpapp.workers.TimerScheduler
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

data class ObservationEntry(
    val brightness: Int,
    val intensity: Int,
    val weather: String,
    val time: String
)

enum class ObservationStep {
    LOCATION,
    FORM
}

class ObservationViewModel : ViewModel() {
    private val apiService: ApiService = ApiClient.apiService
    /* private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://antcloud.ddns.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java) */
    
    var currentStep by mutableStateOf(ObservationStep.LOCATION)
    var sessionId by mutableStateOf<String?>(null)
    var place by mutableStateOf("Определение...")
    var userName by mutableStateOf("")
    var observations by mutableStateOf<List<ObservationEntry>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var timeRemaining by mutableStateOf(0)
    var isTimerRunning by mutableStateOf(false)
    var locationReady by mutableStateOf(false)
    var lat by mutableStateOf(0.0)
    var lon by mutableStateOf(0.0)
    var showTimerNotification by mutableStateOf(false)
    var isSessionLoaded by mutableStateOf(false)
    var timerEndTime by mutableStateOf(0L)
    
    private var timerJob: kotlinx.coroutines.Job? = null
    
    fun updateUserName(name: String) {
        userName = name
    }
    
    fun startObservation(token: String) {
        if (userName.isBlank()) {
            errorMessage = "Введите имя"
            return
        }
        if (!locationReady) {
            errorMessage = "Геолокация не получена"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        val tokenTranslit = Transliterator.transliterate(token)
        val request = StartObservationRequest(tokenTranslit, lat, lon)
        apiService.startObservation(request).enqueue(object : Callback<StartObservationResponse> {
            override fun onResponse(call: Call<StartObservationResponse>, response: Response<StartObservationResponse>) {
                isLoading = false
                if (response.isSuccessful && response.body() != null) {
                    sessionId = response.body()!!.session_id
                    place = response.body()!!.place
                    currentStep = ObservationStep.FORM
                    timeRemaining = 0
                    isTimerRunning = false
                    isSessionLoaded = true
                } else {
                    errorMessage = "Ошибка начала сессии: ${response.code()}"
                }
            }
            
            override fun onFailure(call: Call<StartObservationResponse>, t: Throwable) {
                isLoading = false
                errorMessage = "Ошибка сети: ${t.message}"
            }
        })
    }
    
    fun startTimer(context: Context) {
        isTimerRunning = true
        timeRemaining = 1800
        
        val endTime = System.currentTimeMillis() + (1800 * 1000)
        timerEndTime = endTime
        
        saveSessionToPrefs(context)
        
        sessionId?.let { id ->
            TimerScheduler.scheduleTimer(context, id, endTime)
        }
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timeRemaining > 0 && isTimerRunning) {
                delay(1000)
                timeRemaining--
            }
            if (timeRemaining == 0) {
                isTimerRunning = false
                showTimerNotification = true
            }
        }
    }
    
    fun resumeTimerFromSaved(context: Context) {
        val prefs = context.getSharedPreferences("observation_prefs", Context.MODE_PRIVATE)
        val savedEndTime = prefs.getLong("timer_end_time", 0)
        val savedIsTimerRunning = prefs.getBoolean("is_timer_running", false)
        
        if (savedIsTimerRunning && savedEndTime > 0) {
            val currentTime = System.currentTimeMillis()
            val remaining = (savedEndTime - currentTime) / 1000
            
            if (remaining > 0) {
                timerEndTime = savedEndTime
                timeRemaining = remaining.toInt()
                isTimerRunning = true
                
                timerJob?.cancel()
                timerJob = viewModelScope.launch {
                    while (timeRemaining > 0 && isTimerRunning) {
                        delay(1000)
                        timeRemaining--
                    }
                    if (timeRemaining == 0) {
                        isTimerRunning = false
                        showTimerNotification = true
                    }
                }
            } else {
                timeRemaining = 0
                isTimerRunning = false
            }
        }
    }
    
    fun addObservation(brightness: Int, intensity: Int, weather: String, context: Context) {
        sessionId?.let { id ->
            isLoading = true
            val request = AddObservationRequest(id, brightness, intensity, weather)
            apiService.addObservation(request).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    isLoading = false
                    if (response.isSuccessful) {
                        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        observations = observations + ObservationEntry(brightness, intensity, weather, currentTime)
                        startTimer(context)
                    } else {
                        errorMessage = "Ошибка сохранения: ${response.code()}"
                    }
                }
                
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    isLoading = false
                    errorMessage = "Ошибка сети: ${t.message}"
                }
            })
        } ?: run {
            errorMessage = "Сессия не начата"
        }
    }
    
    fun finishObservation(context: Context, onFinish: () -> Unit) {
        sessionId?.let { id ->
            isLoading = true
            
            TimerScheduler.cancelTimer(context, id)
            
            apiService.finishObservation(mapOf("session_id" to id)).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    isLoading = false
                    if (response.isSuccessful) {
                        val prefs = context.getSharedPreferences("observation_prefs", Context.MODE_PRIVATE)
                        prefs.edit().clear().apply()
                        
                        sessionId = null
                        observations = emptyList()
                        timeRemaining = 0
                        isTimerRunning = false
                        currentStep = ObservationStep.LOCATION
                        isSessionLoaded = false
                        timerEndTime = 0
                        timerJob?.cancel()
                        onFinish()
                    } else {
                        errorMessage = "Ошибка завершения: ${response.code()}"
                    }
                }
                
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    isLoading = false
                    errorMessage = "Ошибка сети: ${t.message}"
                }
            })
        }
    }
    
    fun saveSessionToPrefs(context: Context) {
        val prefs = context.getSharedPreferences("observation_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val observationsJson = gson.toJson(observations)
        
        prefs.edit()
            .putString("session_id", sessionId)
            .putString("place", place)
            .putString("user_name", userName)
            .putString("observations", observationsJson)
            .putInt("time_remaining", timeRemaining)
            .putBoolean("is_timer_running", isTimerRunning)
            .putBoolean("show_timer_notification", showTimerNotification)
            .putLong("timer_end_time", timerEndTime)
            .apply()
    }
    
    fun loadSessionFromPrefs(context: Context): Boolean {
        if (isSessionLoaded) return true
        
        val prefs = context.getSharedPreferences("observation_prefs", Context.MODE_PRIVATE)
        val savedSessionId = prefs.getString("session_id", null)
        val savedPlace = prefs.getString("place", null)
        val savedUserName = prefs.getString("user_name", null)
        val savedObservationsJson = prefs.getString("observations", null)
        
        if (savedSessionId != null && savedPlace != null && savedUserName != null) {
            sessionId = savedSessionId
            place = savedPlace
            userName = savedUserName
            currentStep = ObservationStep.FORM
            isSessionLoaded = true
            
            if (savedObservationsJson != null) {
                val gson = Gson()
                val type = object : TypeToken<List<ObservationEntry>>() {}.type
                observations = gson.fromJson(savedObservationsJson, type) ?: emptyList()
            }
            
            resumeTimerFromSaved(context)
            return true
        }
        return false
    }
    
    fun formatTime(): String {
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationScreen(
    viewModel: ObservationViewModel = viewModel(),
    onBackToMenu: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as androidx.activity.ComponentActivity
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(activity) }
    
    LaunchedEffect(Unit) {
        if (!viewModel.isSessionLoaded) {
            viewModel.loadSessionFromPrefs(context)
        }
    }
    
    LaunchedEffect(viewModel.timeRemaining, viewModel.isTimerRunning) {
        if (viewModel.sessionId != null) {
            viewModel.saveSessionToPrefs(context)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Наблюдение СО") },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "В меню"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (viewModel.currentStep) {
                ObservationStep.LOCATION -> LocationStepContent(
                    viewModel = viewModel,
                    context = context,
                    fusedLocationClient = fusedLocationClient
                )
                ObservationStep.FORM -> FormStepContent(
                    viewModel = viewModel,
                    context = context,
                    onFinish = onFinish
                )
            }
        }
    }
}

@Composable
fun LocationStepContent(
    viewModel: ObservationViewModel,
    context: Context,
    fusedLocationClient: FusedLocationProviderClient
) {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            getLocation(fusedLocationClient, viewModel)
        } else {
            viewModel.errorMessage = "Разрешение на геолокацию не получено"
        }
    }
    
    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (fineGranted || coarseGranted) {
            getLocation(fusedLocationClient, viewModel)
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Начало наблюдения",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = viewModel.userName,
            onValueChange = { viewModel.updateUserName(it) },
            label = { Text("Ваше имя") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (viewModel.locationReady) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✅ Геолокация получена", fontWeight = FontWeight.Medium)
                    Text("Широта: ${String.format("%.4f", viewModel.lat)}")
                    Text("Долгота: ${String.format("%.4f", viewModel.lon)}")
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⏳ Получение геолокации...")
                    Text("Убедитесь, что GPS включен", fontSize = 12.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Button(
            onClick = {
                val token = viewModel.userName.ifBlank { "kitvic" }
                viewModel.startObservation(token)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = viewModel.locationReady && viewModel.userName.isNotBlank() && !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Начать наблюдение", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun FormStepContent(
    viewModel: ObservationViewModel,
    context: Context,
    onFinish: () -> Unit
) {
    var brightness by remember { mutableStateOf(3) }
    var intensity by remember { mutableStateOf(5) }
    var weather by remember { mutableStateOf("Б") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📍 ${viewModel.place}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "👤 ${viewModel.userName}",
                    fontSize = 14.sp
                )
                if (viewModel.isTimerRunning) {
                    Text(
                        text = "⏱️ До следующего: ${viewModel.formatTime()}",
                        fontSize = 14.sp,
                        color = if (viewModel.timeRemaining < 300) 
                            MaterialTheme.colorScheme.error 
                        else MaterialTheme.colorScheme.onSurface
                    )
                } else if (viewModel.timeRemaining == 0 && viewModel.observations.isNotEmpty()) {
                    Text(
                        text = "✅ Готово к добавлению",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Яркость СО (1-5):", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = brightness.toFloat(),
            onValueChange = { brightness = it.toInt() },
            valueRange = 1f..5f,
            steps = 3
        )
        Text("Значение: $brightness", fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Интенсивность (1-10):", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Slider(
            value = intensity.toFloat(),
            onValueChange = { intensity = it.toInt() },
            valueRange = 1f..10f,
            steps = 8
        )
        Text("Значение: $intensity", fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Погода:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("А", "Б", "В", "Г", "Д").forEach { w ->
                FilterChip(
                    selected = weather == w,
                    onClick = { weather = w },
                    label = { Text(w) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Button(
            onClick = {
                viewModel.addObservation(brightness, intensity, weather, context)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !viewModel.isLoading && viewModel.timeRemaining == 0
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else if (viewModel.isTimerRunning) {
                Text("Ожидание: ${viewModel.formatTime()}", fontSize = 16.sp)
            } else {
                Text("Добавить наблюдение", fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = {
                viewModel.finishObservation(context, onFinish)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            enabled = !viewModel.isLoading
        ) {
            Text("Завершить наблюдения", fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Добавлено наблюдений: ${viewModel.observations.size}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        if (viewModel.observations.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.observations) { obs ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("⏰ ${obs.time}", fontSize = 14.sp)
                                Text("☁️ Яркость: ${obs.brightness}, Интенсивность: ${obs.intensity}", fontSize = 14.sp)
                                Text("🌤️ Погода: ${obs.weather}", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getLocation(
    client: FusedLocationProviderClient,
    viewModel: ObservationViewModel
) {
    try {
        client.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                viewModel.lat = it.latitude
                viewModel.lon = it.longitude
                viewModel.locationReady = true
            } ?: run {
                val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                    10000
                ).build()
                
                val locationCallback = object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                        result.lastLocation?.let { loc ->
                            viewModel.lat = loc.latitude
                            viewModel.lon = loc.longitude
                            viewModel.locationReady = true
                        }
                        client.removeLocationUpdates(this)
                    }
                }
                
                client.requestLocationUpdates(locationRequest, locationCallback, android.os.Looper.getMainLooper())
            }
        }
    } catch (e: SecurityException) {
        viewModel.errorMessage = "Нет разрешения на геолокацию"
    }
}
