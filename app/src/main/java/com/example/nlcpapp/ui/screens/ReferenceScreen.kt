package com.example.nlcpapp.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nlcpapp.ui.components.ZoomableImageDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceScreen(onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    var selectedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Справочная информация", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Серебристые облака - это сравнительно редкое атмосферное явление, крайне разрежённые облака, состоящие из мелких льдинок, которые подсвечивает солнце. Возникают эти облака в мезосфере под мезопаузой (на высоте 76—85 км над поверхностью Земли) и видимые в глубоких сумерках, непосредственно после заката или перед восходом Солнца.",
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Параметры СО:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ExpandableSection(title = "Типы СО") {
                Text("Флер", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Структура, не имеющая определенных очертаний, которые иногда отражают фоном другие облачные формы. Флер похож на однородную или неоднородную пелену. Довольно часто, спустя примерно полчаса после появления флера, наблюдаются серебристые облака с более развитой структурой. Далее флер сочетается с другими формами.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ClickableImage("reference/fler.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Полосы", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Тип 2А - размытые полосы. Состоят из линий с нечеткими краями.", fontSize = 14.sp)
                Text("Тип 2Б - резко-очерченные. Состоят из линий с хорошо определенными краями. Полосы могут менять яркость всей структуры в течение 20-60 минут.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ClickableImage("reference/str.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Волны", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Гребешки (III-a) – участки с частым расположением узких, резко очерченных параллельных полос, наподобие легкой ряби на поверхности воды при небольшом порыве ветра.", fontSize = 14.sp)
                Text("Гребни (III-b) – имеют более заметные признаки волновой природы; расстояние между соседними гребнями в 10–20 раз больше, чем у гребешков.", fontSize = 14.sp)
                Text("Волнообразные изгибы (III-c) образуются в результате искривления поверхности облаков, занятой другими формами (полосами, гребешками).", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ClickableImage("reference/waves.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Вихри", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Частичные или, в редких случаях, полные кольца облаков с темной серединой. Иногда можно наблюдать во флёре, полосах и волнах. Выделяют три группы:", fontSize = 14.sp)
                Text("4a) Завихрения и круглые просветы радиусом 0.1° — 0.5°.", fontSize = 14.sp)
                Text("4b) Завихрение в виде простого изгиба одной или нескольких полос в сторону от основного направления с радиусом 3°–5°.", fontSize = 14.sp)
                Text("4c) Мощные вихревые выбросы в сторону от основного облака.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                ClickableImage("reference/winds.png") { selectedBitmap = it }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ExpandableSection(title = "Яркость") {
                Text("Яркость серебристых облаков - один из важнейших параметров который нужно записывать при наблюдениях.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Индекс яркости 1", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Очень слабые серебристые облака, едва видимые на фоне сумеречного неба; определимы только при очень тщательном изучении неба", fontSize = 14.sp)
                ClickableImage("reference/light_1.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Индекс яркости 2", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Серебристые облака легко определимы, но имеют низкую яркость.", fontSize = 14.sp)
                ClickableImage("reference/light_2.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Индекс яркости 3", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Серебристые облака хорошо видны, четко выделяясь на фоне сумеречного неба.", fontSize = 14.sp)
                ClickableImage("reference/light_3.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Индекс яркости 4", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Серебристые облака очень яркие и привлекают внимание наблюдателей-неспециалистов.", fontSize = 14.sp)
                ClickableImage("reference/light_4.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Индекс яркости 5", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Серебристые облака крайне яркие и заметно освещают повернутые к ним предметы.", fontSize = 14.sp)
                ClickableImage("reference/light_5.png") { selectedBitmap = it }
            }
            
            ExpandableSection(title = "Интенсивность") {
                Text("Интенсивность серебристых облаков определяется по 10-балльной шкале на основе:", fontSize = 14.sp)
                Text("A – отношение площади СО к сумеречному сегменту неба.", fontSize = 14.sp)
                Text("B – яркость СО по 5-балльной шкале.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                ClickableImage("reference/intensity.png") { selectedBitmap = it }
            }
            
            ExpandableSection(title = "Площадь") {
                Text("Руководство по определению площади СО на небе.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                ClickableImage("reference/area.png") { selectedBitmap = it }
            }
            
            ExpandableSection(title = "Погода") {
                Text("А – облаков нет или до 20%.", fontSize = 14.sp)
                ClickableImage("reference/weather_1.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Б - сегмент закрыт от 20 до 50%.", fontSize = 14.sp)
                ClickableImage("reference/weather_2.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("В - сегмент закрыт на 50–80%.", fontSize = 14.sp)
                ClickableImage("reference/weather_3.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Г – сегмент закрыт от 80 до 95%.", fontSize = 14.sp)
                ClickableImage("reference/weather_4.png") { selectedBitmap = it }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Д – сегмент закрыт на 95–100%.", fontSize = 14.sp)
                ClickableImage("reference/weather_5.png") { selectedBitmap = it }
            }
        }
    }
    
    selectedBitmap?.let { bitmap ->
        ZoomableImageDialog(
            bitmap = bitmap,
            onDismiss = { selectedBitmap = null }
        )
    }
}

@Composable
fun ClickableImage(path: String, onClick: (android.graphics.Bitmap) -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(path) {
        try {
            context.assets.open(path).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Нажмите для увеличения",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .clickable { onClick(bitmap) },
            contentScale = ContentScale.Fit
        )
    } else {
        Text("Изображение не найдено: $path", color = MaterialTheme.colorScheme.error)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(if (expanded) "▼" else "▶", fontSize = 16.sp)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}
