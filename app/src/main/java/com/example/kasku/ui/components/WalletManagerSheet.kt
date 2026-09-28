package com.example.kasku.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Account
import com.example.kasku.ui.theme.BrandPrimary
import com.example.kasku.ui.theme.ElevatedSurface
import com.example.kasku.ui.theme.ExpenseRed
import com.example.kasku.ui.theme.PureWhiteSurface
import com.example.kasku.ui.theme.SubtleBorder
import com.example.kasku.ui.theme.TextPrimary
import com.example.kasku.ui.theme.TextSecondary
import kotlinx.coroutines.launch

enum class WalletTypeOption(val label: String, val code: String) {
    E_WALLET("E-Wallet", "E_WALLET"),
    BANK("Bank", "BANK"),
    CASH("Tunai", "CASH")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletManagerSheet(
    repository: KasKuRepository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val accounts by repository.getAllAccounts().collectAsState(initial = emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Form states
    var isAddingNew by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }

    var inputName by remember { mutableStateOf("") }
    var inputBalance by remember { mutableStateOf("0") }
    var selectedType by remember { mutableStateOf(WalletTypeOption.E_WALLET) }

    fun resetForm() {
        isAddingNew = false
        editingAccount = null
        inputName = ""
        inputBalance = "0"
        selectedType = WalletTypeOption.E_WALLET
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PureWhiteSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Kelola Dompet & Akun",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "Tambah, ubah saldo, atau hapus sumber dana",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add or Edit Form
            AnimatedVisibility(visible = isAddingNew || editingAccount != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ElevatedSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (editingAccount != null) "Edit Dompet" else "Tambah Dompet Baru",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Type Selector
                        AppleSegmentedControl(
                            items = WalletTypeOption.values().toList(),
                            selectedItem = selectedType,
                            onItemSelected = { selectedType = it },
                            labelProvider = { it.label }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Name Field
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Nama Dompet (cth: DANA, Bank Jago, dll.)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PureWhiteSurface,
                                unfocusedContainerColor = PureWhiteSurface,
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = SubtleBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Balance Field
                        OutlinedTextField(
                            value = inputBalance,
                            onValueChange = { inputBalance = it },
                            label = { Text("Nominal Saldo (Rp)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PureWhiteSurface,
                                unfocusedContainerColor = PureWhiteSurface,
                                focusedBorderColor = BrandPrimary,
                                unfocusedBorderColor = SubtleBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AppleButton(
                                text = "Batal",
                                isPrimary = false,
                                onClick = { resetForm() },
                                modifier = Modifier.weight(1f)
                            )

                            AppleButton(
                                text = if (editingAccount != null) "Perbarui" else "Simpan",
                                onClick = {
                                    val balance = inputBalance.toDoubleOrNull() ?: 0.0
                                    if (inputName.isNotBlank()) {
                                        scope.launch {
                                            if (editingAccount != null) {
                                                repository.updateAccount(
                                                    editingAccount!!.copy(
                                                        name = inputName.trim(),
                                                        balance = balance,
                                                        type = selectedType.code
                                                    )
                                                )
                                            } else {
                                                repository.addAccount(
                                                    Account(
                                                        name = inputName.trim(),
                                                        type = selectedType.code,
                                                        balance = balance,
                                                        iconName = when (selectedType) {
                                                            WalletTypeOption.E_WALLET -> "account_balance_wallet"
                                                            WalletTypeOption.BANK -> "account_balance"
                                                            WalletTypeOption.CASH -> "wallet"
                                                        }
                                                    )
                                                )
                                            }
                                            resetForm()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }
                }
            }

            // Button to trigger Add Form
            if (!isAddingNew && editingAccount == null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            isAddingNew = true
                            inputName = ""
                            inputBalance = "0"
                            selectedType = WalletTypeOption.E_WALLET
                        },
                    shape = RoundedCornerShape(18.dp),
                    color = ElevatedSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tambah Dompet",
                            tint = BrandPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tambah Dompet Baru",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Wallet List
            Text(
                text = "Daftar Dompet (${accounts.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(accounts, key = { it.id }) { acc ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = PureWhiteSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(ElevatedSurface, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (acc.type) {
                                            "BANK" -> Icons.Filled.AccountBalance
                                            "E_WALLET" -> Icons.Filled.AccountBalanceWallet
                                            else -> Icons.Filled.Payments
                                        },
                                        contentDescription = null,
                                        tint = BrandPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = formatRupiah(acc.balance),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }

                            // Edit & Delete Action Buttons
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        editingAccount = acc
                                        inputName = acc.name
                                        inputBalance = acc.balance.toLong().toString()
                                        selectedType = when (acc.type) {
                                            "BANK" -> WalletTypeOption.BANK
                                            "E_WALLET" -> WalletTypeOption.E_WALLET
                                            else -> WalletTypeOption.CASH
                                        }
                                        isAddingNew = false
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "Edit",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            repository.deleteAccount(acc)
                                        }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Hapus",
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(18.dp)
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
