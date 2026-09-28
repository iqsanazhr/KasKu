package com.example.kasku.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Account
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import com.example.kasku.ui.screens.transactions.EntryMode
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoCoralPillLight
import com.example.kasku.ui.theme.MonzoExpenseRed
import com.example.kasku.ui.theme.MonzoIncomeContainer
import com.example.kasku.ui.theme.MonzoIncomeGreen
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import com.example.kasku.ui.theme.PureWhiteSurface
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialType: TransactionType = TransactionType.EXPENSE,
    onTransactionSaved: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val categories by repository.getAllCategories().collectAsState(initial = emptyList())
    val accounts by repository.getAllAccounts().collectAsState(initial = emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var entryMode by remember { mutableStateOf(EntryMode.MANUAL) }
    var naturalLanguageInput by remember { mutableStateOf("") }
    var isAiParsing by remember { mutableStateOf(false) }

    var inputType by remember { mutableStateOf(initialType) }
    var inputTitle by remember { mutableStateOf("") }
    var inputAmount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }

    // Filter kategori berdasarkan tipe yang dipilih (Pemasukan atau Pengeluaran)
    val filteredCategories = remember(categories, inputType) {
        categories.filter { it.type == inputType }
    }

    // Auto-select kategori pertama saat list kategori berubah atau tipe berganti
    LaunchedEffect(filteredCategories) {
        if (filteredCategories.isNotEmpty()) {
            if (selectedCategoryId == null || filteredCategories.none { it.id == selectedCategoryId }) {
                selectedCategoryId = filteredCategories.first().id
            }
        }
    }

    // Auto-select akun/dompet pertama jika belum terpilih
    LaunchedEffect(accounts) {
        if (accounts.isNotEmpty()) {
            if (selectedAccountId == null || accounts.none { it.id == selectedAccountId }) {
                selectedAccountId = accounts.first().id
            }
        }
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
                .padding(horizontal = 22.dp)
                .imePadding()
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Sheet
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Catat Transaksi",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Kelola arus kas masuk dan keluar akun",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MonzoTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = MonzoTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Switcher (Manual vs AI Quick Entry)
            AppleSegmentedControl(
                items = EntryMode.values().toList(),
                selectedItem = entryMode,
                onItemSelected = { entryMode = it },
                labelProvider = { it.label }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (entryMode == EntryMode.AI_QUICK) {
                // ==========================================
                // SEKSI 1: CATAT CEPAT DENGAN AI
                // ==========================================
                Text(
                    text = "Ketik transaksi dalam bahasa sehari-hari:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MonzoTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = naturalLanguageInput,
                    onValueChange = { naturalLanguageInput = it },
                    placeholder = {
                        Text(
                            text = "Contoh: Dapat gaji 5jt ke bca\natau: Beli kopi 25rb bayar gopay",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MonzoTextSecondary.copy(alpha = 0.7f))
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonzoTeal,
                        unfocusedBorderColor = Color(0xFFDCE4DE)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppleButton(
                    text = if (isAiParsing) "Menganalisis Kalimat..." else "Ekstrak & Isi Otomatis",
                    icon = Icons.Filled.AutoAwesome,
                    onClick = {
                        if (naturalLanguageInput.isNotBlank() && !isAiParsing) {
                            scope.launch {
                                isAiParsing = true
                                try {
                                    val provider = aiPreferences.aiProviderFlow.first()
                                    val key = aiPreferences.geminiApiKeyFlow.first()
                                    val gModel = aiPreferences.geminiModelFlow.first()
                                    val lmUrl = aiPreferences.lmStudioUrlFlow.first()
                                    val lmModel = aiPreferences.lmStudioModelFlow.first()

                                    val res = aiService.parseNaturalLanguageEntry(
                                        text = naturalLanguageInput,
                                        provider = provider,
                                        geminiApiKey = key,
                                        geminiModel = gModel,
                                        lmStudioUrl = lmUrl,
                                        lmStudioModel = lmModel
                                    )

                                    res.onSuccess { parsed ->
                                        inputTitle = parsed.storeName
                                        inputAmount = parsed.totalAmount.toLong().toString()

                                        // Cocokkan kategori hasil AI
                                        val matchedCat = categories.firstOrNull {
                                            it.name.contains(parsed.suggestedCategory, ignoreCase = true)
                                        }
                                        if (matchedCat != null) {
                                            inputType = matchedCat.type
                                            selectedCategoryId = matchedCat.id
                                        }

                                        // Cocokkan dompet berdasarkan teks input
                                        val lowerText = naturalLanguageInput.lowercase()
                                        val matchedAcc = accounts.firstOrNull { acc ->
                                            val nameLower = acc.name.lowercase()
                                            lowerText.contains(nameLower) ||
                                                    (acc.type == "BANK" && (lowerText.contains("bank") || lowerText.contains("bca") || lowerText.contains("mandiri") || lowerText.contains("bri") || lowerText.contains("bni"))) ||
                                                    (acc.type == "E_WALLET" && (lowerText.contains("gopay") || lowerText.contains("dana") || lowerText.contains("ovo") || lowerText.contains("shopeepay"))) ||
                                                    (acc.type == "CASH" && (lowerText.contains("tunai") || lowerText.contains("cash")))
                                        }
                                        if (matchedAcc != null) {
                                            selectedAccountId = matchedAcc.id
                                        }

                                        // Alihkan ke Manual untuk review dan konfirmasi
                                        entryMode = EntryMode.MANUAL
                                    }
                                } catch (_: Exception) {
                                } finally {
                                    isAiParsing = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // ==========================================
                // SEKSI 2: FORM INPUT MANUAL
                // ==========================================

                // 1. Pemilihan Tipe Transaksi (Pemasukan vs Pengeluaran)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isExpense = inputType == TransactionType.EXPENSE
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (inputType != TransactionType.EXPENSE) {
                                    inputType = TransactionType.EXPENSE
                                    val newFiltered = categories.filter { it.type == TransactionType.EXPENSE }
                                    if (newFiltered.isNotEmpty()) selectedCategoryId = newFiltered.first().id
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isExpense) MonzoCoralPillLight else Color(0xFFF7FAF7),
                        border = BorderStroke(
                            width = if (isExpense) 1.5.dp else 1.dp,
                            color = if (isExpense) MonzoExpenseRed else Color(0xFFE2E8E3)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (isExpense) MonzoExpenseRed else MonzoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pengeluaran",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isExpense) MonzoExpenseRed else MonzoTextSecondary,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }

                    val isIncome = inputType == TransactionType.INCOME
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (inputType != TransactionType.INCOME) {
                                    inputType = TransactionType.INCOME
                                    val newFiltered = categories.filter { it.type == TransactionType.INCOME }
                                    if (newFiltered.isNotEmpty()) selectedCategoryId = newFiltered.first().id
                                }
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isIncome) MonzoIncomeContainer else Color(0xFFF7FAF7),
                        border = BorderStroke(
                            width = if (isIncome) 1.5.dp else 1.dp,
                            color = if (isIncome) MonzoIncomeGreen else Color(0xFFE2E8E3)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = if (isIncome) MonzoIncomeGreen else MonzoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pemasukan",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isIncome) MonzoIncomeGreen else MonzoTextSecondary,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Input Nominal (Rp)
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { str ->
                        if (str.all { it.isDigit() }) inputAmount = str
                    },
                    label = { Text("Nominal") },
                    placeholder = { Text("0") },
                    leadingIcon = {
                        Text(
                            text = "Rp",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (inputType == TransactionType.EXPENSE) MonzoExpenseRed else MonzoIncomeGreen
                            ),
                            modifier = Modifier.padding(start = 14.dp, end = 4.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (inputType == TransactionType.EXPENSE) MonzoExpenseRed else MonzoIncomeGreen,
                        unfocusedBorderColor = Color(0xFFD6DFD9)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Input Judul / Keterangan
                OutlinedTextField(
                    value = inputTitle,
                    onValueChange = { inputTitle = it },
                    label = { Text("Keterangan / Keperluan") },
                    placeholder = {
                        Text(
                            if (inputType == TransactionType.EXPENSE) "Misal: Makan Siang, Bensin, Belanja Bulanan"
                            else "Misal: Gaji Kantor, Bonus Proyek, Hasil Penjualan"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (inputType == TransactionType.EXPENSE) MonzoExpenseRed else MonzoIncomeGreen,
                        unfocusedBorderColor = Color(0xFFD6DFD9)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 4. PILIHAN SUMBER / TUJUAN DOMPET
                // ==========================================
                val walletSectionTitle = if (inputType == TransactionType.EXPENSE) {
                    "Bayar Dari Dompet (Sumber Dana):"
                } else {
                    "Masuk Ke Dompet (Tujuan Dana):"
                }

                Text(
                    text = walletSectionTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonzoTextPrimary,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (accounts.isEmpty()) {
                    Text(
                        text = "Belum ada akun/dompet terdaftar",
                        style = MaterialTheme.typography.bodySmall.copy(color = MonzoTextSecondary)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(accounts) { acc ->
                            val isSelected = acc.id == selectedAccountId
                            val (accIcon, typeBadge) = when (acc.type) {
                                "BANK" -> Pair(Icons.Filled.AccountBalance, "BANK")
                                "E_WALLET" -> Pair(Icons.Filled.AccountBalanceWallet, "E-WALLET")
                                else -> Pair(Icons.Filled.Payments, "TUNAI")
                            }

                            val activeBorderColor = if (inputType == TransactionType.EXPENSE) MonzoExpenseRed else MonzoIncomeGreen
                            val activeBgColor = if (inputType == TransactionType.EXPENSE) MonzoCoralPillLight else MonzoIncomeContainer

                            Surface(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedAccountId = acc.id },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) activeBgColor else Color(0xFFFAFCFA),
                                border = BorderStroke(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) activeBorderColor else Color(0xFFE2E8E4)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = accIcon,
                                                contentDescription = null,
                                                tint = if (isSelected) activeBorderColor else MonzoTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFE6EBE7)
                                            ) {
                                                Text(
                                                    text = typeBadge,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MonzoTextSecondary,
                                                        fontSize = 9.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = activeBorderColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MonzoTextPrimary,
                                            fontSize = 12.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "Saldo: ${formatRupiah(acc.balance, true)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = MonzoTextSecondary,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 5. PILIHAN KATEGORI (TERFILTER SESUAI TIPE)
                // ==========================================
                val categorySectionTitle = if (inputType == TransactionType.EXPENSE) {
                    "Kategori Pengeluaran:"
                } else {
                    "Kategori Pemasukan:"
                }

                Text(
                    text = categorySectionTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonzoTextPrimary,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (filteredCategories.isEmpty()) {
                    Text(
                        text = "Belum ada kategori untuk tipe ini",
                        style = MaterialTheme.typography.bodySmall.copy(color = MonzoTextSecondary)
                    )
                } else {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCategories) { cat ->
                            val isSelected = cat.id == selectedCategoryId
                            val activeBg = if (inputType == TransactionType.EXPENSE) MonzoCoral else MonzoIncomeGreen

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedCategoryId = cat.id },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) activeBg else Color(0xFFF1F4F2),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) activeBg else Color(0xFFE2E8E4)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Dot warna kategori
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color.White else Color(cat.colorHex)
                                            )
                                    )
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isSelected) Color.White else MonzoTextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ==========================================
                // 6. TOMBOL SIMPAN TRANSAKSI
                // ==========================================
                val canSave = inputTitle.isNotBlank() &&
                        (inputAmount.toDoubleOrNull() ?: 0.0) > 0 &&
                        selectedCategoryId != null &&
                        selectedAccountId != null

                Button(
                    onClick = {
                        val amount = inputAmount.toDoubleOrNull() ?: 0.0
                        val catId = selectedCategoryId
                        val accId = selectedAccountId
                        if (canSave && catId != null && accId != null) {
                            val cat = categories.firstOrNull { it.id == catId }
                            val acc = accounts.firstOrNull { it.id == accId }
                            if (cat != null && acc != null) {
                                scope.launch {
                                    repository.addTransaction(
                                        Transaction(
                                            title = inputTitle.trim(),
                                            amount = amount,
                                            type = inputType,
                                            categoryId = cat.id,
                                            categoryName = cat.name,
                                            categoryIcon = cat.iconName,
                                            categoryColor = cat.colorHex,
                                            accountId = acc.id,
                                            accountName = acc.name,
                                            date = System.currentTimeMillis()
                                        )
                                    )
                                    onTransactionSaved()
                                    onDismiss()
                                }
                            }
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (inputType == TransactionType.EXPENSE) MonzoCoral else MonzoIncomeGreen,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFD6DFD9),
                        disabledContentColor = Color(0xFF8D9992)
                    )
                ) {
                    Text(
                        text = if (inputType == TransactionType.EXPENSE) "Simpan Pengeluaran" else "Simpan Pemasukan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}
