package com.example.kasku.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// PURE MONZO BANKING COLOR PALETTE
// ==========================================

// 1. Monzo Canvas & Surfaces
val MonzoBackground = Color(0xFFEFF4EF)          // Soft warm mint-tinted gray canvas
val MonzoSurface = Color(0xFFFFFFFF)             // Crisp pure white card surface
val MonzoElevated = Color(0xFFE8EFEA)            // Secondary surfaces, chips, action containers
val MonzoBorder = Color(0xFFE5EBE5)              // Subtle 1dp border / dividers
val MonzoCardBorder = Color(0xFFE2E9E3)          // Card borders

// 2. Monzo Signature Hot Coral (Front Debit Card & Danger/Expense)
val MonzoCoral = Color(0xFFEB5B44)               // Iconic Monzo Hot Coral
val MonzoCoralDark = Color(0xFF8C2C1E)           // Dark coral shadow / pressed
val MonzoCoralPill = Color(0xFFA13426)           // Pill button inside Coral card
val MonzoCoralPillLight = Color(0xFFFDECE9)      // Soft coral container

// 3. Monzo Midnight Navy (Flex Card & Header Accents)
val MonzoNavy = Color(0xFF142232)                // Midnight Navy for Flex Card
val MonzoNavyDark = Color(0xFF0C1622)
val MonzoNavyLight = Color(0xFF1D2E42)

// 4. Monzo Interactive Teal (Primary Actions, Tabs, Icons, Pills)
val MonzoTeal = Color(0xFF147B96)                // Monzo Interactive Blue/Teal
val MonzoTealLight = Color(0xFFE2F3F6)           // Soft cyan/teal pill background (Greeting / Upgrade pill)
val MonzoTealBorder = Color(0xFFB3DFE8)          // Border for the greeting pill
val MonzoTealDark = Color(0xFF0E5C70)

// 5. Monzo Olive Avatar
val MonzoAvatarBg = Color(0xFF2D4436)            // Monzo User Avatar Dark Olive/Green
val MonzoAvatarText = Color(0xFFFFFFFF)

// 6. Typography Colors
val MonzoTextPrimary = Color(0xFF142232)         // Deep midnight navy text
val MonzoTextSecondary = Color(0xFF6E8092)       // Muted slate text
val MonzoTextTertiary = Color(0xFFA4B0BD)        // Subtle label text

// 7. Financial Indicators
val MonzoIncomeGreen = Color(0xFF26A66B)         // Monzo Vibrant Green for Income (+20.00)
val MonzoIncomeContainer = Color(0xFFE7F7EF)
val MonzoExpenseRed = Color(0xFFEB5B44)          // Monzo Coral for Expenses

// 8. Suggested Actions Card Background
val MonzoActionCardBg = Color(0xFFEBF7F9)        // Soft light teal/mint card for actions

// ==========================================
// SYSTEM ALIASES (Full Monzo Integration)
// ==========================================
val CeramicBackground = MonzoBackground
val PureWhiteSurface = MonzoSurface
val ElevatedSurface = MonzoElevated
val SubtleBorder = MonzoBorder
val TextPrimary = MonzoTextPrimary
val TextSecondary = MonzoTextSecondary
val TextTertiary = MonzoTextTertiary
val BrandPrimary = MonzoTeal
val BrandPrimaryDark = MonzoTealDark
val OnBrandPrimary = Color.White
val IncomeGreen = MonzoIncomeGreen
val IncomeGreenContainer = MonzoIncomeContainer
val ExpenseRed = MonzoExpenseRed
val ExpenseRedContainer = MonzoCoralPillLight

// Gradients
val MonzoCoralBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFF0654F), Color(0xFFEB5B44))
)
val MonzoNavyBrush = Brush.verticalGradient(
    colors = listOf(Color(0xFF17283C), Color(0xFF121D2C))
)
val NavyHeaderBrush = MonzoNavyBrush
val TokoHeaderBrush = MonzoNavyBrush
val TokoGreen = MonzoTeal
val TokoGreenLight = MonzoTealLight
val TokoGreenDark = MonzoTealDark
val TokoGreenBorder = MonzoBorder
val TokoGreenSurface = MonzoSurface
val AiBrush = MonzoCoralBrush
val AiSurfaceContainer = MonzoTealLight
val AiBorder = MonzoBorder