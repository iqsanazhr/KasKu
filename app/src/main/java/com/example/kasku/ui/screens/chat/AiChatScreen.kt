package com.example.kasku.ui.screens.chat

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.preferences.UserPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.ui.screens.insights.AiInsightsViewModel
import com.example.kasku.ui.screens.insights.ChatMessage
import com.example.kasku.ui.screens.insights.ChatSender
import com.example.kasku.ui.theme.MonzoBackground
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoElevated
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTealLight
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * KasKu AI Chat Screen - Desain Layout 1:1 Gemini App
 * Dilengkapi Drawer Riwayat Chat di sebelah kiri, Floating Header Minimalis tanpa Badge Online,
 * Manajemen Sesi Tersimpan secara Persisten, dan Tampilan Hero Welcome Khas Gemini.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiChatScreen(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    userPreferences: UserPreferences? = null,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToSettings: (() -> Unit)? = null,
    viewModel: AiInsightsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AiInsightsViewModel.Factory(repository, aiService, aiPreferences)
    )
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAnsweringQuestion by viewModel.isAnsweringQuestion.collectAsState()
    val chatSessions by viewModel.chatSessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val currentModel by aiPreferences.geminiModelFlow.collectAsState(initial = "gemini-2.5-flash")
    val userName by (userPreferences?.userNameFlow ?: remember { kotlinx.coroutines.flow.flowOf("") })
        .collectAsState(initial = "")

    var inputMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val chatListState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val quickQuestions = remember {
        listOf(
            "Evaluasi pengeluaranku bulan ini",
            "Kategori apa yang paling boros?",
            "Berapa sisa uang amanku saat ini?",
            "Tips hemat untuk pekan ini",
            "Apakah pengeluaran makanku wajar?"
        )
    }

    val isKeyboardOpen = WindowInsets.isImeVisible

    // Auto-scroll ke pesan terbaru
    LaunchedEffect(chatMessages.size, isAnsweringQuestion) {
        val target = chatMessages.size + if (isAnsweringQuestion) 1 else 0
        if (target > 0) {
            chatListState.animateScrollToItem(target - 1)
        }
    }

    // Auto-scroll saat keyboard dibuka
    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen && chatMessages.isNotEmpty()) {
            delay(120)
            val target = chatMessages.size + if (isAnsweringQuestion) 1 else 0
            if (target > 0) {
                chatListState.animateScrollToItem(target - 1)
            }
        }
    }

    // ModalNavigationDrawer: Drawer Riwayat Obrolan di sebelah kiri (1:1 Gemini)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MonzoSurface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Drawer: Logo & Brand KasKu AI
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = Color(0xFFEEF2FF)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "KasKu AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = currentModel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonzoTeal,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Tombol "+ Chat Baru" (Gemini Style New Chat Button)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.createNewSession()
                                coroutineScope.launch { drawerState.close() }
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = MonzoTealLight,
                        border = BorderStroke(1.dp, MonzoTeal.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Obrolan Baru",
                                tint = MonzoTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Chat Baru",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTeal,
                                    fontSize = 13.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Judul Section Riwayat Obrolan
                    Text(
                        text = "Riwayat Obrolan",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MonzoTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )

                    // Daftar Sesi Percakapan yang Tersimpan
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (chatSessions.isEmpty()) {
                            item {
                                Text(
                                    text = "Belum ada riwayat percakapan.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else {
                            items(chatSessions, key = { it.id }) { session ->
                                val isSelected = session.id == currentSessionId
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            viewModel.selectSession(session.id)
                                            coroutineScope.launch { drawerState.close() }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MonzoTeal.copy(alpha = 0.12f) else Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                                contentDescription = null,
                                                tint = if (isSelected) MonzoTeal else MonzoTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = session.title,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MonzoTeal else MonzoTextPrimary,
                                                    fontSize = 13.sp
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Tombol Hapus Sesi Satuan
                                        IconButton(
                                            onClick = { viewModel.deleteSession(session.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.DeleteOutline,
                                                contentDescription = "Hapus Sesi",
                                                tint = MonzoTextSecondary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MonzoBorder,
                        thickness = 0.8.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Footer Drawer: Kembali, Pengaturan AI & Hapus Riwayat
                    if (onNavigateBack != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    coroutineScope.launch { drawerState.close() }
                                    onNavigateBack.invoke()
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke Beranda",
                                tint = MonzoTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Kembali ke Beranda",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MonzoTextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                coroutineScope.launch { drawerState.close() }
                                onNavigateToSettings?.invoke()
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Pengaturan AI",
                            tint = MonzoTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Pengaturan AI & Model",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MonzoTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.clearAllHistory()
                                coroutineScope.launch { drawerState.close() }
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = "Hapus Semua Riwayat",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Hapus Semua Riwayat",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    ) {
        // Konten Utama Layar Chat (1:1 Gemini: Bersih, Tanpa Bar Atas Solid, Tanpa Badge Online)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MonzoBackground)
                .imePadding()
        ) {
            // ==========================================
            // 1. TOP FLOATING MINIMAL HEADER (1:1 GEMINI)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Hamburger Menu Tunggal di Kiri (Buka Drawer Riwayat Chat)
                IconButton(
                    onClick = { coroutineScope.launch { drawerState.open() } },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Buka Riwayat Chat",
                        tint = MonzoTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Brand KasKu AI di Tengah (Minimalis & Modern)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "KasKu AI",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary,
                            fontSize = 17.sp
                        )
                    )
                }

                // Tombol Mulai Chat Baru di Kanan Atas
                IconButton(
                    onClick = { viewModel.createNewSession() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Chat Baru",
                        tint = MonzoTeal,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            // ==========================================
            // 2. AREA PESAN CHAT ATAU HERO WELCOME GEMINI
            // ==========================================
            val isWelcomeState = chatMessages.isEmpty() || (chatMessages.size == 1 && chatMessages.first().sender == ChatSender.AI)

            if (isWelcomeState) {
                // Tampilan Hero Welcome Khas Gemini saat Chat Masih Baru
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Ikon Sparkle Bintang Gemini yang Bersinar
                        val infiniteTransition = rememberInfiniteTransition(label = "StarPulse")
                        val starScale by infiniteTransition.animateFloat(
                            initialValue = 0.95f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 1500),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "StarScale"
                        )

                        Surface(
                            modifier = Modifier
                                .size(64.dp)
                                .scale(starScale),
                            shape = CircleShape,
                            color = Color(0xFFEEF2FF),
                            border = BorderStroke(1.dp, Color(0xFFC7D2FE))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Halo, ${userName.ifBlank { "Sobat KasKu" }}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 24.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ada yang bisa saya bantu dengan keuanganmu hari ini?",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 13.5.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Rekomendasi Pertanyaan Awal (Chips Gemini)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickQuestions.take(3).forEach { question ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            viewModel.sendMessage(question)
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = MonzoSurface,
                                    border = BorderStroke(1.dp, MonzoBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = MonzoTeal,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = question,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MonzoTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tampilan Daftar Pesan Berjalan
                LazyColumn(
                    state = chatListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(chatMessages, key = { it.id }) { msg ->
                        GeminiChatBubbleItem(
                            message = msg,
                            onCopyText = {
                                clipboardManager.setText(AnnotatedString(msg.text))
                                Toast.makeText(context, "Pesan disalin", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (isAnsweringQuestion) {
                        item {
                            TypingIndicatorItem()
                        }
                    }
                }
            }

            // Chips Rekomendasi Horizontal (Jika Chat Sudah Berjalan & Keyboard Tertutup)
            if (!isWelcomeState) {
                AnimatedVisibility(visible = !isKeyboardOpen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickQuestions.take(2).forEach { question ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isAnsweringQuestion) {
                                        viewModel.sendMessage(question)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = MonzoSurface,
                                border = BorderStroke(1.dp, MonzoBorder)
                            ) {
                                Text(
                                    text = question,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MonzoTeal,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. FLOATING PILL INPUT BAR (1:1 GEMINI)
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(28.dp),
                color = MonzoSurface,
                border = BorderStroke(1.dp, MonzoBorder),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputMessage,
                        onValueChange = { inputMessage = it },
                        placeholder = {
                            Text(
                                text = "Tanya KasKu AI...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 13.5.sp
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputMessage.isNotBlank() && !isAnsweringQuestion) {
                                    val query = inputMessage
                                    inputMessage = ""
                                    focusManager.clearFocus()
                                    viewModel.sendMessage(query)
                                }
                            }
                        )
                    )

                    // Tombol Kirim Bundar Khas Gemini
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(enabled = inputMessage.isNotBlank() && !isAnsweringQuestion) {
                                if (inputMessage.isNotBlank()) {
                                    val query = inputMessage
                                    inputMessage = ""
                                    focusManager.clearFocus()
                                    viewModel.sendMessage(query)
                                }
                            },
                        shape = CircleShape,
                        color = if (inputMessage.isNotBlank() && !isAnsweringQuestion) MonzoTeal else Color(0xFFE2E8F0)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isAnsweringQuestion) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Kirim",
                                    tint = if (inputMessage.isNotBlank()) Color.White else MonzoTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bubble Chat Khas Gemini (Pesan User & Respon AI dengan Fitur Salin)
 */
@Composable
private fun GeminiChatBubbleItem(
    message: ChatMessage,
    onCopyText: () -> Unit
) {
    val isUser = message.sender == ChatSender.USER
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp),
                shape = CircleShape,
                color = Color(0xFFEEF2FF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(if (isUser) 0.82f else 0.90f)
        ) {
            Surface(
                shape = if (isUser) {
                    RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                },
                color = if (isUser) MonzoTeal else MonzoSurface,
                border = if (isUser) null else BorderStroke(1.dp, MonzoBorder),
                shadowElevation = if (isUser) 0.dp else 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isUser) Color.White else MonzoTextPrimary,
                            lineHeight = 20.sp,
                            fontSize = 13.5.sp
                        )
                    )

                    // Aksi di bawah Pesan AI (Tombol Salin)
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onCopyText() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = "Salin",
                                tint = MonzoTextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Salin",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonzoTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MonzoTextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Indikator AI Sedang Mengetik (Typing Indicator)
 */
@Composable
private fun TypingIndicatorItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = Color(0xFFEEF2FF)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5),
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = MonzoSurface,
            border = BorderStroke(1.dp, MonzoBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "DotsTyping")
                val dot1Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Dot1"
                )
                val dot2Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, delayMillis = 150),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Dot2"
                )
                val dot3Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, delayMillis = 300),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Dot3"
                )

                Box(modifier = Modifier.size(6.dp).background(MonzoTeal.copy(alpha = dot1Alpha), CircleShape))
                Box(modifier = Modifier.size(6.dp).background(MonzoTeal.copy(alpha = dot2Alpha), CircleShape))
                Box(modifier = Modifier.size(6.dp).background(MonzoTeal.copy(alpha = dot3Alpha), CircleShape))

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "KasKu AI sedang memproses...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MonzoTextSecondary,
                        fontSize = 11.5.sp
                    )
                )
            }
        }
    }
}
