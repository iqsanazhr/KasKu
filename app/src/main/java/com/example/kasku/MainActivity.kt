package com.example.kasku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainAppContainer(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CeramicBackground),
        containerColor = CeramicBackground,
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            if (!WindowInsets.isImeVisible && currentScreen != Screen.Scanner && currentScreen != Screen.AiChat) {
                AppleFloatingBottomBar(
                    currentRoute = currentScreen.route,
                    onNavigate = { currentScreen = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(280),
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.Dashboard -> DashboardScreen(
                        repository = repository,
                        userPreferences = userPreferences,
                        aiService = aiService,
                        aiPreferences = aiPreferences,
                        onNavigateToScanner = { currentScreen = Screen.Scanner },
                        onNavigateToAiInsights = { currentScreen = Screen.Insights },
                        onQuickAddClick = { currentScreen = Screen.Transactions },
                        onManualAddClick = { currentScreen = Screen.Transactions },
                        onNavigateToSettings = { currentScreen = Screen.Settings },
                        onNavigateToAiChat = { currentScreen = Screen.AiChat }
                    )
                    Screen.Transactions -> TransactionsScreen(
                        repository = repository,
                        aiService = aiService,
                        aiPreferences = aiPreferences
                    )
                    Screen.Scanner -> ReceiptScannerScreen(
                        repository = repository,
                        aiService = aiService,
                        aiPreferences = aiPreferences,
                        onCloseScanner = { currentScreen = Screen.Dashboard },
                        onTransactionSaved = { currentScreen = Screen.Dashboard }
                    )
                    Screen.Insights -> AiInsightsScreen(
                        repository = repository,
                        aiService = aiService,
                        aiPreferences = aiPreferences
                    )
                    Screen.AiChat -> AiChatScreen(
                        repository = repository,
                        aiService = aiService,
                        aiPreferences = aiPreferences,
                        onNavigateBack = { currentScreen = Screen.Dashboard }
                    )
                    Screen.Settings -> SettingsScreen(
                        repository = repository,
                        aiService = aiService,
                        aiPreferences = aiPreferences,
                        userPreferences = userPreferences,
                        onNavigateToDashboard = { currentScreen = Screen.Dashboard }
                    )
                }
            }
        }
    }
}