package com.example.kasku.ui.screens.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import com.example.kasku.ui.components.AddTransactionSheet
import com.example.kasku.ui.components.formatRupiah
import com.example.kasku.ui.theme.MonzoBackground
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoCoralPillLight
import com.example.kasku.ui.theme.MonzoElevated
import com.example.kasku.ui.theme.MonzoExpenseRed
import com.example.kasku.ui.theme.MonzoIncomeContainer
import com.example.kasku.ui.theme.MonzoIncomeGreen
import com.example.kasku.ui.theme.MonzoNavy
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTealBorder
import com.example.kasku.ui.theme.MonzoTealLight
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import com.example.kasku.ui.theme.MonzoTextTertiary
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.launch
import com.example.kasku.data.preferences.UserPreferences
import com.example.kasku.ui.components.FeatureTutorialOverlay
import com.example.kasku.ui.components.TutorialStep
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransactionFilter(val label: String) {
    ALL("Semua"),
    EXPENSE("Pengeluaran"),
    INCOME("Pemasukan")
}

enum class EntryMode(val label: String) {
    AI_QUICK("Catat Cepat AI"),
    MANUAL("Input Manual")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    modifier: Modifier = Modifier,
    userPreferences: UserPreferences? = null,
    onTransactionClick: ((Long) -> Unit)? = null,
    viewModel: TransactionsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = TransactionsViewModel.Factory(repository, aiService, aiPreferences)
    )
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    // Hitung ringkasan transaksi yang tampil
    val totalFilteredIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }
    val totalFilteredExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }

    val isTutorialCompleted by (userPreferences?.isTutorialTransactionsCompletedFlow ?: remember { kotlinx.coroutines.flow.flowOf(true) })
        .collectAsState(initial = true)
    var currentTutorialStepIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var statsCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var searchFilterCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var listCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var fabCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    var statsBounds by remember { mutableStateOf<Rect?>(null) }
    var searchFilterBounds by remember { mutableStateOf<Rect?>(null) }
    var listBounds by remember { mutableStateOf<Rect?>(null) }
    var fabBounds by remember { mutableStateOf<Rect?>(null) }

    fun updateAllBounds(root: LayoutCoordinates) {
        if (!root.isAttached) return
        statsCoords?.takeIf { it.isAttached }?.let { statsBounds = root.localBoundingBoxOf(it, false) }
        searchFilterCoords?.takeIf { it.isAttached }?.let { searchFilterBounds = root.localBoundingBoxOf(it, false) }
        listCoords?.takeIf { it.isAttached }?.let { listBounds = root.localBoundingBoxOf(it, false) }
        fabCoords?.takeIf { it.isAttached }?.let { fabBounds = root.localBoundingBoxOf(it, false) }
    }

    val tutorialSteps = remember(statsBounds, searchFilterBounds, listBounds, fabBounds) {
        listOf(
            TutorialStep(
                id = "tx_stats",
                title = "Ringkasan Mutasi Kas",
                description = "Pantau akumulasi total pemasukan dan pengeluaran secara real-time sesuai dengan pencarian atau filter yang sedang aktif.",
                category = "Histori",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                targetRect = statsBounds
            ),
            TutorialStep(
                id = "tx_search_filter",
                title = "Pencarian & Filter Transaksi",
                description = "Cari mutasi berdasarkan nama toko/keterangan, serta beralih cepat antara kategori Semua, Pengeluaran, dan Pemasukan.",
                category = "Pencarian",
                icon = Icons.Filled.Search,
                targetRect = searchFilterBounds
            ),
            TutorialStep(
                id = "tx_list",
                title = "Daftar Riwayat Transaksi",
                description = "Sentuh item transaksi untuk membuka detail lengkap, atau tekan ikon tong sampah untuk menghapus catatan transaksi.",
                category = "Rincian",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                targetRect = listBounds
            ),
            TutorialStep(
                id = "tx_fab",
                title = "Catat Transaksi Instan",
                description = "Gunakan tombol plus melayang ini untuk menambah mutasi kas baru kapan saja secara cepat dan praktis.",
                category = "Pencatatan",
                icon = Icons.Filled.Add,
                targetRect = fabBounds
            )
        )
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentTutorialStepIndex, isTutorialCompleted) {
        if (!isTutorialCompleted) {
            when (currentTutorialStepIndex) {
                0, 1 -> lazyListState.animateScrollToItem(0)
                2 -> lazyListState.animateScrollToItem(3)
                3 -> lazyListState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(lazyListState.firstVisibleItemScrollOffset, lazyListState.firstVisibleItemIndex) {
        rootCoordinates?.let { updateAllBounds(it) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MonzoBackground)
            .onGloballyPositioned { root ->
                rootCoordinates = root
                updateAllBounds(root)
            }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. TOP HEADER TITLE
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Histori Kas",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MonzoTextSecondary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Riwayat Transaksi",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonzoTextPrimary,
                                fontSize = 23.sp
                            )
                        )
                    }

                    // Badge Jumlah Transaksi
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MonzoTealLight,
                        border = BorderStroke(1.dp, MonzoTealBorder)
                    ) {
                        Text(
                            text = "${filteredTransactions.size} Transaksi",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonzoTeal,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }
            }

            // ==========================================
            // 2. STATS OVERVIEW CARD (MONZO STYLE)
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
                        )
                        .onGloballyPositioned { coords ->
                            statsCoords = coords
                            rootCoordinates?.let { root ->
                                if (root.isAttached && coords.isAttached) {
                                    statsBounds = root.localBoundingBoxOf(coords, false)
                                }
                            }
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = MonzoSurface,
                    border = BorderStroke(1.dp, MonzoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Kolom Pemasukan
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MonzoIncomeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Total Masuk",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = "+${formatRupiah(totalFilteredIncome, false)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoIncomeGreen,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Garis Pembatas Vertikal Halus
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp)
                                .background(MonzoBorder)
                        )

                        // Kolom Pengeluaran
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = MonzoExpenseRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Total Keluar",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Text(
                                text = "-${formatRupiah(totalFilteredExpense, false)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoExpenseRed,
                                    fontSize = 15.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 3. SEARCH & FILTER PILLS
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            searchFilterCoords = coords
                            rootCoordinates?.let { root ->
                                if (root.isAttached && coords.isAttached) {
                                    searchFilterBounds = root.localBoundingBoxOf(coords, false)
                                }
                            }
                        },
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = "Cari transaksi, toko, atau akun...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MonzoTextTertiary, fontSize = 13.5.sp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = MonzoTeal,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Bersihkan",
                                        tint = MonzoTextSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MonzoSurface,
                            unfocusedContainerColor = MonzoSurface,
                            focusedBorderColor = MonzoTeal,
                            unfocusedBorderColor = MonzoBorder,
                            focusedTextColor = MonzoTextPrimary,
                            unfocusedTextColor = MonzoTextPrimary
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionFilter.values().forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setFilter(filter) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MonzoTeal else MonzoSurface,
                                border = BorderStroke(1.dp, if (isSelected) MonzoTeal else MonzoBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = filter.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MonzoTextSecondary,
                                            fontSize = 12.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 5. TRANSACTIONS LIST IN BEAUTIFUL CARDS
            // ==========================================
            if (filteredTransactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color(0x08000000),
                                spotColor = Color(0x10000000)
                            )
                            .onGloballyPositioned { coords ->
                                listCoords = coords
                                rootCoordinates?.let { root ->
                                    if (root.isAttached && coords.isAttached) {
                                        listBounds = root.localBoundingBoxOf(coords, false)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(20.dp),
                        color = MonzoSurface,
                        border = BorderStroke(1.dp, MonzoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = CircleShape,
                                color = MonzoTealLight
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                        contentDescription = null,
                                        tint = MonzoTeal,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Tidak Ada Transaksi",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "Tidak ditemukan catatan dengan kata kunci \"$searchQuery\"."
                                } else {
                                    "Belum ada catatan yang sesuai dengan filter ini."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    val isFirstItem = filteredTransactions.firstOrNull()?.id == tx.id
                    MonzoTransactionCard(
                        transaction = tx,
                        onClick = { onTransactionClick?.invoke(tx.id) },
                        onDeleteClick = { transactionToDelete = tx },
                        modifier = if (isFirstItem) {
                            Modifier.onGloballyPositioned { coords ->
                                listCoords = coords
                                rootCoordinates?.let { root ->
                                    if (root.isAttached && coords.isAttached) {
                                        listBounds = root.localBoundingBoxOf(coords, false)
                                    }
                                }
                            }
                        } else Modifier
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(130.dp))
            }
        }

        // ==========================================
        // 6. DIALOG KONFIRMASI HAPUS TRANSAKSI
        // ==========================================
        if (transactionToDelete != null) {
            val tx = transactionToDelete!!
            AlertDialog(
                onDismissRequest = { transactionToDelete = null },
                containerColor = MonzoSurface,
                title = {
                    Text(
                        text = "Hapus Transaksi?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary
                        )
                    )
                },
                text = {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus \"${tx.title}\" sebesar ${formatRupiah(tx.amount)}? Saldo dompet ${tx.accountName} akan disesuaikan kembali secara otomatis.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MonzoTextSecondary,
                            fontSize = 13.5.sp
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTransaction(tx)
                            transactionToDelete = null
                            com.example.kasku.ui.components.TopNotif.showInfo(
                                title = "Transaksi Dihapus",
                                message = "Catatan transaksi telah dihapus dari histori"
                            )
                        }
                    ) {
                        Text(
                            text = "Hapus",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonzoExpenseRed
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { transactionToDelete = null }) {
                        Text(
                            text = "Batal",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MonzoTextSecondary
                            )
                        )
                    }
                }
            )
        }

        // ==========================================
        // 7. BOTTOM SHEET INPUT TRANSAKSI
        // ==========================================
        if (showAddSheet) {
            AddTransactionSheet(
                repository = repository,
                aiService = aiService,
                aiPreferences = aiPreferences,
                onDismiss = { showAddSheet = false },
                onTransactionSaved = { showAddSheet = false }
            )
        }

        // ==========================================
        // 8. FLOATING ACTION BUTTON TAMBAH TRANSAKSI
        // ==========================================
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 95.dp, end = 18.dp)
                .size(54.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Color(0x33EB5B44),
                    spotColor = Color(0x4DEB5B44)
                )
                .clip(CircleShape)
                .clickable { showAddSheet = true }
                .onGloballyPositioned { coords ->
                    fabCoords = coords
                    rootCoordinates?.let { root ->
                        if (root.isAttached && coords.isAttached) {
                            fabBounds = root.localBoundingBoxOf(coords, false)
                        }
                    }
                },
            color = MonzoCoral,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Tambah Transaksi",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Overlay Tutorial Interaktif Riwayat
        if (!isTutorialCompleted && userPreferences != null) {
            FeatureTutorialOverlay(
                steps = tutorialSteps,
                currentStepIndex = currentTutorialStepIndex,
                onNextStep = {
                    if (currentTutorialStepIndex < tutorialSteps.size - 1) {
                        currentTutorialStepIndex++
                    } else {
                        coroutineScope.launch {
                            userPreferences.setTutorialTransactionsCompleted(true)
                        }
                    }
                },
                onSkipTutorial = {
                    coroutineScope.launch {
                        userPreferences.setTutorialTransactionsCompleted(true)
                    }
                }
            )
        }
    }
}

/**
 * Kartu Transaksi Modern Monzo Banking (Bungkus Card untuk Riwayat Transaksi)
 */
@Composable
fun MonzoTransactionCard(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val isTransfer = transaction.type == TransactionType.TRANSFER

    val formattedDate = remember(transaction.date) {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
        sdf.format(Date(transaction.date))
    }

    // Icon & Color styling sesuai kategori dan jenis transaksi
    val (iconBg, iconTint, icon) = when {
        isIncome -> Triple(MonzoIncomeContainer, MonzoIncomeGreen, Icons.Filled.AccountBalance)
        isTransfer -> Triple(MonzoTealLight, MonzoTeal, Icons.Filled.SwapHoriz)
        transaction.title.contains("uber", ignoreCase = true) ||
                transaction.title.contains("gojek", ignoreCase = true) ||
                transaction.title.contains("grab", ignoreCase = true) ||
                transaction.categoryName.contains("transpor", ignoreCase = true) ->
            Triple(Color(0xFFE8F2FA), Color(0xFF1E88E5), Icons.Filled.DirectionsCar)
        transaction.title.contains("makan", ignoreCase = true) ||
                transaction.title.contains("kopi", ignoreCase = true) ||
                transaction.categoryName.contains("makan", ignoreCase = true) ->
            Triple(MonzoCoralPillLight, MonzoCoral, Icons.Filled.Fastfood)
        transaction.categoryName.contains("belanja", ignoreCase = true) ||
                transaction.categoryName.contains("supermarket", ignoreCase = true) ->
            Triple(Color(0xFFF3E8FF), Color(0xFF8B5CF6), Icons.Filled.ShoppingBag)
        else -> Triple(MonzoElevated, MonzoNavy, Icons.AutoMirrored.Filled.ReceiptLong)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Color(0x08000000),
                spotColor = Color(0x10000000)
            ),
        shape = RoundedCornerShape(18.dp),
        color = MonzoSurface,
        border = BorderStroke(1.dp, MonzoBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Baris Utama: Icon Squircle, Info Judul & Kategori, Nominal & Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sisi Kiri: Squircle Icon + Detail Judul
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = iconBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonzoTextPrimary,
                                fontSize = 15.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Badges Kategori & Akun Dompet
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Chip Dompet
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MonzoElevated,
                                border = BorderStroke(0.5.dp, MonzoBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MonzoTeal,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = transaction.accountName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MonzoTextPrimary,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            // Kategori
                            Text(
                                text = transaction.categoryName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sisi Kanan: Nominal Rupiah
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    val amountColor = when {
                        isIncome -> MonzoIncomeGreen
                        isTransfer -> MonzoTeal
                        else -> MonzoExpenseRed
                    }
                    val amountPrefix = when {
                        isIncome -> "+"
                        isTransfer -> ""
                        else -> "-"
                    }

                    Text(
                        text = "$amountPrefix${formatRupiah(transaction.amount, false)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = amountColor,
                            fontSize = 16.sp
                        )
                    )

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MonzoTextTertiary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Catatan transaksi jika tersedia
            if (transaction.note.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF7FAF7)
                ) {
                    Text(
                        text = "Catatan: ${transaction.note}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MonzoTextSecondary,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Baris Bawah: Rincian barang struk & Tombol Hapus Cepat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (transaction.rawReceiptItems.isNotEmpty()) {
                    Text(
                        text = "📦 ${transaction.rawReceiptItems.size} barang tercatat dari struk",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MonzoTeal,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (onDeleteClick != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onDeleteClick)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Hapus Transaksi",
                            tint = MonzoTextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Hapus",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MonzoTextTertiary,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
