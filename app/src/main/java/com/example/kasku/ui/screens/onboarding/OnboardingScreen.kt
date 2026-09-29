package com.example.kasku.ui.screens.onboarding

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.data.preferences.UserPreferences
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Account
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoCoralPillLight
import com.example.kasku.ui.theme.MonzoElevated
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import com.example.kasku.ui.theme.MonzoTextTertiary
import kotlinx.coroutines.launch

data class WalletTypeOption(
    val code: String,
    val label: String,
    val icon: ImageVector,
    val defaultName: String
)

/**
 * Dialog Onboarding yang terpusat vertikal dengan latar belakang Home yang di-blur
 */
@Composable
fun OnboardingDialog(
    userPreferences: UserPreferences,
    repository: KasKuRepository,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var userNameInput by remember { mutableStateOf("") }
    var selectedWalletType by remember { mutableStateOf("CASH") }
    var walletNameInput by remember { mutableStateOf("Uang Tunai (Cash)") }
    var initialBalanceInput by remember { mutableStateOf("") }

    val walletOptions = listOf(
        WalletTypeOption("CASH", "Tunai", Icons.Filled.Money, "Uang Tunai (Cash)"),
        WalletTypeOption("BANK", "Bank", Icons.Filled.AccountBalance, "Rekening Bank (BCA)"),
        WalletTypeOption("E_WALLET", "E-Wallet", Icons.Filled.AccountBalanceWallet, "E-Wallet (GoPay/DANA)")
    )

    fun submitOnboarding() {
        scope.launch {
            val finalName = userNameInput.trim().ifBlank { "Pengguna KasKu" }
            userPreferences.saveUserName(finalName)

            val rawBalance = initialBalanceInput.replace(".", "").replace(",", "").toDoubleOrNull() ?: 0.0
            val finalWalletName = walletNameInput.trim().ifBlank { "Uang Tunai" }

            // Simpan akun utama pertama pengguna dengan saldo awalnya
            repository.addAccount(
                Account(
                    name = finalWalletName,
                    type = selectedWalletType,
                    balance = rawBalance,
                    iconName = when (selectedWalletType) {
                        "BANK" -> "account_balance"
                        "E_WALLET" -> "account_balance_wallet"
                        else -> "wallet"
                    }
                )
            )

            userPreferences.setOnboardingCompleted(true)
            onCompleted()
        }
    }

    // Scrim overlay semi-transparan yang memfokuskan dialog di tengah
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x66081420))
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        // Modal Card Vertikal Centered
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Color(0x33000000),
                    spotColor = Color(0x59000000)
                ),
            shape = RoundedCornerShape(26.dp),
            color = MonzoSurface,
            border = BorderStroke(1.dp, MonzoBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Icon Emblem KasKu
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = MonzoCoralPillLight
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Wallet,
                            contentDescription = null,
                            tint = MonzoCoral,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // 2. Judul & Subjudul
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Selamat Datang di KasKu",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Siapkan profil dan dompet pertamamu untuk mulai mengelola keuangan secara cerdas.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MonzoTextSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // 3. Form Input Nama
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nama Panggilan",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary,
                            fontSize = 12.5.sp
                        )
                    )
                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = { userNameInput = it },
                        placeholder = {
                            Text(
                                text = "cth: Budi / Alex",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MonzoTextTertiary, fontSize = 13.5.sp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MonzoTeal,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MonzoSurface,
                            unfocusedContainerColor = MonzoSurface,
                            focusedBorderColor = MonzoTeal,
                            unfocusedBorderColor = MonzoBorder
                        ),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 4. Form Dompet Pertama
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dompet Pertama & Saldo Awal",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary,
                            fontSize = 12.5.sp
                        )
                    )

                    // Pilihan Jenis Dompet (Pills)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        walletOptions.forEach { opt ->
                            val isSelected = selectedWalletType == opt.code
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedWalletType = opt.code
                                        walletNameInput = opt.defaultName
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MonzoTeal else MonzoElevated,
                                border = BorderStroke(1.dp, if (isSelected) MonzoTeal else MonzoBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = opt.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else MonzoTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = opt.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MonzoTextSecondary,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Nama Dompet
                    OutlinedTextField(
                        value = walletNameInput,
                        onValueChange = { walletNameInput = it },
                        label = { Text("Nama Dompet", fontSize = 12.sp) },
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

                    // Saldo Awal
                    OutlinedTextField(
                        value = initialBalanceInput,
                        onValueChange = { initialBalanceInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Saldo Awal (Rp)", fontSize = 12.sp) },
                        placeholder = { Text("0") },
                        prefix = {
                            Text(
                                text = "Rp ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary
                                )
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MonzoSurface,
                            unfocusedContainerColor = MonzoSurface,
                            focusedBorderColor = MonzoTeal,
                            unfocusedBorderColor = MonzoBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "💡 Saldo awal menjaga agar catatan keuanganmu akurat dan tidak bernilai minus saat mencatat belanjaan pertama.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MonzoTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 5. Tombol Aksi "Mulai Gunakan KasKu"
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { submitOnboarding() },
                    shape = RoundedCornerShape(16.dp),
                    color = MonzoCoral,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Mulai Gunakan KasKu",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Kompatibilitas alias untuk OnboardingScreen
 */
@Composable
fun OnboardingScreen(
    userPreferences: UserPreferences,
    repository: KasKuRepository,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    OnboardingDialog(
        userPreferences = userPreferences,
        repository = repository,
        onCompleted = onCompleted,
        modifier = modifier
    )
}
