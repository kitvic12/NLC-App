package com.example.nlcpapp.ui.screens

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(
    imageUrl: String,
    onBackClick: () -> Unit = {}
) {
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("ImageScreen", "Loading image from: $imageUrl")
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connectTimeout = 25000
                connection.readTimeout = 25000
                connection.connect()
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    bitmap = BitmapFactory.decodeStream(connection.inputStream)
                    Log.d("ImageScreen", "Image loaded successfully")
                } else {
                    errorMessage = "Ошибка загрузки: ${connection.responseCode}"
                    Log.e("ImageScreen", "HTTP error: ${connection.responseCode}")
                }
                connection.disconnect()
            } catch (e: Exception) {
                errorMessage = "Ошибка: ${e.message}"
                Log.e("ImageScreen", "Exception: ${e.message}", e)
            }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Изображение") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                bitmap?.let { bmp ->
                    Image(
                        painter = BitmapPainter(bmp.asImageBitmap()),
                        contentDescription = "Image from notification",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
