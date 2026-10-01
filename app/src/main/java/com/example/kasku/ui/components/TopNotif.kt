package com.example.kasku.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tipe notifikasi TopNotif
 */
enum class TopNotifType {
    SUCCESS,
    ERROR,
    INFO,
    WARNING
}

/**
 * Model data payload TopNotif
 */
data class TopNotifData(
    val title: String,
    val message: String? = null,
    val type: TopNotifType = TopNotifType.SUCCESS,
    val icon: ImageVector? = null,
    val durationMs: Long = 3500L,
    val id: Long = System.currentTimeMillis()
)

/**
 * Pengontrol Notifikasi Global KasKu (TopNotif)
 */
object TopNotif {
    private val _currentNotification = MutableStateFlow<TopNotifData?>(null)
    val currentNotification: StateFlow<TopNotifData?> = _currentNotification.asStateFlow()

    private var autoDismissJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun show(
        title: String,
        message: String? = null,
        type: TopNotifType = TopNotifType.SUCCESS,
        icon: ImageVector? = null,
        durationMs: Long = 3500L
    ) {
        autoDismissJob?.cancel()
        val data = TopNotifData(
            title = title,
            message = message,
            type = type,
            icon = icon,
            durationMs = durationMs,
            id = System.currentTimeMillis()
        )
        _currentNotification.value = data

        autoDismissJob = scope.launch {
            delay(durationMs)
            if (_currentNotification.value?.id == data.id) {
                _currentNotification.value = null
            }
        }
    }

    fun dismiss() {
        autoDismissJob?.cancel()
        _currentNotification.value = null
    }

    fun showSuccess(title: String, message: String? = null, durationMs: Long = 3500L) {
        show(title = title, message = message, type = TopNotifType.SUCCESS, durationMs = durationMs)
    }

    fun showError(title: String, message: String? = null, durationMs: Long = 4200L) {
        show(title = title, message = message, type = TopNotifType.ERROR, durationMs = durationMs)
    }

    fun showInfo(title: String, message: String? = null, durationMs: Long = 3500L) {
        show(title = title, message = message, type = TopNotifType.INFO, durationMs = durationMs)
    }

    fun showWarning(title: String, message: String? = null, durationMs: Long = 3800L) {
        show(title = title, message = message, type = TopNotifType.WARNING, durationMs = durationMs)
    }
}

/**
 * Komponen Host Notifikasi Melayang Atas (TopNotifHost)
 */
@Composable
fun TopNotifHost(
    modifier: Modifier = Modifier
) {
    val notification by TopNotif.currentNotification.collectAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            .zIndex(9999f),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = notification != null,
            enter = slideInVertically(
                initialOffsetY = { -it - 80 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.82f),
            exit = slideOutVertically(
                targetOffsetY = { -it - 80 },
                animationSpec = tween(260)
            ) + fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.85f)
        ) {
            notification?.let { data ->
                val (badgeBgColor, badgeIconTint, defaultIcon) = when (data.type) {
                    TopNotifType.SUCCESS -> Triple(
                        Color(0xFF22C55E), // Emerald Green
                        Color.White,
                        Icons.Filled.Check
                    )
                    TopNotifType.ERROR -> Triple(
                        Color(0xFFEF4444), // Crimson Red
                        Color.White,
                        Icons.Filled.ErrorOutline
                    )
                    TopNotifType.WARNING -> Triple(
                        Color(0xFFF59E0B), // Amber
                        Color.White,
                        Icons.Filled.WarningAmber
                    )
                    TopNotifType.INFO -> Triple(
                        Color(0xFF0EA5E9), // Sky Blue
                        Color.White,
                        Icons.Filled.AutoAwesome
                    )
                }

                val displayIcon = data.icon ?: defaultIcon

                // Surface Kapsul Bubble Notifikasi Atas
                Surface(
                    modifier = Modifier
                        .widthIn(min = 220.dp, max = 390.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = Color(0x70000000),
                            ambientColor = Color(0x35000000)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { TopNotif.dismiss() }
                        ),
                    shape = RoundedCornerShape(32.dp),
                    color = Color(0xFF0F172A), // Deep Night Slate OLED
                    border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.85f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ikon Indikator Bulat
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(badgeBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = displayIcon,
                                    contentDescription = null,
                                    tint = badgeIconTint,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Konten Teks Notifikasi
                            Column(
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = data.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (!data.message.isNullOrBlank()) {
                                    Text(
                                        text = data.message,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFFCBD5E1),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 11.5.sp,
                                            lineHeight = 14.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Tombol Tutup Kecil
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .clickable { TopNotif.dismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Tutup",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
