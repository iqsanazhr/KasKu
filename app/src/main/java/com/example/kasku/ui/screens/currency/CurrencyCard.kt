package com.example.kasku.ui.screens.currency

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kasku.ui.common.UiState
import com.example.kasku.ui.components.formatRupiah
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoIncomeGreen
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import java.util.Locale

/**
 * Komponen Kartu Kurs Mata Uang Real-Time.
 * Memenuhi kriteria standar:
 * - Materi 5: Networking & API (Retrofit REST API open.er-api.com)
 * - Materi 6: Arsitektur MVVM + UiState (Loading, Success, Error)
 */
@Composable
fun CurrencyExchangeCard(
    modifier: Modifier = Modifier,
    viewModel: CurrencyViewModel = viewModel()
) {
    val currencyState by viewModel.currencyState.collectAsState()

    Surface(
        modifier = modifier
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Icon + Title + Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MonzoTeal.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                tint = MonzoTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Kurs Mata Uang (Retrofit)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonzoTextPrimary,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "REST API: open.er-api.com (USD Base)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MonzoTextSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.fetchRates("USD") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang Kurs",
                        tint = MonzoTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // State Handling: Loading, Success, Error, Idle
            when (val state = currencyState) {
                is UiState.Loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = MonzoTeal,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = "Menghubungi REST API via Retrofit...",
                            style = MaterialTheme.typography.bodySmall.copy(color = MonzoTextSecondary)
                        )
                    }
                }

                is UiState.Success -> {
                    val rates = state.data.rates
                    val idrRate = rates["IDR"] ?: 0.0
                    val eurRate = rates["EUR"] ?: 0.0
                    val sgdRate = rates["SGD"] ?: 0.0
                    val myrRate = rates["MYR"] ?: 0.0

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CurrencyRateRow(symbol = "🇺🇸 1 USD ➔ 🇮🇩 IDR", value = formatRupiah(idrRate), isPrimary = true)
                        CurrencyRateRow(symbol = "🇪🇺 1 USD ➔ 🇪🇺 EUR", value = "€ ${String.format(Locale.US, "%.3f", eurRate)}")
                        CurrencyRateRow(symbol = "🇸🇬 1 USD ➔ 🇸🇬 SGD", value = "S$ ${String.format(Locale.US, "%.3f", sgdRate)}")
                        CurrencyRateRow(symbol = "🇲🇾 1 USD ➔ 🇲🇾 MYR", value = "RM ${String.format(Locale.US, "%.3f", myrRate)}")

                        if (state.data.lastUpdateUtc.isNotBlank()) {
                            Text(
                                text = "Pembaruan: ${state.data.lastUpdateUtc.take(16)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonzoTextSecondary.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MonzoCoral,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = MonzoCoral, fontSize = 12.sp)
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.fetchRates("USD") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MonzoTeal)
                        ) {
                            Text("Coba Lagi via Retrofit", fontSize = 12.sp)
                        }
                    }
                }

                UiState.Idle -> {
                    Text(
                        text = "Siap memuat data kurs mata uang.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MonzoTextSecondary)
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencyRateRow(
    symbol: String,
    value: String,
    isPrimary: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isPrimary) MonzoTeal.copy(alpha = 0.07f) else Color(0x05000000),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal,
                color = MonzoTextPrimary,
                fontSize = 12.5.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) MonzoIncomeGreen else MonzoTextPrimary,
                fontSize = 13.sp
            )
        )
    }
}
