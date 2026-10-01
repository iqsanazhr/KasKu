package com.example.kasku.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.preferences.UserPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.AiProvider
import com.example.kasku.ui.components.WalletManagerSheet
import com.example.kasku.ui.components.formatRupiah
import com.example.kasku.ui.theme.MonzoAvatarBg
import com.example.kasku.ui.theme.MonzoBackground
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoCoralPillLight
import com.example.kasku.ui.theme.MonzoElevated
import com.example.kasku.ui.theme.MonzoExpenseRed
import com.example.kasku.ui.theme.MonzoIncomeGreen
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTealBorder
import com.example.kasku.ui.theme.MonzoTealLight
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import com.example.kasku.ui.theme.MonzoTextTertiary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier,
    onNavigateToDashboard: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val accounts by repository.getAllAccounts().collectAsState(initial = emptyList())
    val savedUserName by userPreferences.userNameFlow.collectAsState(initial = "")
    var userNameInput by remember(savedUserName) { mutableStateOf(savedUserName) }
    var isNameSaved by remember { mutableStateOf(false) }

    val aiProvider by aiPreferences.aiProviderFlow.collectAsState(initial = AiProvider.GEMINI)
    val savedGeminiKey by aiPreferences.geminiApiKeyFlow.collectAsState(initial = "")
    val savedGeminiModel by aiPreferences.geminiModelFlow.collectAsState(initial = "gemini-3.5-flash-lite")
    val savedLmUrl by aiPreferences.lmStudioUrlFlow.collectAsState(initial = "http://10.0.2.2:1234/v1")
    val savedLmModel by aiPreferences.lmStudioModelFlow.collectAsState(initial = "gemma-3-4b-it")

    var currentProvider by remember(aiProvider) { mutableStateOf(aiProvider) }
    var geminiKeyInput by remember(savedGeminiKey) { mutableStateOf(savedGeminiKey) }
    var geminiModelInput by remember(savedGeminiModel) { mutableStateOf(savedGeminiModel) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    var lmUrlInput by remember(savedLmUrl) { mutableStateOf(savedLmUrl) }
    var lmModelInput by remember(savedLmModel) { mutableStateOf(savedLmModel) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }
    var showWalletManager by remember { mutableStateOf(false) }
    var showResetOnboardingSnackbar by remember { mutableStateOf(false) }

    if (showWalletManager) {
        WalletManagerSheet(
            repository = repository,
            onDismiss = { showWalletManager = false }
        )
    }

    val userInitial = (userNameInput.ifBlank { savedUserName }).trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MonzoBackground)
            .padding(horizontal = 16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ==========================================
        // 1. TOP HEADER (CLEAN MONZO - NO BACK BUTTON)
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Column {
                Text(
                    text = "Akun & Preferensi",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MonzoTextSecondary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonzoTextPrimary,
                        fontSize = 23.sp
                    )
                )
            }
        }

        // ==========================================
        // 2. KARTU PROFIL PENGGUNA
        // ==========================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x08000000),
                        spotColor = Color(0x10000000)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MonzoSurface,
                border = BorderStroke(1.dp, MonzoBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = MonzoAvatarBg
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = userInitial,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 19.sp
                                    )
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Profil Pengguna",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "Nama yang disapa di Beranda KasKu",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userNameInput,
                            onValueChange = {
                                userNameInput = it
                                isNameSaved = false
                            },
                            placeholder = { Text("Masukkan nama panggilan...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MonzoSurface,
                                unfocusedContainerColor = MonzoSurface,
                                focusedBorderColor = MonzoTeal,
                                unfocusedBorderColor = MonzoBorder,
                                focusedTextColor = MonzoTextPrimary,
                                unfocusedTextColor = MonzoTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            modifier = Modifier
                                .height(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    if (userNameInput.isNotBlank()) {
                                        scope.launch {
                                            userPreferences.saveUserName(userNameInput)
                                            isNameSaved = true
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isNameSaved) MonzoTealLight else MonzoTeal
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isNameSaved) "Tersimpan" else "Simpan",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isNameSaved) MonzoTeal else Color.White,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. KARTU KELOLA DOMPET & AKUN
        // ==========================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x08000000),
                        spotColor = Color(0x10000000)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MonzoSurface,
                border = BorderStroke(1.dp, MonzoBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Dompet & Sumber Dana",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "${accounts.size} dompet terdaftar",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MonzoTealLight,
                            border = BorderStroke(1.dp, MonzoTealBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showWalletManager = true }
                        ) {
                            Text(
                                text = "Kelola Dompet",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonzoTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (accounts.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MonzoElevated
                        ) {
                            Text(
                                text = "Belum ada dompet terdaftar. Ketuk \"Kelola Dompet\" untuk menambahkan rekening atau uang tunai.",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            accounts.forEach { acc ->
                                val isNegative = acc.balance < 0.0
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showWalletManager = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isNegative) MonzoCoralPillLight else MonzoElevated,
                                    border = BorderStroke(0.5.dp, if (isNegative) MonzoCoral.copy(alpha = 0.4f) else MonzoBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = when (acc.type) {
                                                    "BANK" -> Icons.Filled.AccountBalance
                                                    "E_WALLET" -> Icons.Filled.AccountBalanceWallet
                                                    else -> Icons.Filled.Money
                                                },
                                                contentDescription = null,
                                                tint = if (isNegative) MonzoExpenseRed else MonzoTeal,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = acc.name,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MonzoTextPrimary,
                                                        fontSize = 14.sp
                                                    )
                                                )
                                                if (isNegative) {
                                                    Text(
                                                        text = "Saldo minus • Ketuk untuk sesuaikan",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = MonzoExpenseRed,
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = formatRupiah(acc.balance),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isNegative) MonzoExpenseRed else MonzoTextPrimary,
                                                fontSize = 14.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. KARTU DUAL-ENGINE AI CONFIGURATION
        // ==========================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x08000000),
                        spotColor = Color(0x10000000)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MonzoSurface,
                border = BorderStroke(1.dp, MonzoBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(34.dp),
                            shape = CircleShape,
                            color = MonzoTealLight
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MonzoTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Konfigurasi KasKu AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "Mesin AI untuk scan struk & asisten finansial",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // Switcher Provider AI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AiProvider.GEMINI to "Google Gemini",
                            AiProvider.LM_STUDIO to "LM Studio (Lokal)"
                        ).forEach { (provider, label) ->
                            val isSelected = currentProvider == provider
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        currentProvider = provider
                                        scope.launch { aiPreferences.saveAiProvider(provider) }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MonzoTeal else MonzoElevated,
                                border = BorderStroke(1.dp, if (isSelected) MonzoTeal else MonzoBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MonzoTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (currentProvider == AiProvider.GEMINI) {
                        // Gemini API Key & Model Form
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = geminiKeyInput,
                                onValueChange = {
                                    geminiKeyInput = it
                                    scope.launch { aiPreferences.saveGeminiConfig(it, geminiModelInput) }
                                },
                                label = { Text("Gemini API Key", fontSize = 12.sp) },
                                placeholder = { Text("AIzaSy...") },
                                visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                        Icon(
                                            imageVector = if (isApiKeyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = null,
                                            tint = MonzoTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MonzoSurface,
                                    unfocusedContainerColor = MonzoSurface,
                                    focusedBorderColor = MonzoTeal,
                                    unfocusedBorderColor = MonzoBorder
                                ),
                                singleLine = true
                            )

                            // Pilihan Model Gemini Cepat
                            Text(
                                text = "Pilihan Model:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 11.5.sp
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "gemini-3.5-flash-lite" to "⚡ 3.5 Flash Lite (Rekomendasi)",
                                    "gemini-2.5-flash" to "2.5 Flash"
                                ).forEach { (modelId, label) ->
                                    val isSelected = geminiModelInput == modelId
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                geminiModelInput = modelId
                                                scope.launch { aiPreferences.saveGeminiConfig(geminiKeyInput, modelId) }
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MonzoTealLight else MonzoElevated,
                                        border = BorderStroke(1.dp, if (isSelected) MonzoTeal else MonzoBorder)
                                    ) {
                                        Text(
                                            text = label,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) MonzoTeal else MonzoTextSecondary,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 10.5.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // LM Studio Form
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = lmUrlInput,
                                onValueChange = {
                                    lmUrlInput = it
                                    scope.launch { aiPreferences.saveLmStudioConfig(it, lmModelInput) }
                                },
                                label = { Text("URL Endpoint Server (cth: http://10.0.2.2:1234/v1)", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MonzoSurface,
                                    unfocusedContainerColor = MonzoSurface,
                                    focusedBorderColor = MonzoTeal,
                                    unfocusedBorderColor = MonzoBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = lmModelInput,
                                onValueChange = {
                                    lmModelInput = it
                                    scope.launch { aiPreferences.saveLmStudioConfig(lmUrlInput, it) }
                                },
                                label = { Text("Nama Model (cth: gemma-3-4b-it)", fontSize = 12.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MonzoSurface,
                                    unfocusedContainerColor = MonzoSurface,
                                    focusedBorderColor = MonzoTeal,
                                    unfocusedBorderColor = MonzoBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Tombol Uji Koneksi AI
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isTestingConnection) {
                                scope.launch {
                                    isTestingConnection = true
                                    testResult = null
                                    try {
                                        val res = aiService.testConnection(
                                            provider = currentProvider,
                                            geminiApiKey = geminiKeyInput,
                                            geminiModel = geminiModelInput,
                                            lmStudioUrl = lmUrlInput,
                                            lmStudioModel = lmModelInput
                                        )
                                        res.onSuccess {
                                            isTestSuccess = true
                                            testResult = it
                                        }.onFailure {
                                            isTestSuccess = false
                                            testResult = "Gagal terhubung: ${it.localizedMessage}"
                                        }
                                    } catch (e: Exception) {
                                        isTestSuccess = false
                                        testResult = "Error: ${e.localizedMessage}"
                                    } finally {
                                        isTestingConnection = false
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MonzoElevated,
                        border = BorderStroke(1.dp, MonzoBorder)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MonzoTeal,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Menguji Koneksi...",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MonzoTeal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MonzoTeal,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Uji Koneksi AI (Ping)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = MonzoTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }
                        }
                    }

                    // Banner Hasil Uji Koneksi
                    testResult?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isTestSuccess) Color(0xFFE8F7F0) else MonzoCoralPillLight,
                            border = BorderStroke(
                                1.dp,
                                if (isTestSuccess) MonzoIncomeGreen.copy(alpha = 0.4f) else MonzoExpenseRed.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTestSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = if (isTestSuccess) MonzoIncomeGreen else MonzoExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isTestSuccess) MonzoIncomeGreen else MonzoExpenseRed,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. KARTU INFORMASI APLIKASI & RESET ONBOARDING
        // ==========================================
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color(0x08000000),
                        spotColor = Color(0x10000000)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = MonzoSurface,
                border = BorderStroke(1.dp, MonzoBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KasKu Keuangan",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = "Versi 1.0 • Monzo-Inspired Edition",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MonzoTealLight,
                                border = BorderStroke(0.8.dp, MonzoTeal.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        scope.launch {
                                            userPreferences.resetTutorial()
                                            onNavigateToDashboard()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = MonzoTeal,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Mulai Tutorial",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MonzoTeal,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MonzoElevated,
                                border = BorderStroke(0.5.dp, MonzoBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        scope.launch {
                                            userPreferences.resetOnboarding()
                                            showResetOnboardingSnackbar = true
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = MonzoTextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Onboarding",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MonzoTextSecondary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (showResetOnboardingSnackbar) {
                        Text(
                            text = "Onboarding direset! Saat aplikasi dibuka ulang atau kembali ke Beranda, modal penyiapan awal akan muncul kembali.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MonzoTeal,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
