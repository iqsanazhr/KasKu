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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
        // 2B. KARTU PILIHAN MATA UANG APLIKASI
        // ==========================================
        item {
            val selectedCurrency by userPreferences.selectedCurrencyFlow.collectAsState(initial = "IDR")
            var isCurrencyDropdownExpanded by remember { mutableStateOf(false) }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = CircleShape,
                                color = MonzoTeal.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Money,
                                        contentDescription = null,
                                        tint = MonzoTeal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Mata Uang Aplikasi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoTextPrimary,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = "Pilih mata uang utama untuk seluruh transaksi",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }

                    // Selector Dropdown Box
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentCurrencyInfo = com.example.kasku.ui.components.CurrencyConfig.supportedCurrencies
                            .firstOrNull { it.first == selectedCurrency }
                        val currentLabel = currentCurrencyInfo?.let { "${it.second.first} (${it.first} - ${it.second.second})" }
                            ?: "$selectedCurrency (${com.example.kasku.ui.components.CurrencyConfig.getSymbol(selectedCurrency)})"

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { isCurrencyDropdownExpanded = true },
                            shape = RoundedCornerShape(14.dp),
                            color = MonzoElevated,
                            border = BorderStroke(1.dp, if (isCurrencyDropdownExpanded) MonzoTeal else MonzoBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentLabel,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MonzoTextPrimary,
                                            fontSize = 13.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Simbol Aktif: ${com.example.kasku.ui.components.CurrencyConfig.getSymbol(selectedCurrency)}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MonzoTeal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = if (isCurrencyDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                    contentDescription = "Pilih Mata Uang",
                                    tint = MonzoTextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isCurrencyDropdownExpanded,
                            onDismissRequest = { isCurrencyDropdownExpanded = false },
                            modifier = Modifier
                                .background(MonzoSurface)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            com.example.kasku.ui.components.CurrencyConfig.supportedCurrencies.forEach { (code, pair) ->
                                val (name, symbol) = pair
                                val isSelected = selectedCurrency == code
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                Text(
                                                    text = "$name ($code)",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) MonzoTeal else MonzoTextPrimary,
                                                        fontSize = 13.sp
                                                    )
                                                )
                                                Text(
                                                    text = "Simbol: $symbol",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = MonzoTextSecondary,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MonzoTeal,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        isCurrencyDropdownExpanded = false
                                        com.example.kasku.ui.components.CurrencyConfig.currentCurrency = code
                                        scope.launch { userPreferences.saveSelectedCurrency(code) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2C. KARTU KURS MATA UANG (RETROFIT REST API & UISTATE)
        // ==========================================
        item {
            com.example.kasku.ui.screens.currency.CurrencyExchangeCard()
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

                            var isFetchingModels by remember { mutableStateOf(false) }
                            var fetchedGeminiModels by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
                            var fetchModelStatusMessage by remember { mutableStateOf<String?>(null) }
                            var fetchModelError by remember { mutableStateOf<String?>(null) }

                            // Tombol Cek Izin & Sinkronisasi Model Otomatis dari API Key
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = {
                                        if (geminiKeyInput.isNotBlank()) {
                                            isFetchingModels = true
                                            fetchModelStatusMessage = null
                                            fetchModelError = null
                                            scope.launch {
                                                val res = aiService.fetchAvailableGeminiModels(geminiKeyInput)
                                                res.onSuccess { models ->
                                                    fetchedGeminiModels = models
                                                    fetchModelStatusMessage = "Sukses: ${models.size} model aktif terverifikasi dari Google API."
                                                }.onFailure { err ->
                                                    fetchModelError = "Gagal memuat: ${err.localizedMessage}"
                                                }
                                                isFetchingModels = false
                                            }
                                        }
                                    },
                                    enabled = !isFetchingModels && geminiKeyInput.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = MonzoTeal
                                    ),
                                    border = BorderStroke(1.dp, if (geminiKeyInput.isNotBlank()) MonzoTeal.copy(alpha = 0.5f) else MonzoBorder)
                                ) {
                                    if (isFetchingModels) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = MonzoTeal,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Memeriksa API...", fontSize = 11.5.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Cek Izin & Sinkron Model", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                if (fetchedGeminiModels.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MonzoIncomeGreen.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${fetchedGeminiModels.size} Model Terhubung",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MonzoIncomeGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }

                            if (fetchModelStatusMessage != null) {
                                Text(
                                    text = fetchModelStatusMessage!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoIncomeGreen,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                            if (fetchModelError != null) {
                                Text(
                                    text = fetchModelError!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoExpenseRed,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }

                            // Pilihan Model Gemini (Dropdown Dinamis + Kustom)
                            Text(
                                text = if (fetchedGeminiModels.isNotEmpty()) "Pilihan Model (Dinamis dari API):" else "Pilihan Model Gemini:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 11.5.sp
                                )
                            )

                            var isModelDropdownExpanded by remember { mutableStateOf(false) }
                            var showCustomModelDialog by remember { mutableStateOf(false) }
                            val defaultGeminiModels = remember {
                                listOf(
                                    "gemini-3.8-flash" to "Gemini 3.8 Flash (Ultra Cepat & Mutakhir)",
                                    "gemini-3.6-flash" to "Gemini 3.6 Flash (Cepat & Akurat)",
                                    "gemini-3.5-flash" to "Gemini 3.5 Flash (Cerdas & Responsif)",
                                    "gemini-3.5-flash-lite" to "Gemini 3.5 Flash Lite (Hemat Kuota)",
                                    "gemini-2.5-flash" to "Gemini 2.5 Flash (Rekomendasi Cepat)",
                                    "gemini-2.5-flash-lite" to "Gemini 2.5 Flash Lite (Ringan & Hemat Kuota)",
                                    "gemini-2.5-pro" to "Gemini 2.5 Pro (Penalaran Kompleks)",
                                    "gemini-1.5-flash" to "Gemini 1.5 Flash (Stabil)",
                                    "gemini-1.5-pro" to "Gemini 1.5 Pro (Analisis Mendalam)"
                                )
                            }
                            val geminiModelOptions = if (fetchedGeminiModels.isNotEmpty()) fetchedGeminiModels else defaultGeminiModels
                            val selectedModelLabel = geminiModelOptions.firstOrNull { it.first == geminiModelInput }?.second
                                ?: geminiModelInput.ifBlank { "Gemini 3.8 Flash (Ultra Cepat & Mutakhir)" }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { isModelDropdownExpanded = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MonzoElevated,
                                    border = BorderStroke(1.dp, if (isModelDropdownExpanded) MonzoTeal else MonzoBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = selectedModelLabel,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MonzoTextPrimary,
                                                    fontSize = 13.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "ID: ${geminiModelInput.ifBlank { "gemini-3.8-flash" }}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MonzoTeal,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isModelDropdownExpanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                                            contentDescription = "Pilih Model",
                                            tint = MonzoTextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isModelDropdownExpanded,
                                    onDismissRequest = { isModelDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(MonzoSurface)
                                        .clip(RoundedCornerShape(14.dp))
                                ) {
                                    geminiModelOptions.forEach { (modelId, label) ->
                                        val isSelected = geminiModelInput == modelId
                                        DropdownMenuItem(
                                            text = {
                                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (isSelected) MonzoTeal else MonzoTextPrimary,
                                                            fontSize = 13.sp
                                                        )
                                                    )
                                                    Text(
                                                        text = modelId,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = MonzoTextSecondary,
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            },
                                            onClick = {
                                                geminiModelInput = modelId
                                                isModelDropdownExpanded = false
                                                scope.launch { aiPreferences.saveGeminiConfig(geminiKeyInput, modelId) }
                                            }
                                        )
                                    }

                                    androidx.compose.material3.HorizontalDivider(
                                        color = MonzoBorder,
                                        thickness = 1.dp
                                    )

                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = MonzoTeal,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Kustom / Masukkan Model ID Lainnya...",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Medium,
                                                        color = MonzoTeal,
                                                        fontSize = 12.5.sp
                                                    )
                                                )
                                            }
                                        },
                                        onClick = {
                                            isModelDropdownExpanded = false
                                            showCustomModelDialog = true
                                        }
                                    )
                                }
                            }

                            if (showCustomModelDialog) {
                                var tempModelInput by remember { mutableStateOf(geminiModelInput) }
                                androidx.compose.material3.AlertDialog(
                                    onDismissRequest = { showCustomModelDialog = false },
                                    title = {
                                        Text(
                                            text = "Input Model Gemini Kustom",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MonzoTextPrimary
                                            )
                                        )
                                    },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(
                                                text = "Ketikkan ID model Google Gemini yang ingin digunakan (contoh: gemini-3.8-flash, gemini-3.6-flash, gemini-exp-1206):",
                                                style = MaterialTheme.typography.bodySmall.copy(color = MonzoTextSecondary)
                                            )
                                            OutlinedTextField(
                                                value = tempModelInput,
                                                onValueChange = { tempModelInput = it },
                                                singleLine = true,
                                                placeholder = { Text("gemini-3.8-flash") },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MonzoTeal,
                                                    unfocusedBorderColor = MonzoBorder,
                                                    focusedTextColor = MonzoTextPrimary,
                                                    unfocusedTextColor = MonzoTextPrimary,
                                                    cursorColor = MonzoTeal
                                                )
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        androidx.compose.material3.TextButton(
                                            onClick = {
                                                val clean = tempModelInput.trim()
                                                if (clean.isNotBlank()) {
                                                    geminiModelInput = clean
                                                    scope.launch { aiPreferences.saveGeminiConfig(geminiKeyInput, clean) }
                                                }
                                                showCustomModelDialog = false
                                            }
                                        ) {
                                            Text("Terapkan", color = MonzoTeal, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    dismissButton = {
                                        androidx.compose.material3.TextButton(onClick = { showCustomModelDialog = false }) {
                                            Text("Batal", color = MonzoTextSecondary)
                                        }
                                    },
                                    containerColor = MonzoSurface,
                                    shape = RoundedCornerShape(20.dp)
                                )
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
                    }

                    // Baris Tombol Aksi Responsif (Mulai Tutorial & Reset Onboarding)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        userPreferences.resetTutorial()
                                        onNavigateToDashboard()
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = MonzoTealLight,
                            border = BorderStroke(0.8.dp, MonzoTeal.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MonzoTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mulai Tutorial",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MonzoTeal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        userPreferences.resetOnboarding()
                                        showResetOnboardingSnackbar = true
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = MonzoElevated,
                            border = BorderStroke(0.8.dp, MonzoBorder)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = MonzoTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reset Onboarding",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MonzoTextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp
                                    )
                                )
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
