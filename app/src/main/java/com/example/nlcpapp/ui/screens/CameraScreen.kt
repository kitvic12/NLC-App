package com.example.nlcpapp.ui.screens

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class CameraLocation(
    val name: String,
    val url: String,
    val lat: Double,
    val lon: Double
)

class CameraViewModel : ViewModel() {
    var selectedCamera by mutableStateOf<CameraLocation?>(null)
    var imageBitmap by mutableStateOf<android.graphics.Bitmap?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var showOnlyClearCameras by mutableStateOf(false)
    var clearCameras by mutableStateOf<List<CameraLocation>>(emptyList())
    
    fun selectCamera(camera: CameraLocation) {
        selectedCamera = camera
        loadImage()
    }
    
    fun loadImage() {
        selectedCamera?.let { camera ->
            viewModelScope.launch {
                isLoading = true
                errorMessage = null
                imageBitmap = null
                
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        downloadImage(camera.url, camera.name)
                    }
                    if (bitmap != null) {
                        imageBitmap = bitmap
                    } else {
                        errorMessage = "Не удалось загрузить изображение"
                    }
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Exception: ${e.message}", e)
                    errorMessage = "Ошибка: ${e.message}"
                }
                
                isLoading = false
            }
        }
    }
    
    private suspend fun downloadImage(urlString: String, cameraName: String): android.graphics.Bitmap? {
        return try {
            Log.d("CameraViewModel", "=== Starting download for $cameraName ===")
            Log.d("CameraViewModel", "URL: $urlString")
            
            if (cameraName == "Тырнауз") {
                Log.d("CameraViewModel", "Loading Тырнауз directly")
                loadImageFromUrl(urlString)
            } else {
                Log.d("CameraViewModel", "Loading HTML page")
                val html = loadHtml(urlString)
                if (html == null) {
                    Log.e("CameraViewModel", "Failed to load HTML")
                    return null
                }
                
                Log.d("CameraViewModel", "HTML loaded, length: ${html.length}")
                val imageUrl = extractImageUrl(html, urlString)
                Log.d("CameraViewModel", "Extracted image URL: $imageUrl")
                
                if (imageUrl == null) {
                    Log.e("CameraViewModel", "Could not find image URL in HTML")
                    null
                } else {
                    loadImageFromUrl(imageUrl)
                }
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Download failed: ${e.message}", e)
            null
        }
    }
    
    private suspend fun loadHtml(urlString: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 25000
            connection.readTimeout = 25000
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            connection.connect()
            
            val responseCode = connection.responseCode
            Log.d("CameraViewModel", "HTML Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use {
                    it.readText()
                }
            } else {
                Log.e("CameraViewModel", "HTTP error: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Load HTML failed: ${e.message}", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
    
    private fun extractImageUrl(html: String, baseUrl: String): String? {
        try {
            val regex = Regex("""<img[^>]*class=["'][^"']*single-city-image[^"']*["'][^>]*src=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            val match = regex.find(html)
            val src = match?.groups?.get(1)?.value
            
            if (src != null) {
                return if (src.startsWith("http")) src else baseUrl + src
            }
            
            val regex2 = Regex("""<img[^>]*src=["']([^"']+)["'][^>]*class=["'][^"']*single-city-image""", RegexOption.IGNORE_CASE)
            val match2 = regex2.find(html)
            val src2 = match2?.groups?.get(1)?.value
            
            return if (src2 != null) {
                if (src2.startsWith("http")) src2 else baseUrl + src2
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Regex failed: ${e.message}")
            return null
        }
    }
    
    private suspend fun loadImageFromUrl(urlString: String): android.graphics.Bitmap? {
        var connection: HttpURLConnection? = null
        return try {
            Log.d("CameraViewModel", "Loading image from: $urlString")
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 25000
            connection.readTimeout = 25000
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            connection.connect()
            
            val responseCode = connection.responseCode
            Log.d("CameraViewModel", "Image Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                if (bitmap != null) {
                    Log.d("CameraViewModel", "Image loaded successfully: ${bitmap.width}x${bitmap.height}")
                } else {
                    Log.e("CameraViewModel", "BitmapFactory returned null")
                }
                bitmap
            } else {
                Log.e("CameraViewModel", "Image HTTP error: $responseCode")
                null
            }
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Load image failed: ${e.message}", e)
            null
        } finally {
            connection?.disconnect()
        }
    }
}

val allCameras = listOf(
    CameraLocation("Калуга", "https://starvisor.ru/klg/", 54.0, 36.0),
    CameraLocation("Москва", "https://starvisor.ru/msc/", 55.0, 37.0),
    CameraLocation("Попово", "https://starvisor.ru/ppv/", 57.0, 38.0),
    CameraLocation("Рязань", "https://starvisor.ru/rzn/", 54.0, 40.0),
    CameraLocation("Тула", "https://starvisor.ru/tula/", 54.0, 37.0),
    CameraLocation("Уткино", "https://starvisor.ru/utk/", 57.0, 40.0),
    CameraLocation("Ярославль", "https://starvisor.ru/yar/", 57.0, 39.0),
    CameraLocation("Васкелово", "https://starvisor.ru/spbd/", 60.0, 30.0),
    CameraLocation("Вологда", "https://starvisor.ru/vlg/", 59.0, 39.0),
    CameraLocation("Калининград", "https://starvisor.ru/kln/", 54.0, 20.0),
    CameraLocation("Русское", "https://starvisor.ru/rus/", 47.0, 38.0),
    CameraLocation("Гулькевичи", "https://starvisor.ru/gul/", 45.0, 40.0),
    CameraLocation("Краснодар", "https://starvisor.ru/nov/", 45.0, 38.0),
    CameraLocation("Тырнауз", "https://gw.cmo.sai.msu.ru/webcam6.jpg", 42.0, 43.0),
    CameraLocation("Азево", "https://starvisor.ru/azv/", 56.0, 53.0),
    CameraLocation("Альметьевск", "https://starvisor.ru/almet/", 54.0, 52.0),
    CameraLocation("Березники", "https://starvisor.ru/brz/", 59.0, 57.0),
    CameraLocation("Ирбит", "https://starvisor.ru/irb/", 57.0, 63.0),
    CameraLocation("Каменск-Уральский", "https://starvisor.ru/kur/", 56.0, 61.0),
    CameraLocation("Пермь", "https://starvisor.ru/prm/", 58.0, 56.0),
    CameraLocation("Остроленский", "https://starvisor.ru/ost/", 53.0, 59.0),
    CameraLocation("Челябинск", "https://starvisor.ru/chb/", 55.0, 61.0),
    CameraLocation("Айхал", "https://starvisor.ru/ayk/", 65.0, 111.0),
    CameraLocation("Багдарин", "https://starvisor.ru/bag/", 54.0, 113.0),
    CameraLocation("Стрежевой", "https://starvisor.ru/str/", 61.0, 77.0),
    CameraLocation("Юрга", "https://starvisor.ru/yur/", 55.0, 84.0),
    CameraLocation("Пятиречье", "https://starvisor.ru/ptr/", 60.0, 30.0),
    CameraLocation("Провидения", "https://starvisor.ru/prv/", 64.0, -173.0)
)

val regions = mapOf(
    "ЦФО" to listOf("Калуга", "Москва", "Попово", "Рязань", "Тула", "Уткино", "Ярославль"),
    "Северо-Запад(СЗФО)" to listOf("Васкелово", "Вологда", "Калининград", "Русское"),
    "Юг России" to listOf("Гулькевичи", "Краснодар", "Тырнауз"),
    "Поволжье" to listOf("Азево", "Альметьевск", "Березники", "Ирбит", "Каменск-Уральский", "Пермь"),
    "Урал" to listOf("Остроленский", "Челябинск"),
    "Сибирь" to listOf("Айхал", "Багдарин", "Стрежевой", "Юрга"),
    "Дальний Восток" to listOf("Пятиречье", "Провидения")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Камеры Starvisor") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
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
            if (viewModel.selectedCamera != null) {
                CameraImageScreen(viewModel)
            } else {
                CameraListScreen(viewModel)
            }
        }
    }
}

@Composable
fun CameraListScreen(viewModel: CameraViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = viewModel.showOnlyClearCameras,
                onCheckedChange = { viewModel.showOnlyClearCameras = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Только камеры без облачности",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        if (viewModel.showOnlyClearCameras) {
            if (viewModel.clearCameras.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "На данный момент камеры без облачности отсутствуют",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.clearCameras) { camera ->
                        Card(
                            onClick = { viewModel.selectCamera(camera) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = camera.name,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                regions.forEach { (region, cameraNames) ->
                    item {
                        ExpandableRegionCard(
                            region = region,
                            cameraNames = cameraNames,
                            onSelectCamera = { cameraName ->
                                val camera = allCameras.find { it.name == cameraName }
                                camera?.let { viewModel.selectCamera(it) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableRegionCard(
    region: String,
    cameraNames: List<String>,
    onSelectCamera: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                expanded = !expanded
            }
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = region,
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Свернуть" else "Развернуть"
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    cameraNames.forEach { cameraName ->
                        TextButton(
                            onClick = { onSelectCamera(cameraName) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = cameraName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraImageScreen(viewModel: CameraViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.selectedCamera?.name ?: "",
                style = MaterialTheme.typography.titleLarge
            )
            
            TextButton(onClick = { 
                viewModel.selectedCamera = null
                viewModel.imageBitmap = null
            }) {
                Text("Назад")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Загрузка изображения...")
                }
            }
        } else {
            viewModel.imageBitmap?.let { bitmap ->
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "Camera image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Fit
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.errorMessage ?: "Неизвестная ошибка",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Проверьте логи: adb logcat | grep CameraViewModel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
