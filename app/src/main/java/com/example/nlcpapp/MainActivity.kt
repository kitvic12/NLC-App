package com.example.nlcpapp
import androidx.compose.ui.platform.LocalContext

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nlcpapp.ui.screens.*
import com.example.nlcpapp.ui.theme.NLCAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
        val isDark = PreferencesManager.isDarkTheme(context)
        NLCAppTheme(darkTheme = isDark) {
                NlcApp()
            }
        }
    }
}

@Composable
fun NlcApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = androidx.compose.ui.platform.LocalContext.current as ComponentActivity
    
    val imageUrl = activity.intent?.getStringExtra("image_url")
    
    var showWelcome by remember { mutableStateOf(PreferencesManager.isFirstLaunch(context)) }
    var showNotifications by remember { mutableStateOf(!PreferencesManager.isNotificationsScreenShown(context)) }
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        PreferencesManager.setNotificationsScreenShown(context)
        PreferencesManager.setNotificationsEnabled(context, isGranted)
        showNotifications = false
    }
    
    if (!imageUrl.isNullOrEmpty() && !showWelcome && !showNotifications) {
        ImageScreen(imageUrl = "https://antcloud.ddns.net$imageUrl")
    } else {
        when {
            showWelcome -> {
                WelcomeScreen(
                    onContinueClick = {
                        PreferencesManager.setFirstLaunchCompleted(context)
                        showWelcome = false
                    }
                )
            }
            showNotifications -> {
                NotificationsScreen(
                    onYesClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            when {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                                    PreferencesManager.setNotificationsScreenShown(context)
                                    PreferencesManager.setNotificationsEnabled(context, true)
                                    showNotifications = false
                                }
                                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            PreferencesManager.setNotificationsScreenShown(context)
                            PreferencesManager.setNotificationsEnabled(context, true)
                            showNotifications = false
                        }
                    },
                    onNoClick = {
                        PreferencesManager.setNotificationsScreenShown(context)
                        PreferencesManager.setNotificationsEnabled(context, false)
                        showNotifications = false
                    }
                )
            }
            else -> MainNavigation()
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "menu"
    ) {
        composable("menu") {
            MenuScreen(
                onNavigateToCameras = { navController.navigate("cameras") },
                onNavigateToDatabase = { navController.navigate("database") },
                onNavigateToObservation = { navController.navigate("observation") },
                onNavigateToReport = { navController.navigate("report") },
                onNavigateToSupport = { navController.navigate("support") },
                onNavigateToReference = { navController.navigate("reference") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToNotifications = { navController.navigate("notifications_list") }
            )
        }
        
        composable("cameras") {
            CamerasScreen(onBackToMenu = { navController.popBackStack("menu", false) })
        }
        
        composable("database") {
            DatabaseScreen(
                onObservationClick = { date, user, place ->
                    navController.navigate("observation_detail/$date/$user/$place")
                },
                onNewObservationClick = { navController.navigate("observation") },
                onBackToMenu = { navController.popBackStack("menu", false) }
            )
        }
        
        composable("observation_detail/{date}/{user}/{place}") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val user = backStackEntry.arguments?.getString("user") ?: ""
            val place = backStackEntry.arguments?.getString("place") ?: ""
            ObservationDetailScreen(
                date = date, user = user, place = place,
                onBackToMenu = { navController.popBackStack("menu", false) }
            )
        }

        composable("observation") {
            ObservationScreen(
                onBackToMenu = { navController.popBackStack("menu", false) },
                onFinish = { navController.popBackStack("menu", false) }
            )
        }

        composable("report") {
            ReportScreen(onBackToMenu = { navController.popBackStack("menu", false) })
        }

        composable("support") {
            SupportScreen(onBackToMenu = { navController.popBackStack("menu", false) })
        }

        composable("reference") {
            ReferenceScreen(onBackToMenu = { navController.popBackStack("menu", false) })
        }

        composable("settings") {
            SettingsScreen(onBackToMenu = { navController.popBackStack("menu", false) })
        }

        composable("notifications_list") {
            NotificationsListScreen(onBackToMenu = { navController.popBackStack("menu", false) })
        }
    }
}
