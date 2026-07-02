package com.example.nlcpapp.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit
import com.example.nlcpapp.ui.components.ZoomableImageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CamerasScreen(onBackToMenu: () -> Unit) {
    var expandedRegion by remember { mutableStateOf<String?>(null) }
    var selectedCamera by remember { mutableStateOf<String?>(null) }
    var cameraImage by remember { mutableStateOf<ByteArray?>(null) }
    var cameraBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOnlyClear by remember { mutableStateOf(false) }
    var clearCameras by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoadingCameras by remember { mutableStateOf(false) }
    var showZoomDialog by remember { mutableStateOf(false) }
    
    val regions = listOf("ЦФО", "Северо-Запад(СЗФО)", "Юг", "Поволжье", "Урал", "Сибирь", "Дальний")
    
    val placeDict = mapOf(
        "ЦФО" to listOf("Калуга", "Москва", "Попово", "Рязань", "Тула", "Уткино", "Ярославль"),
        "Северо-Запад(СЗФО)" to listOf("Васкелово", "Вологда", "Калининград", "Русское"),
        "Юг" to listOf("Гулькевичи", "Краснодар", "Тырнауз"),
        "Поволжье" to listOf("Азево", "Альметьевск", "Березники", "Ирбит", "Каменск-Уральский", "Пермь"),
        "Урал" to listOf("Остроленский", "Челябинск"),
        "Сибирь" to listOf("Айхал", "Багдарин", "Стрежевой", "Юрга"),
        "Дальний Восток" to listOf("Пятиречье", "Провидения")
    )
    
    val cityMap = mapOf(
        "Азево" to "https://starvisor.ru/azv/",
        "Айхал" to "https://starvisor.ru/ayk/",
        "Альметьевск" to "https://starvisor.ru/almet/",
        "Васкелово" to "https://starvisor.ru/spbd/",
        "Багдарин" to "https://starvisor.ru/bag/",
        "Березники" to "https://starvisor.ru/brz/",
        "Вологда" to "https://starvisor.ru/vlg/",
        "Гулькевичи" to "https://starvisor.ru/gul/",
        "Воркута" to "https://starvisor.ru/vrk/",
        "Провидения" to "https://starvisor.ru/prv/",
        "Ирбит" to "https://starvisor.ru/irb/",
        "Калининград" to "https://starvisor.ru/kln/",
        "Калуга" to "https://starvisor.ru/klg/",
        "Каменск-Уральский" to "https://starvisor.ru/kur/",
        "Краснодар" to "https://starvisor.ru/nov/",
        "Москва" to "https://starvisor.ru/msc/",
        "Остроленский" to "https://starvisor.ru/ost/",
        "Пермь" to "https://starvisor.ru/prm/",
        "Попово" to "https://starvisor.ru/ppv/",
        "Пятиречье" to "https://starvisor.ru/ptr/",
        "Русское" to "https://starvisor.ru/rus/",
        "Рязань" to "https://starvisor.ru/rzn/",
        "Стрежевой" to "https://starvisor.ru/str/",
        "Тула" to "https://starvisor.ru/tula/",
        "Уткино" to "https://starvisor.ru/utk/",
        "Челябинск" to "https://starvisor.ru/chb/",
        "Юрга" to "https://starvisor.ru/yur/",
        "Ярославль" to "https://starvisor.ru/yar/",
        "Тырнауз" to "https://gw.cmo.sai.msu.ru/webcam6.jpg"
    )
    
    LaunchedEffect(showOnlyClear) {
        if (showOnlyClear) {
            isLoadingCameras = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build()
                    
                    val request = Request.Builder()
                        .url("https://antcloud.ddns.net/kitvic/api/weather")
                        .build()
                    
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        parseClearCameras(body)
                    } else {
                        emptyList()
                    }
                }
                clearCameras = result
            } catch (e: Exception) {
                e.printStackTrace()
                clearCameras = emptyList()
            } finally {
                isLoadingCameras = false
            }
        } else {
            clearCameras = emptyList()
        }
    }
    
    LaunchedEffect(selectedCamera) {
        selectedCamera?.let { camera ->
            isLoading = true
            errorMessage = null
            cameraImage = null
            cameraBitmap = null
            
            val imageUrl = cityMap[camera]
            if (imageUrl != null) {
                val imageBytes = withTimeoutOrNull(20000) {
                    withContext(Dispatchers.IO) {
                        fetchCameraImage(camera, imageUrl)
                    }
                }
                
                if (imageBytes != null) {
                    cameraImage = imageBytes
                    cameraBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } else {
                    errorMessage = "Ошибка загрузки изображения"
                }
            }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Камеры", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "В меню")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = showOnlyClear,
                    onCheckedChange = { showOnlyClear = it }
                )
                Text("Оставить только чистое небо", fontSize = 16.sp)
                if (isLoadingCameras) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (selectedCamera != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = selectedCamera!!,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                            }
                        } else if (cameraBitmap != null) {
                            Image(
                                bitmap = cameraBitmap!!.asImageBitmap(),
                                contentDescription = "Нажмите для увеличения",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clickable { showZoomDialog = true },
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "👆 Нажмите для увеличения",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                regions.forEach { region ->
                    val camerasInRegion = placeDict[region] ?: emptyList()
                    val filteredCameras = if (showOnlyClear) {
                        camerasInRegion.filter { it in clearCameras }
                    } else {
                        camerasInRegion
                    }
                    
                    if (filteredCameras.isNotEmpty()) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                expandedRegion = if (expandedRegion == region) null else region
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "▶ $region",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(if (expandedRegion == region) "▼" else "▶")
                                    }
                                    
                                    if (expandedRegion == region) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        filteredCameras.forEach { camera ->
                                            Text(
                                                text = "▶ $camera",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedCamera = camera }
                                                    .padding(vertical = 4.dp),
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showZoomDialog && cameraBitmap != null) {
        ZoomableImageDialog(
            bitmap = cameraBitmap!!,
            onDismiss = { showZoomDialog = false }
        )
    }
}

suspend fun fetchCameraImage(city: String, url: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            
            if (city == "Тырнауз") {
                if (response.isSuccessful) {
                    return@withContext response.body?.bytes()
                }
                return@withContext null
            }
            
            if (response.isSuccessful) {
                val html = response.body?.string()
                if (html != null) {
                    val doc = Jsoup.parse(html)
                    val imgElement = doc.selectFirst("img.single-city-image")
                    
                    if (imgElement != null) {
                        var imgUrl = imgElement.attr("src")
                        if (!imgUrl.startsWith("http")) {
                            imgUrl = url + imgUrl
                        }
                        
                        val imgRequest = Request.Builder().url(imgUrl).build()
                        val imgResponse = client.newCall(imgRequest).execute()
                        
                        if (imgResponse.isSuccessful) {
                            return@withContext imgResponse.body?.bytes()
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}

fun parseClearCameras(json: String?): List<String> {
    return try {
        if (json.isNullOrBlank()) return emptyList()
        val jsonArray = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        list
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}
