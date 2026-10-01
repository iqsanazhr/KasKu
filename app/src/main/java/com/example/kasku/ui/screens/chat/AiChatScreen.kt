package com.example.kasku.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.ui.screens.insights.AiInsightsViewModel
import com.example.kasku.ui.screens.insights.ChatMessage
import com.example.kasku.ui.screens.insights.ChatSender
import com.example.kasku.ui.theme.MonzoBackground
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiChatScreen(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AiInsightsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AiInsightsViewModel.Factory(repository, aiService, aiPreferences)
    )
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAnsweringQuestion by viewModel.isAnsweringQuestion.collectAsState()

    var inputMessage by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val chatListState = rememberLazyListState()

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MonzoBackground)
            .imePadding()
    ) {
        // ==========================================
        // 1. TOP HEADER APP BAR KASKU AI
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    ambientColor = Color(0x08000000),
                    spotColor = Color(0x10000000)
                ),
            color = MonzoSurface,
            border = BorderStroke(0.5.dp, MonzoBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke Beranda",
                                tint = MonzoTextPrimary
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.size(38.dp),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "KasKu AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MonzoTextPrimary,
                                    fontSize = 17.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "Online",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = "Asisten Keuangan Cerdas Pribadi",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MonzoTextSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                // Tombol Bersihkan Chat
                IconButton(
                    onClick = { viewModel.clearChat() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Bersihkan Chat",
                        tint = MonzoTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ==========================================
        // 2. DAFTAR PESAN CHAT (LAZYCOLUMN)
        // ==========================================
        LazyColumn(
            state = chatListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages, key = { it.id }) { msg ->
                ChatBubbleItem(message = msg)
            }

            if (isAnsweringQuestion) {
                item {
                    TypingIndicatorItem()
                }
            }
        }

        // ==========================================
        // 3. REKOMENDASI PERTANYAAN CEPAT (CHIPS)
        // ==========================================
        AnimatedVisibility(visible = !isKeyboardOpen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickQuestions.forEach { question ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isAnsweringQuestion) {
                                viewModel.sendMessage(question)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2F6F3),
                        border = BorderStroke(1.dp, Color(0xFFDFE6E1))
                    ) {
                        Text(
                            text = question,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
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

        // ==========================================
        // 4. BARIS INPUT PESAN DI BAGIAN BAWAH
        // ==========================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = MonzoSurface,
            border = BorderStroke(0.5.dp, MonzoBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text(
                            text = "Tanya seputar pengeluaran, tips hemat...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MonzoTextSecondary,
                                fontSize = 12.5.sp
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MonzoTeal,
                        unfocusedBorderColor = Color(0xFFDFE7E1),
                        focusedContainerColor = Color(0xFFFBFDFB),
                        unfocusedContainerColor = Color(0xFFFBFDFB)
                    ),
                    singleLine = true,
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

                // Tombol Kirim
                Surface(
                    modifier = Modifier
                        .size(46.dp)
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
                    color = if (inputMessage.isNotBlank() && !isAnsweringQuestion) MonzoTeal else Color(0xFFD1DAD3)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isAnsweringQuestion) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bubble item percakapan untuk Pengguna dan AI
 */
@Composable
private fun ChatBubbleItem(message: ChatMessage) {
    val isUser = message.sender == ChatSender.USER
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Mini AI Avatar
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
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Surface(
                shape = if (isUser) {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                },
                color = if (isUser) MonzoTeal else Color.White,
                border = if (isUser) null else BorderStroke(1.dp, Color(0xFFE2E9E3)),
                shadowElevation = if (isUser) 0.dp else 1.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isUser) Color.White else MonzoTextPrimary,
                        lineHeight = 19.sp,
                        fontSize = 13.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MonzoTextSecondary.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

/**
 * Animasi Typing Indicator saat AI sedang menyusun jawaban
 */
@Composable
private fun TypingIndicatorItem() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
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
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E9E3))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4F46E5).copy(alpha = dotAlpha))
                )
                Text(
                    text = "KasKu AI sedang menganalisis...",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MonzoTextSecondary,
                        fontSize = 11.5.sp
                    )
                )
            }
        }
    }
}
