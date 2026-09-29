package com.example.kasku.ui.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import com.example.kasku.ui.components.FeatureTutorialOverlay
import com.example.kasku.ui.components.TutorialStep
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.preferences.UserPreferences
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Account
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import com.example.kasku.ui.components.AddTransactionSheet
import com.example.kasku.ui.components.WalletManagerSheet
import com.example.kasku.ui.components.formatRupiah
import com.example.kasku.ui.theme.MonzoAvatarBg
import com.example.kasku.ui.theme.MonzoBackground
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoCoralPill
import com.example.kasku.ui.theme.MonzoCoralPillLight
import com.example.kasku.ui.theme.MonzoExpenseRed
import com.example.kasku.ui.theme.MonzoIncomeContainer
import com.example.kasku.ui.theme.MonzoIncomeGreen
import com.example.kasku.ui.theme.MonzoNavy
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTealLight
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import com.example.kasku.ui.theme.MonzoTextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    repository: KasKuRepository,
    userPreferences: UserPreferences,
    onNavigateToScanner: () -> Unit,
    onNavigateToAiInsights: () -> Unit,
    onQuickAddClick: () -> Unit,
    onManualAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    aiService: AiService? = null,
    aiPreferences: AiPreferences? = null,
    viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DashboardViewModel.Factory(repository, userPreferences)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsState()

    val allAccounts = uiState.allAccounts
    val totalBalance = uiState.totalBalance
    val totalIncome = uiState.totalIncome
    val totalExpense = uiState.totalExpense
    val netCashFlow = totalIncome - totalExpense
    val recentTransactions = uiState.recentTransactions
    val allTransactions = uiState.allTransactions
    val userName = uiState.userName
    val primaryAccount = allAccounts.firstOrNull()
    val userInitial = userName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "K"

    val displayedTransactions = recentTransactions

    val weeklyCashFlowData = remember(allTransactions) {
        val list = mutableListOf<DayCashFlow>()
        val dayFormat = SimpleDateFormat("EEE", Locale.forLanguageTag("id-ID"))

        for (i in 6 downTo 0) {
            val cal = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, -i)
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val startMs = cal.timeInMillis

            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
            cal.set(java.util.Calendar.MINUTE, 59)
            cal.set(java.util.Calendar.SECOND, 59)
            cal.set(java.util.Calendar.MILLISECOND, 999)
            val endMs = cal.timeInMillis

            val dayTx = allTransactions.filter { it.date in startMs..endMs }
            val inc = dayTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val exp = dayTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val label = dayFormat.format(cal.time)

            list.add(DayCashFlow(label = label, income = inc, expense = exp))
        }
        list
    }

    val weekTotalIncome = remember(weeklyCashFlowData) { weeklyCashFlowData.sumOf { it.income } }
    val weekTotalExpense = remember(weeklyCashFlowData) { weeklyCashFlowData.sumOf { it.expense } }
    val weekNetFlow = weekTotalIncome - weekTotalExpense
    var selectedHomeBarIndex by remember { mutableIntStateOf(-1) }

    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greetingText = remember(currentHour) {
        when (currentHour) {
            in 4..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }

    var showWalletManager by remember { mutableStateOf(false) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }

    val cardStack = remember(allAccounts, totalBalance, userName) {
        buildCardStack(allAccounts, totalBalance, userName)
    }
    var cardStackOrder by remember(cardStack) { mutableStateOf(cardStack) }

    val coroutineScope = rememberCoroutineScope()
    val frontCardOffsetY = remember { Animatable(0f) }
    val backCardIncomingY = remember { Animatable(0f) }
    var isCyclingCard by remember { mutableStateOf(false) }

    fun cycleCardToBack() {
        if (isCyclingCard || cardStackOrder.size <= 1) return
        isCyclingCard = true
        coroutineScope.launch {
            // 1. Kartu depan meluncur turun sedikit untuk memisahkan diri dari tumpukan
            frontCardOffsetY.animateTo(
                targetValue = 95f,
                animationSpec = tween(durationMillis = 110, easing = FastOutLinearInEasing)
            )

            // 2. Pindahkan kartu depan lama ke paling belakang list (send to back)
            val oldFront = cardStackOrder.first()
            val newOrder = cardStackOrder.drop(1) + listOf(oldFront)
            cardStackOrder = newOrder

            // 3. Setup posisi awal: kartu belakang baru berada 50f lebih rendah, kartu depan baru di 0f
            backCardIncomingY.snapTo(50f)
            frontCardOffsetY.snapTo(0f)

            // 4. Animasikan kartu belakang baru meluncur naik ke posisinya (0f)
            backCardIncomingY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            isCyclingCard = false
        }
    }

    if (showWalletManager) {
        WalletManagerSheet(
            repository = repository,
            onDismiss = { showWalletManager = false }
        )
    }

    if (showAddTransactionSheet && aiService != null && aiPreferences != null) {
        AddTransactionSheet(
            repository = repository,
            aiService = aiService,
            aiPreferences = aiPreferences,
            onDismiss = { showAddTransactionSheet = false },
            onTransactionSaved = { showAddTransactionSheet = false }
        )
    }

    val isTutorialCompleted by userPreferences.isTutorialCompletedFlow.collectAsState(initial = true)
    var currentTutorialStepIndex by remember { mutableIntStateOf(0) }

    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var cardStackCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var actionPillsCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var cashFlowCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var aiButtonCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    var cardStackBounds by remember { mutableStateOf<Rect?>(null) }
    var actionPillsBounds by remember { mutableStateOf<Rect?>(null) }
    var cashFlowBounds by remember { mutableStateOf<Rect?>(null) }
    var aiButtonBounds by remember { mutableStateOf<Rect?>(null) }

    val lazyListState = rememberLazyListState()

    fun updateAllBounds(root: LayoutCoordinates) {
        if (!root.isAttached) return
        cardStackCoords?.takeIf { it.isAttached }?.let { cardStackBounds = root.localBoundingBoxOf(it, false) }
        actionPillsCoords?.takeIf { it.isAttached }?.let { actionPillsBounds = root.localBoundingBoxOf(it, false) }
        cashFlowCoords?.takeIf { it.isAttached }?.let { cashFlowBounds = root.localBoundingBoxOf(it, false) }
        aiButtonCoords?.takeIf { it.isAttached }?.let { aiButtonBounds = root.localBoundingBoxOf(it, false) }
    }

    val tutorialSteps = remember(cardStackBounds, actionPillsBounds, cashFlowBounds, aiButtonBounds) {
        listOf(
            TutorialStep(
                id = "card_stack",
                title = "Tumpukan Kartu & Saldo",
                description = "Pantau saldo kumulatif atau geser ke bawah untuk melihat masing-masing dompet & rekening bank milikmu. Kamu juga bisa mengetuk ikon mata untuk menyembunyikan saldo.",
                category = "Dompet",
                icon = Icons.Filled.CreditCard,
                targetRect = cardStackBounds
            ),
            TutorialStep(
                id = "action_pills",
                title = "Aksi Catat & Kelola Kas",
                description = "Tekan 'Catat Kas' untuk menambah pemasukan atau pengeluaran baru dengan cepat, atau tombol 'Dompet' untuk mengelola daftar rekening.",
                category = "Transaksi",
                icon = Icons.Filled.Add,
                targetRect = actionPillsBounds
            ),
            TutorialStep(
                id = "cash_flow",
                title = "Grafik Arus Kas 7 Hari",
                description = "Visualisasi arus kas masuk vs keluar dalam sepekan terakhir. Ketuk diagram batang harian untuk melihat rincian angka surplus atau defisit.",
                category = "Analisis",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                targetRect = cashFlowBounds
            ),
            TutorialStep(
                id = "ai_button",
                title = "Asisten KasKu AI",
                description = "Akses asisten AI pintar di sudut kanan atas untuk konsultasi pengelolaan uang, analisis pengeluaran, dan tanya jawab finansial otomatis.",
                category = "AI Pintar",
                icon = Icons.Filled.AutoAwesome,
                targetRect = aiButtonBounds
            )
        )
    }

    LaunchedEffect(currentTutorialStepIndex, isTutorialCompleted) {
        if (!isTutorialCompleted) {
            if (currentTutorialStepIndex == 2) {
                lazyListState.animateScrollToItem(1)
            } else if (currentTutorialStepIndex == 0 || currentTutorialStepIndex == 1 || currentTutorialStepIndex == 3) {
                lazyListState.animateScrollToItem(0)
            }
        }
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // ==========================================
        // 1. MONZO TOP APP BAR
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Monzo User Initial Avatar (Dark Olive Circle) -> Buka Pengaturan
                Surface(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateToSettings),
                    shape = CircleShape,
                    color = MonzoAvatarBg
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userInitial,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                // Sapaan Waktu & Nama: Teks biasa (Bukan Badge / Bubble)
                Text(
                    text = "$greetingText, ${userName.ifBlank { "KasKu" }}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonzoTextPrimary,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Monzo Right Action Icons (Search, KasKu AI, Tambah Cepat)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Icon (Navigates to Riwayat Transaksi)
                IconButton(
                    onClick = onManualAddClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Cari Transaksi",
                        tint = MonzoTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // KasKu AI Icon Button (Diletakkan di kanan atas samping menu pencarian)
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onNavigateToAiChat)
                        .onGloballyPositioned { coords ->
                            aiButtonCoords = coords
                            rootCoordinates?.let { root ->
                                if (root.isAttached && coords.isAttached) {
                                    aiButtonBounds = root.localBoundingBoxOf(coords, false)
                                }
                            }
                        },
                    shape = CircleShape,
                    color = MonzoTealLight,
                    border = BorderStroke(1.dp, MonzoTeal.copy(alpha = 0.35f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "KasKu AI Assistant",
                            tint = MonzoTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Monzo Teal Circle Add Button
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onQuickAddClick),
                    shape = CircleShape,
                    color = MonzoTeal
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tambah Cepat",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. SCROLLABLE DASHBOARD CONTENT
        // ==========================================
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // MONZO DUAL CARD STACK / INTERACTIVE STACK
            // ==========================================
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(242.dp)
                            .padding(top = 4.dp)
                            .zIndex(5f)
                            .onGloballyPositioned { coords ->
                                cardStackCoords = coords
                                rootCoordinates?.let { root ->
                                    if (root.isAttached && coords.isAttached) {
                                        cardStackBounds = root.localBoundingBoxOf(coords, false)
                                    }
                                }
                            }
                    ) {
                        // 1. KARTU BELAKANG (Back Layer): Mengintip di bagian atas (y = 4.dp)
                        if (cardStackOrder.size > 1) {
                            val backCard = cardStackOrder[1]
                            val backY = 4.dp + backCardIncomingY.value.dp

                            MonzoBackCardLayout(
                                card = backCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = backY)
                                    .graphicsLayer {
                                        scaleX = 0.94f
                                        scaleY = 0.94f
                                        alpha = 0.94f
                                    }
                                    .zIndex(1f),
                                onClick = { cycleCardToBack() }
                            )
                        }

                        // 2. KARTU DEPAN AKTIF: Selalu mulai dengan kartu Merah (Kumulatif)
                        val frontCard = cardStackOrder.first()
                        val frontY = 38.dp + frontCardOffsetY.value.dp

                        MonzoPhysicalCardLayout(
                            card = frontCard,
                            isBalanceVisible = isBalanceVisible,
                            onToggleBalanceVisibility = { viewModel.toggleBalanceVisibility() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = frontY)
                                .zIndex(5f)
                                .then(
                                    if (cardStackOrder.size > 1) {
                                        Modifier.pointerInput(cardStackOrder, isCyclingCard) {
                                            detectVerticalDragGestures(
                                                onDragStart = { /* noop */ },
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    if (!isCyclingCard && cardStackOrder.size > 1) {
                                                        coroutineScope.launch {
                                                            val next = (frontCardOffsetY.value + dragAmount * 0.75f).coerceIn(0f, 100f)
                                                            frontCardOffsetY.snapTo(next)
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    if (!isCyclingCard && cardStackOrder.size > 1) {
                                                        if (frontCardOffsetY.value > 45f) {
                                                            cycleCardToBack()
                                                        } else {
                                                            coroutineScope.launch {
                                                                frontCardOffsetY.animateTo(
                                                                    targetValue = 0f,
                                                                    animationSpec = spring(
                                                                        dampingRatio = 0.75f,
                                                                        stiffness = Spring.StiffnessMedium
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                                onDragCancel = {
                                                    coroutineScope.launch {
                                                        frontCardOffsetY.animateTo(0f, spring(dampingRatio = 0.75f))
                                                    }
                                                }
                                            )
                                        }
                                    } else Modifier
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hint Geser Kartu (Informatif, bisa di-tap juga)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(1f)
                            .clickable(enabled = cardStackOrder.size > 1) { cycleCardToBack() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Layers,
                            contentDescription = null,
                            tint = MonzoTeal,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cardStackOrder.size > 1) {
                                "Geser kartu ke bawah untuk ganti dompet (${cardStackOrder.size} dompet)"
                            } else {
                                "Dompet Utama KasKu"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MonzoTeal,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                // Action Pills Row di bawah kartu fisik Monzo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(10f)
                        .onGloballyPositioned { coords ->
                            actionPillsCoords = coords
                            rootCoordinates?.let { root ->
                                if (root.isAttached && coords.isAttached) {
                                    actionPillsBounds = root.localBoundingBoxOf(coords, false)
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "+ Catat Kas" Pill Button
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                if (aiService != null && aiPreferences != null) {
                                    showAddTransactionSheet = true
                                } else {
                                    onManualAddClick()
                                }
                            },
                        shape = CircleShape,
                        color = MonzoCoral
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Catat Kas",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    // "Dompet" Pill Button
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showWalletManager = true },
                        shape = CircleShape,
                        color = MonzoSurface,
                        border = BorderStroke(1.dp, MonzoBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CreditCard,
                                contentDescription = null,
                                tint = MonzoTeal,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Dompet",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 12.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Options Menu Button "⋮"
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { showWalletManager = true },
                        shape = CircleShape,
                        color = MonzoSurface,
                        border = BorderStroke(1.dp, MonzoBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Opsi Dompet",
                                tint = MonzoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

            // ==========================================
            // 2B. KARTU GRAFIK ARUS KAS MINGGUAN (HOME)
            // ==========================================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            cashFlowCoords = coords
                            rootCoordinates?.let { root ->
                                if (root.isAttached && coords.isAttached) {
                                    cashFlowBounds = root.localBoundingBoxOf(coords, false)
                                }
                            }
                        }
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
                        // Header Baris: "Grafik Arus Kas" & Badge Status Surplus / Defisit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Grafik Arus Kas",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoTextPrimary,
                                        fontSize = 16.5.sp
                                    )
                                )
                                Text(
                                    text = "7 Hari Terakhir",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            // Badge Status Arus Kas Mingguan
                            val isWeekSurplus = weekNetFlow >= 0
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isWeekSurplus) MonzoIncomeContainer else MonzoCoralPillLight,
                                border = BorderStroke(
                                    1.dp,
                                    if (isWeekSurplus) MonzoIncomeGreen.copy(alpha = 0.35f) else MonzoExpenseRed.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isWeekSurplus) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isWeekSurplus) MonzoIncomeGreen else MonzoExpenseRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isWeekSurplus) "Surplus" else "Defisit",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWeekSurplus) MonzoIncomeGreen else MonzoExpenseRed,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Baris Ringkasan Angka Arus Kas Mingguan (Tanpa Card Kotak Terpisah)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Masuk: +${formatRupiah(weekTotalIncome, false)}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoIncomeGreen,
                                        fontSize = 12.5.sp
                                    )
                                )
                                Text(
                                    text = "Keluar: -${formatRupiah(weekTotalExpense, false)}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoExpenseRed,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }

                            Text(
                                text = (if (weekNetFlow >= 0) "+" else "") + formatRupiah(weekNetFlow),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (weekNetFlow >= 0) MonzoIncomeGreen else MonzoExpenseRed,
                                    fontSize = 12.5.sp
                                )
                            )
                        }

                        // GRAFIK DUAL-BAR CANVAS (HOME)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(135.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            HomeCashFlowBarChart(
                                data = weeklyCashFlowData,
                                selectedIndex = selectedHomeBarIndex,
                                onBarSelected = { selectedHomeBarIndex = it }
                            )
                        }

                        // DETAIL TOOLTIP JIKA BAR DIKETUK
                        if (selectedHomeBarIndex in weeklyCashFlowData.indices) {
                            val item = weeklyCashFlowData[selectedHomeBarIndex]
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF3F7F4),
                                border = BorderStroke(1.dp, Color(0xFFDCE6DF))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.label}:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MonzoTextPrimary
                                        )
                                    )
                                    Text(
                                        text = "+${formatRupiah(item.income, false)} / -${formatRupiah(item.expense, false)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MonzoTextPrimary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Legend Warna di Bawah Grafik
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MonzoIncomeGreen, CircleShape)
                                    )
                                    Text(
                                        text = "Pemasukan",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MonzoTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MonzoExpenseRed, CircleShape)
                                    )
                                    Text(
                                        text = "Pengeluaran",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MonzoTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "Ketuk bar untuk rincian",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonzoTextTertiary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 3. MONZO "ACTIVITY" SECTION CARD
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
                        modifier = Modifier.padding(18.dp)
                    ) {
                        // Header: "Aktivitas Transaksi" (Otomatis Semua Pemasukan & Pengeluaran)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Aktivitas Transaksi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoTextPrimary,
                                        fontSize = 17.sp
                                    )
                                )
                                Text(
                                    text = "Semua transaksi masuk & keluar",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (displayedTransactions.isEmpty()) {
                            // Friendly Empty State
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Belum Ada Aktivitas Transaksi",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MonzoTextPrimary
                                    )
                                )
                                Text(
                                    text = "Gunakan tombol Catat Kas untuk mencatat transaksi pertamamu.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextSecondary
                                    )
                                )
                            }
                        } else {
                            // Transaction List Items styled like Monzo
                            displayedTransactions.forEachIndexed { index, tx ->
                                MonzoActivityItem(transaction = tx)
                                if (index < displayedTransactions.lastIndex) {
                                    HorizontalDivider(
                                        color = Color(0xFFF0F2F4),
                                        thickness = 0.7.dp,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF0F2F4), thickness = 0.7.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // "Lihat Semua Transaksi" Button at the bottom of the card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onManualAddClick() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Lihat Semua Transaksi",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MonzoTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 4. MONZO "SUGGESTED ACTIONS" SECTION
            // ==========================================
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Aksi Cepat",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary,
                            fontSize = 16.sp
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Pindai Struk Belanja
                        MonzoActionCard(
                            title = "Pindai Struk",
                            subtitle = "Foto & Ekstrak AI",
                            icon = Icons.Filled.DocumentScanner,
                            containerColor = Color(0xFFE5F4F7),
                            iconColor = MonzoTeal,
                            onClick = onNavigateToScanner
                        )

                        // Card 2: Catat AI
                        MonzoActionCard(
                            title = "Catat AI",
                            subtitle = "Ketik Bahasa Santai",
                            icon = Icons.Filled.AutoAwesome,
                            containerColor = Color(0xFFFDECE9),
                            iconColor = MonzoCoral,
                            onClick = onQuickAddClick
                        )

                        // Card 3: Kelola Dompet
                        MonzoActionCard(
                            title = "Kelola Dompet",
                            subtitle = "Atur Rekening & Saldo",
                            icon = Icons.Filled.Wallet,
                            containerColor = Color(0xFFFEF7EC),
                            iconColor = Color(0xFFD97706),
                            onClick = { showWalletManager = true }
                        )

                        // Card 4: Asisten Finansial
                        MonzoActionCard(
                            title = "Asisten Finansial",
                            subtitle = "Analisis & Tips AI",
                            icon = Icons.Filled.CardGiftcard,
                            containerColor = Color(0xFFEEF2FF),
                            iconColor = Color(0xFF4F46E5),
                            onClick = onNavigateToAiInsights
                        )
                    }
                }
            }

            // Bottom space so content doesn't get covered by bottom nav
            item {
                Spacer(modifier = Modifier.height(130.dp))
            }
        }
    }

    // Overlay Tutorial Interaktif
    if (!isTutorialCompleted) {
        FeatureTutorialOverlay(
            steps = tutorialSteps,
            currentStepIndex = currentTutorialStepIndex,
            onNextStep = {
                if (currentTutorialStepIndex < tutorialSteps.size - 1) {
                    currentTutorialStepIndex++
                } else {
                    coroutineScope.launch {
                        userPreferences.setTutorialCompleted(true)
                    }
                }
            },
            onSkipTutorial = {
                coroutineScope.launch {
                    userPreferences.setTutorialCompleted(true)
                }
            }
        )
    }
}
}

/**
 * Monzo Activity Transaction Row Item
 */
@Composable
fun MonzoActivityItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val formattedDate = remember(transaction.date) {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        sdf.format(Date(transaction.date))
    }

    // Monzo Squircle Icon configuration
    val (iconBg, iconTint, icon) = when {
        isIncome -> Triple(Color(0xFFE8F7F0), MonzoIncomeGreen, Icons.Filled.AccountBalance)
        transaction.title.contains("uber", ignoreCase = true) || transaction.title.contains("gojek", ignoreCase = true) ->
            Triple(Color(0xFF111111), Color.White, Icons.Filled.DirectionsCar)
        transaction.title.contains("makan", ignoreCase = true) || transaction.title.contains("resto", ignoreCase = true) ->
            Triple(Color(0xFFFDECE9), MonzoCoral, Icons.Filled.Fastfood)
        else -> Triple(Color(0xFF142232), Color.White, Icons.AutoMirrored.Filled.ReceiptLong)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Monzo Squircle Icon
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(13.dp),
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MonzoTextPrimary,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.accountName} • $formattedDate",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MonzoTextSecondary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Monzo Amount (+20.00 in Green for Income, 47.00 in Navy for Expense)
        Text(
            text = if (isIncome) "+${formatRupiah(transaction.amount, false)}" else formatRupiah(transaction.amount, false),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = if (isIncome) MonzoIncomeGreen else MonzoTextPrimary,
                fontSize = 16.sp
            )
        )
    }
}

/**
 * Monzo Suggested Actions Card
 */
@Composable
private fun MonzoActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(140.dp)
            .height(145.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MonzoSurface,
        border = BorderStroke(1.dp, MonzoBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Illustrated Square Box
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = containerColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MonzoTextPrimary,
                        fontSize = 12.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MonzoTextSecondary,
                        fontSize = 10.5.sp,
                        lineHeight = 13.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


/**
 * Model data arus kas harian untuk grafik Home
 */
data class DayCashFlow(
    val label: String,
    val income: Double,
    val expense: Double
)

/**
 * Visual Dual-Bar Chart Arus Kas Mingguan untuk Home
 */
@Composable
private fun HomeCashFlowBarChart(
    data: List<DayCashFlow>,
    selectedIndex: Int,
    onBarSelected: (Int) -> Unit
) {
    if (data.isEmpty()) return

    val maxAmount = remember(data) {
        val highest = data.maxOfOrNull { kotlin.math.max(it.income, it.expense) } ?: 0.0
        if (highest <= 0.0) 100000.0 else highest
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalSlots = data.size
            if (totalSlots == 0) return@Canvas

            val chartWidth = size.width
            val chartHeight = size.height - 20.dp.toPx()
            val slotWidth = chartWidth / totalSlots
            val barWidth = (slotWidth * 0.30f).coerceAtMost(14.dp.toPx())
            val barSpacing = 2.5.dp.toPx()

            // 1. Garis Grid Horizontal Tipis
            val gridColor = Color(0xFFEFF2EE)
            for (g in 1..2) {
                val y = chartHeight * (g / 2.5f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // 2. Gambar Batang Pemasukan & Pengeluaran
            data.forEachIndexed { index, item ->
                val centerX = slotWidth * index + slotWidth / 2f
                val incomeX = centerX - barWidth - (barSpacing / 2f)
                val expenseX = centerX + (barSpacing / 2f)

                val incomeHeightPx = if (item.income > 0) {
                    ((item.income / maxAmount) * (chartHeight * 0.88f)).toFloat().coerceAtLeast(3.dp.toPx())
                } else 2.dp.toPx()

                val expenseHeightPx = if (item.expense > 0) {
                    ((item.expense / maxAmount) * (chartHeight * 0.88f)).toFloat().coerceAtLeast(3.dp.toPx())
                } else 2.dp.toPx()

                val incomeTopY = chartHeight - incomeHeightPx
                val expenseTopY = chartHeight - expenseHeightPx
                val cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
                val isSelected = index == selectedIndex

                // Highlight kolom jika dipilih
                if (isSelected) {
                    drawRoundRect(
                        color = Color(0xFFE9F2EC),
                        topLeft = Offset(slotWidth * index + 2.dp.toPx(), 0f),
                        size = Size(slotWidth - 4.dp.toPx(), chartHeight + 16.dp.toPx()),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                // Bar Pemasukan (Hijau)
                drawRoundRect(
                    color = if (item.income > 0) MonzoIncomeGreen else Color(0xFFD7E5DB),
                    topLeft = Offset(incomeX, incomeTopY),
                    size = Size(barWidth, incomeHeightPx),
                    cornerRadius = cornerRadius
                )

                // Bar Pengeluaran (Merah Coral)
                drawRoundRect(
                    color = if (item.expense > 0) MonzoExpenseRed else Color(0xFFEAD8D5),
                    topLeft = Offset(expenseX, expenseTopY),
                    size = Size(barWidth, expenseHeightPx),
                    cornerRadius = cornerRadius
                )

                // Label Hari Sumbu X
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = if (isSelected) 0xFF147B96.toInt() else 0xFF6E8092.toInt()
                        textSize = 9.5.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isSelected
                        isAntiAlias = true
                    }
                    drawText(item.label, centerX, chartHeight + 14.dp.toPx(), paint)
                }
            }
        }

        // Invisible touch interceptors
        Row(modifier = Modifier.fillMaxSize()) {
            data.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onBarSelected(if (selectedIndex == index) -1 else index)
                        }
                )
            }
        }
    }
}

/**
 * Compatibility alias for TransactionsScreen
 */
@Composable
fun TransactionRowItem(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    MonzoActivityItem(transaction = transaction, modifier = modifier)
}

/**
 * EMV Chip realistis ala kartu fisik Monzo
 */
@Composable
fun EmvChip(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(width = 44.dp, height = 34.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFC8CFD7),
        border = BorderStroke(1.dp, Color(0xFFA5B0BD))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeW = 1.dp.toPx()
            val lineColor = Color(0xFF7A889B)

            // Garis sirkuit horizontal tengah
            drawLine(lineColor, Offset(0f, h * 0.48f), Offset(w, h * 0.48f), strokeW)
            // Garis sirkuit vertikal tengah
            drawLine(lineColor, Offset(w * 0.48f, 0f), Offset(w * 0.48f, h), strokeW)

            // Kotak chip tengah
            drawRoundRect(
                color = lineColor,
                topLeft = Offset(w * 0.26f, h * 0.22f),
                size = Size(w * 0.48f, h * 0.56f),
                cornerRadius = CornerRadius(2.dp.toPx()),
                style = Stroke(strokeW)
            )
            // Sirkuit samping
            drawLine(lineColor, Offset(w * 0.26f, h * 0.35f), Offset(0f, h * 0.35f), strokeW)
            drawLine(lineColor, Offset(w * 0.74f, h * 0.35f), Offset(w, h * 0.35f), strokeW)
            drawLine(lineColor, Offset(w * 0.26f, h * 0.62f), Offset(0f, h * 0.62f), strokeW)
            drawLine(lineColor, Offset(w * 0.74f, h * 0.62f), Offset(w, h * 0.62f), strokeW)
        }
    }
}

/**
 * Contactless Wave / NFC wave indicator
 */
@Composable
fun ContactlessWave(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 24.dp, height = 26.dp)) {
        val strokeW = 2.2.dp.toPx()
        val color = Color.White
        val radii = listOf(5.dp.toPx(), 9.5.dp.toPx(), 14.dp.toPx(), 18.5.dp.toPx())
        radii.forEach { r ->
            drawArc(
                color = color,
                startAngle = -42f,
                sweepAngle = 84f,
                useCenter = false,
                topLeft = Offset(-r + 2.dp.toPx(), size.height / 2 - r),
                size = Size(r * 2, r * 2),
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

/**
 * Branding data untuk tipe wallet
 */
data class WalletBranding(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color = MonzoCoral
)

fun getWalletBranding(account: Account?): WalletBranding {
    val name = account?.name?.lowercase() ?: ""
    val type = account?.type?.uppercase() ?: "CASH"

    return when {
        name.contains("dana") -> WalletBranding("DANA", "DOMPET DIGITAL", Icons.Filled.AccountBalanceWallet, Color(0xFF118EEA))
        name.contains("gopay") -> WalletBranding("GOPAY", "DOMPET DIGITAL", Icons.Filled.AccountBalanceWallet, Color(0xFF00AED6))
        name.contains("ovo") -> WalletBranding("OVO", "DOMPET DIGITAL", Icons.Filled.AccountBalanceWallet, Color(0xFF4C2A86))
        name.contains("shopee") -> WalletBranding("SHOPEE", "DOMPET DIGITAL", Icons.Filled.AccountBalanceWallet, Color(0xFFEE4D2D))
        name.contains("bri") -> WalletBranding("BRI", "BANK", Icons.Filled.AccountBalance, Color(0xFF00529C))
        name.contains("bca") -> WalletBranding("BCA", "BANK", Icons.Filled.AccountBalance, Color(0xFF005EAA))
        name.contains("mandiri") -> WalletBranding("MANDIRI", "BANK", Icons.Filled.AccountBalance, Color(0xFF003D79))
        name.contains("bni") -> WalletBranding("BNI", "BANK", Icons.Filled.AccountBalance, Color(0xFFF15A24))
        name.contains("jago") -> WalletBranding("JAGO", "BANK", Icons.Filled.AccountBalance, Color(0xFFF99D1C))
        type == "BANK" -> WalletBranding(account?.name?.uppercase() ?: "BANK", "BANK", Icons.Filled.AccountBalance, Color(0xFF1B365D))
        type == "E_WALLET" -> WalletBranding(account?.name?.uppercase() ?: "DOMPET", "DOMPET DIGITAL", Icons.Filled.AccountBalanceWallet, MonzoTeal)
        type == "INVESTMENT" -> WalletBranding(account?.name?.uppercase() ?: "INVESTASI", "INVESTASI", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF1B4D3E))
        else -> WalletBranding(account?.name?.uppercase() ?: "TUNAI", "TUNAI", Icons.Filled.Money, MonzoNavy)
    }
}

/**
 * Badge tipe wallet pengganti logo Mastercard (Double Overlapping Circle Emblem)
 */
@Composable
fun WalletTypeBadge(
    branding: WalletBranding,
    cardColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val iconColor = cardColor ?: branding.accentColor
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        // Double Overlapping Circle Emblem ala logo kartu Mastercard
        Box(
            modifier = Modifier.size(width = 38.dp, height = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Lingkaran 1
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White.copy(alpha = 0.35f), CircleShape)
            )
            // Lingkaran 2
            Box(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .size(24.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = branding.icon,
                    contentDescription = branding.title,
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Teks Nama Wallet & Tipe
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = branding.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1
            )
            Text(
                text = branding.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 8.5.sp
                ),
                maxLines = 1
            )
        }
    }
}

/**
 * Model kartu dalam tumpukan stack
 */
data class CardStackItem(
    val id: String,
    val isCumulative: Boolean,
    val title: String,
    val balance: Double,
    val cardholderName: String,
    val branding: WalletBranding,
    val cardColor: Color,
    val account: Account? = null
)

/**
 * Factory untuk membuat daftar kartu stack (Kumulatif + Dompet Individual)
 */
fun buildCardStack(
    allAccounts: List<Account>,
    totalBalance: Double,
    userName: String
): List<CardStackItem> {
    val userDisplayName = userName.ifBlank { "BUDI" }.uppercase()
    val cumulativeCard = CardStackItem(
        id = "cumulative",
        isCumulative = true,
        title = "Total Saldo",
        balance = totalBalance,
        cardholderName = userDisplayName,
        branding = WalletBranding("KUMULATIF", "TOTAL SALDO", Icons.Filled.AccountBalanceWallet),
        cardColor = MonzoCoral
    )

    val accountCards = if (allAccounts.isEmpty()) {
        listOf(
            CardStackItem(
                id = "flex_default",
                isCumulative = false,
                title = "KasKu Tunai",
                balance = 0.0,
                cardholderName = userDisplayName,
                branding = WalletBranding("TUNAI", "KASKU TUNAI", Icons.Filled.Money),
                cardColor = MonzoNavy
            )
        )
    } else {
        allAccounts.mapIndexed { index, acc ->
            val branding = getWalletBranding(acc)
            val cardColor = when {
                acc.type.equals("CASH", ignoreCase = true) || 
                acc.name.contains("Tunai", ignoreCase = true) || 
                acc.name.contains("Cash", ignoreCase = true) -> MonzoNavy // Midnight Navy ala kartu Monzo Flex

                acc.type.equals("E_WALLET", ignoreCase = true) || 
                acc.name.contains("DANA", ignoreCase = true) || 
                acc.name.contains("GOPAY", ignoreCase = true) || 
                acc.name.contains("OVO", ignoreCase = true) -> MonzoTeal

                acc.type.equals("BANK", ignoreCase = true) || 
                acc.name.contains("BRI", ignoreCase = true) || 
                acc.name.contains("BCA", ignoreCase = true) || 
                acc.name.contains("Bank", ignoreCase = true) -> Color(0xFF1B365D)

                acc.type.equals("INVESTMENT", ignoreCase = true) -> Color(0xFF1B4D3E)

                else -> MonzoNavy
            }
            CardStackItem(
                id = "account_${acc.id}",
                isCumulative = false,
                title = acc.name,
                balance = acc.balance,
                cardholderName = userDisplayName,
                branding = branding,
                cardColor = cardColor,
                account = acc
            )
        }
    }

    // Kartu awal SELALU kartu Merah (Kumulatif)
    return listOf(cumulativeCard) + accountCards
}

/**
 * Layout Kartu Belakang yang Mengintip (Header Bersih ala Monzo)
 * Menampilkan nama dompet & icon di kiri, serta nominal saldo di kanan tanpa chip yang terpotong.
 */
@Composable
fun MonzoBackCardLayout(
    card: CardStackItem,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(195.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x33142232),
                spotColor = Color(0x24142232)
            ),
        shape = RoundedCornerShape(22.dp),
        color = card.cardColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Baris Header Bersih (Terlihat rapi saat mengintip di atas kartu depan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = card.branding.icon,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (card.isCumulative) "Total Saldo KasKu" else card.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            letterSpacing = 0.2.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = formatRupiah(card.balance, false),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.5.sp
                    )
                )
            }

            // Elemen bagian bawah kartu (terlihat saat kartu depan ditarik turun saat swipe)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EmvChip()
                        ContactlessWave()
                    }
                    Text(
                        text = "KasKu",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 22.sp
                        )
                    )
                }

                Text(
                    text = "••••  ••••  ••••  ••••",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 2.sp,
                        fontSize = 15.sp
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = card.cardholderName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        letterSpacing = 1.2.sp
                    )
                )
                WalletTypeBadge(branding = card.branding)
            }
        }
    }
}

/**
 * Reusable Card Layout for Monzo Physical Card Style (Kartu Fisik Monzo KasKu)
 */
@Composable
fun MonzoPhysicalCardLayout(
    card: CardStackItem,
    isBalanceVisible: Boolean,
    onToggleBalanceVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(195.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x3D142232),
                spotColor = Color(0x29142232)
            ),
        shape = RoundedCornerShape(22.dp),
        color = card.cardColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP ROW: EMV Chip + Contactless Wave (Kiri) & "KasKu" Brand (Kanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EmvChip()
                    ContactlessWave()
                }

                Text(
                    text = "KasKu",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        fontSize = 24.sp
                    )
                )
            }

            // 2. MIDDLE ROW: Saldo & Embossed Masked Card Number
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBalanceVisible) formatRupiah(card.balance) else "Rp ••••••",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 21.sp
                        )
                    )
                    IconButton(
                        onClick = onToggleBalanceVisibility,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isBalanceVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Tampilkan atau Sembunyikan Saldo",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Embossed Card Number Masked
                Text(
                    text = "••••  ••••  ••••  ••••",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.95f),
                        letterSpacing = 2.sp,
                        fontSize = 15.sp
                    )
                )
            }

            // 3. BOTTOM ROW: Nama Pemilik Akun (Kiri Bawah) & Logo Tipe Wallet (Kanan Bawah)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = card.cardholderName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.2.sp,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                WalletTypeBadge(branding = card.branding)
            }
        }
    }
}

