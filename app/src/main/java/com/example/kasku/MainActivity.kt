package com.example.kasku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.example.kasku.ui.screens.splash.KasKuSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.kasku.data.local.KasKuDatabase
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.preferences.UserPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.ui.navigation.AppleFloatingBottomBar
import com.example.kasku.ui.navigation.IPhoneDynamicIslandBottomBar
import com.example.kasku.ui.navigation.TelegramLiquidGlassBottomBar
import com.example.kasku.ui.navigation.Screen
import com.example.kasku.ui.screens.chat.AiChatScreen
import com.example.kasku.ui.screens.dashboard.DashboardScreen
import com.example.kasku.ui.screens.insights.AiInsightsScreen
import com.example.kasku.ui.screens.onboarding.OnboardingDialog
import com.example.kasku.ui.screens.onboarding.OnboardingScreen
import com.example.kasku.ui.screens.scanner.ReceiptScannerScreen
import com.example.kasku.ui.screens.settings.SettingsScreen
import com.example.kasku.ui.screens.transactions.TransactionsScreen
import com.example.kasku.ui.theme.CeramicBackground
import com.example.kasku.ui.theme.KasKuTheme
import com.example.kasku.ui.theme.TokoGreen
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = KasKuDatabase.getDatabase(applicationContext)
        val repository = KasKuRepository(database)
        val aiService = AiService()
        val aiPreferences = AiPreferences(applicationContext)
        val userPreferences = UserPreferences(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            repository.ensureDefaultData()
        }

        setContent {
            KasKuTheme {
                val isOnboardingCompleted by userPreferences.isOnboardingCompletedFlow.collectAsState(initial = null)
                var isSplashShowing by remember { mutableStateOf(true) }

                AnimatedVisibility(
                    visible = isSplashShowing,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(450))
                ) {
                    KasKuSplashScreen(
                        onSplashFinished = { isSplashShowing = false }
                    )
                }

                if (!isSplashShowing) {
                    when (isOnboardingCompleted) {
                        null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CeramicBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = TokoGreen)
                            }
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                // Halaman utama aplikasi di latar belakang (blur jika onboarding belum selesai)
                                MainAppContainer(
                                    repository = repository,
                                    aiService = aiService,
                                    aiPreferences = aiPreferences,
                                    userPreferences = userPreferences,
                                    modifier = if (isOnboardingCompleted == false) {
                                        Modifier
                                            .fillMaxSize()
                                            .blur(20.dp)
                                    } else {
                                        Modifier.fillMaxSize()
                                    }
                                )

                                // Dialog Onboarding terpusat vertikal & horizontal
                                if (isOnboardingCompleted == false) {
                                    OnboardingDialog(
                                        userPreferences = userPreferences,
                                        repository = repository,
                                        onCompleted = { }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppContainer(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CeramicBackground),
        containerColor = CeramicBackground,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (!WindowInsets.isImeVisible && currentRoute != Screen.Scanner.route && currentRoute != Screen.AiChat.route) {
                TelegramLiquidGlassBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        if (screen.route == Screen.Dashboard.route) {
                            // Selalu kembali ke root Dashboard dengan aman tanpa memulihkan stack lama yang error
                            val popped = navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                            if (!popped) {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    repository = repository,
                    userPreferences = userPreferences,
                    aiService = aiService,
                    aiPreferences = aiPreferences,
                    onNavigateToScanner = { navController.navigate(Screen.Scanner.route) },
                    onNavigateToAiInsights = { navController.navigate(Screen.Insights.route) },
                    onQuickAddClick = { navController.navigate(Screen.Transactions.route) },
                    onManualAddClick = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAiChat = { navController.navigate(Screen.AiChat.route) }
                )
            }
            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    repository = repository,
                    aiService = aiService,
                    aiPreferences = aiPreferences
                )
            }
            composable(Screen.Scanner.route) {
                ReceiptScannerScreen(
                    repository = repository,
                    aiService = aiService,
                    aiPreferences = aiPreferences,
                    onCloseScanner = { navController.popBackStack() },
                    onTransactionSaved = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                        }
                    }
                )
            }
            composable(Screen.Insights.route) {
                AiInsightsScreen(
                    repository = repository,
                    aiService = aiService,
                    aiPreferences = aiPreferences
                )
            }
            composable(Screen.AiChat.route) {
                AiChatScreen(
                    repository = repository,
                    aiService = aiService,
                    aiPreferences = aiPreferences,
                    userPreferences = userPreferences,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    aiService = aiService,
                    aiPreferences = aiPreferences,
                    userPreferences = userPreferences,
                    onNavigateToDashboard = {
                        val popped = navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        if (!popped) {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
    }
}