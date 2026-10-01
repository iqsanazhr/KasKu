package com.example.kasku.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.ui.theme.CeramicBackground

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isAiSpecial: Boolean = false
) {
    object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Transactions : Screen("transactions", "Riwayat", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz)
    object Scanner : Screen("scanner", "Scan", Icons.Filled.DocumentScanner, Icons.Outlined.DocumentScanner, isAiSpecial = true)
    object Insights : Screen("insights", "Tren", Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp)
    object AiChat : Screen("ai_chat", "KasKu AI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, isAiSpecial = true)
    object Settings : Screen("settings", "Setelan", Icons.Filled.Settings, Icons.Outlined.Settings)
    object TransactionDetail : Screen("transaction_detail/{transactionId}", "Detail", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz) {
        fun createRoute(transactionId: Long): String = "transaction_detail/$transactionId"
    }
}

val navItems = listOf(
    Screen.Dashboard,
    Screen.Transactions,
    Screen.Scanner,
    Screen.Insights,
    Screen.Settings
)

/**
 * Telegram iOS Solid Capsule Bottom Bar
 * - Bentuk Kapsul Mengambang (Floating Pill Capsule)
 * - Solid Opaque (100% Tidak Transparan) sehingga teks & kartu di belakang tertutup rapat
 * - Border kontras tegas (1.2 dp) & Shadow 16 dp agar menu tetap terpisah jelas di atas latar putih
 * - Tata letak 1:1 Telegram: Ikon di atas, teks nama menu di bawahnya
 * - Aksen aktif: Telegram Light Blue (#0088CC) dengan background pill lembut (#EBF5FB)
 * - Warna inaktif: Slate 500 (#64748B) untuk keterbacaan tajam
 */
@Composable
fun TelegramLiquidGlassBottomBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val telegramLightBlue = Color(0xFF0088CC) // Telegram iOS Light Blue
    val telegramInactive = Color(0xFF64748B)  // Slate 500: Tajam & terbaca jelas

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Kapsul Solid (Tidak transparan, kontras tegas di latar putih)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = Color(0x30000000),
                    spotColor = Color(0x40000000)
                ),
            shape = RoundedCornerShape(36.dp),
            color = Color.White, // 100% Solid Putih Opaque (Tidak tembus pandang)
            border = BorderStroke(0.5.dp, Color(0xFFD1D9E0)) // Hairline border ultra-tipis (0.5 dp)
        ) {
            // Susunan 1:1 Tab Menu Telegram (Ikon di atas, nama menu di bawah)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.88f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "tabScale_${screen.route}"
                    )

                    val itemColor by animateColorAsState(
                        targetValue = if (isSelected) telegramLightBlue else telegramInactive,
                        animationSpec = spring(stiffness = Spring.StiffnessLow),
                        label = "tabColor_${screen.route}"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                if (isSelected) Color(0xFFEBF5FB) else Color.Transparent
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onNavigate(screen) }
                            )
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Logo / Ikon Menu di Atas
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title,
                            tint = itemColor,
                            modifier = Modifier.size(23.dp)
                        )

                        Spacer(modifier = Modifier.height(2.5.dp))

                        // Nama Menu di Bawah Logo
                        Text(
                            text = screen.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = (-0.2).sp
                            ),
                            color = itemColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
