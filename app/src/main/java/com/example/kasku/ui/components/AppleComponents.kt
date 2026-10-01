package com.example.kasku.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoElevated
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTealBorder
import com.example.kasku.ui.theme.MonzoTealLight
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import java.text.NumberFormat
import java.util.Locale

object CurrencyConfig {
    var currentCurrency by mutableStateOf("IDR")

    val supportedCurrencies = listOf(
        "IDR" to ("Rupiah Indonesia" to "Rp"),
        "USD" to ("US Dollar" to "$"),
        "EUR" to ("Euro" to "€"),
        "SGD" to ("Singapore Dollar" to "S$"),
        "MYR" to ("Ringgit Malaysia" to "RM"),
        "JPY" to ("Japanese Yen" to "¥"),
        "GBP" to ("British Pound" to "£"),
        "AUD" to ("Australian Dollar" to "A$"),
        "SAR" to ("Saudi Riyal" to "SR")
    )

    fun getSymbol(code: String = currentCurrency): String {
        return when (code.uppercase()) {
            "IDR" -> "Rp "
            "USD" -> "$ "
            "EUR" -> "€ "
            "SGD" -> "S$ "
            "MYR" -> "RM "
            "JPY" -> "¥ "
            "GBP" -> "£ "
            "AUD" -> "A$ "
            "SAR" -> "SR "
            else -> "$code "
        }
    }
}

// Multi-Currency & Rupiah Formatter
fun formatRupiah(amount: Double, withPrefix: Boolean = true, currencyCode: String? = null): String {
    val code = currencyCode ?: CurrencyConfig.currentCurrency
    val locale = when (code.uppercase()) {
        "IDR" -> Locale("id", "ID")
        "USD" -> Locale.US
        "EUR" -> Locale.GERMANY
        "SGD" -> Locale("en", "SG")
        "MYR" -> Locale("ms", "MY")
        "JPY" -> Locale.JAPAN
        "GBP" -> Locale.UK
        "AUD" -> Locale("en", "AU")
        "SAR" -> Locale("ar", "SA")
        else -> Locale.US
    }
    val format = NumberFormat.getNumberInstance(locale)
    if (code.uppercase() == "IDR" || code.uppercase() == "JPY") {
        format.maximumFractionDigits = 0
    } else {
        format.maximumFractionDigits = 2
    }
    val formattedNumber = format.format(amount)
    return if (withPrefix) "${CurrencyConfig.getSymbol(code)}$formattedNumber" else formattedNumber
}

fun formatCurrency(amount: Double, withPrefix: Boolean = true, currencyCode: String? = null): String {
    return formatRupiah(amount, withPrefix, currencyCode)
}

/**
 * Monzo Banking Card with 1dp subtle border and crisp white surface.
 */
@Composable
fun AppleCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color = MonzoSurface,
    borderColor: Color = MonzoBorder,
    elevation: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(elevation, shape = RoundedCornerShape(cornerRadius), ambientColor = Color(0x08000000), spotColor = Color(0x10000000))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

/**
 * Monzo Teal Pill Badge
 */
@Composable
fun AppleAiBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier
            .border(BorderStroke(1.dp, MonzoTealBorder), shape = RoundedCornerShape(100.dp))
            .clip(RoundedCornerShape(100.dp)),
        color = MonzoTealLight
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MonzoTeal,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Text(
                    text = "✨",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MonzoTeal,
                    letterSpacing = 0.3.sp
                )
            )
        }
    }
}

/**
 * Monzo Segmented Control with smooth pill indicator.
 */
@Composable
fun <T> AppleSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    labelProvider: (T) -> String
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = MonzoElevated
    ) {
        Row(
            modifier = Modifier
                .padding(3.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                val animElevation by animateDpAsState(
                    targetValue = if (isSelected) 2.dp else 0.dp,
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "elev"
                )
                val targetBg = if (isSelected) MonzoSurface else Color.Transparent
                val targetTextColor = if (isSelected) MonzoTextPrimary else MonzoTextSecondary

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .shadow(animElevation, RoundedCornerShape(11.dp), clip = false)
                        .background(targetBg, RoundedCornerShape(11.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onItemSelected(item)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            color = targetTextColor
                        )
                    )
                }
            }
        }
    }
}

/**
 * Monzo Solid Pill Button (Teal Primary or Elevated Secondary)
 */
@Composable
fun AppleButton(
    text: String,
    onClick: UnitFunction = {},
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true
) {
    val bg = if (isPrimary) MonzoTeal else MonzoElevated
    val contentColor = if (isPrimary) Color.White else MonzoTextPrimary

    Surface(
        modifier = modifier
            .height(48.dp)
            .shadow(if (isPrimary) 2.dp else 0.dp, RoundedCornerShape(100.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(100.dp),
        color = if (enabled) bg else bg.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )
            )
        }
    }
}

private typealias UnitFunction = () -> Unit
