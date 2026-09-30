package com.example.kasku.ui.screens.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.ui.components.formatRupiah
import com.example.kasku.ui.theme.MonzoBackground
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoExpenseRed
import com.example.kasku.ui.theme.MonzoIncomeGreen
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import kotlin.math.max

@Composable
fun AiInsightsScreen(
    repository: KasKuRepository,
    aiService: AiService,
    aiPreferences: AiPreferences,
    modifier: Modifier = Modifier,
    viewModel: AiInsightsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = AiInsightsViewModel.Factory(repository, aiService, aiPreferences)
    )
) {
    val transactions by viewModel.transactions.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    val chartData = remember(transactions, selectedPeriod) {
        when (selectedPeriod) {
            ChartPeriod.WEEKLY -> viewModel.getWeeklyData(transactions)
            ChartPeriod.MONTHLY -> viewModel.getMonthlyData(transactions)
        }
    }

    val categorySpendings = remember(transactions, selectedPeriod) {
        viewModel.getCategorySpending()
    }

    val totalIncome = remember(chartData) { chartData.sumOf { it.income } }
    val totalExpense = remember(chartData) { chartData.sumOf { it.expense } }
    val netCashFlow = totalIncome - totalExpense

    var selectedBarIndex by remember(selectedPeriod) { mutableIntStateOf(-1) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MonzoBackground)
    ) {
        // ==========================================
        // MAIN SCROLLABLE CONTENT
        // ==========================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. TOP HEADER TITLE (Selaras dengan Riwayat & Pengaturan)
            // ==========================================
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Column {
                    Text(
                        text = "Grafik & Analisis",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MonzoTextSecondary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Tren & Analitik Arus Kas",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary,
                            fontSize = 23.sp
                        )
                    )
                }
            }

            // ==========================================
            // 2. KARTU GRAFIK MINGGUAN & BULANAN (PALING ATAS)
            // ==========================================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0x0A000000),
                            spotColor = Color(0x14000000)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = MonzoSurface,
                    border = BorderStroke(1.dp, MonzoBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header Grafik & Periode Toggle
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
                                    text = if (selectedPeriod == ChartPeriod.WEEKLY) "7 Hari Terakhir" else "4 Pekan Terakhir",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            // Periode Switcher (Mingguan / Bulanan)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF1F4F1),
                                border = BorderStroke(1.dp, Color(0xFFE2E8E3))
                            ) {
                                Row(modifier = Modifier.padding(3.dp)) {
                                    ChartPeriod.values().forEach { period ->
                                        val isSelected = selectedPeriod == period
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(11.dp))
                                                .clickable {
                                                    viewModel.setChartPeriod(period)
                                                    selectedBarIndex = -1
                                                },
                                            shape = RoundedCornerShape(11.dp),
                                            color = if (isSelected) MonzoTeal else Color.Transparent
                                        ) {
                                            Text(
                                                text = period.label,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else MonzoTextSecondary,
                                                    fontSize = 11.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Status & Ringkasan Angka Arus Kas
                        val isSurplus = netCashFlow >= 0
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
                                    text = "Masuk: +${formatRupiah(totalIncome, false)}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoIncomeGreen,
                                        fontSize = 12.5.sp
                                    )
                                )
                                Text(
                                    text = "Keluar: -${formatRupiah(totalExpense, false)}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoExpenseRed,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSurplus) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (isSurplus) MonzoIncomeGreen else MonzoExpenseRed,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Bersih: " + (if (isSurplus) "+" else "") + formatRupiah(netCashFlow),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSurplus) MonzoIncomeGreen else MonzoExpenseRed,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }

                        // VISUAL DUAL-BAR CHART (CANVAS)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .padding(vertical = 6.dp)
                        ) {
                            CashFlowBarChart(
                                data = chartData,
                                selectedIndex = selectedBarIndex,
                                onBarSelected = { selectedBarIndex = it }
                            )
                        }

                        // DETAIL TOOLTIP JIKA ADA BAR YANG DIKETUK
                        if (selectedBarIndex in chartData.indices) {
                            val item = chartData[selectedBarIndex]
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF3F7F4),
                                border = BorderStroke(1.dp, Color(0xFFDCE6DF))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Detail: ${item.label}",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MonzoTextPrimary
                                            )
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "Masuk: +${formatRupiah(item.income, false)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MonzoIncomeGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            text = "Keluar: -${formatRupiah(item.expense, false)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MonzoExpenseRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Legend Warna & Petunjuk
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
                                text = "Ketuk bar untuk detail",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonzoTextTertiaryColor,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // ==========================================
            // SEKSI 1: BREAKDOWN PENGELUARAN PER KATEGORI
            // ==========================================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0x0A000000),
                            spotColor = Color(0x14000000)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = MonzoSurface,
                    border = BorderStroke(1.dp, MonzoBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Pengeluaran per Kategori",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoTextPrimary,
                                        fontSize = 16.5.sp
                                    )
                                )
                                Text(
                                    text = "Distribusi pengeluaran ${if (selectedPeriod == ChartPeriod.WEEKLY) "7 hari" else "4 pekan"} terakhir",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            if (categorySpendings.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F2)
                                ) {
                                    Text(
                                        text = "${categorySpendings.size} Kategori",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MonzoTextSecondary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        if (categorySpendings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada catatan pengeluaran pada periode ini.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextTertiaryColor,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                categorySpendings.forEach { item ->
                                    val catColor = Color(item.categoryColor)
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .background(catColor, CircleShape)
                                                )
                                                Text(
                                                    text = item.categoryName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MonzoTextPrimary,
                                                        fontSize = 13.5.sp
                                                    )
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = catColor.copy(alpha = 0.12f)
                                                ) {
                                                    Text(
                                                        text = "${(item.percentage * 100).toInt()}%",
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = catColor,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.5.sp
                                                        )
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "-${formatRupiah(item.totalAmount, false)}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = MonzoExpenseRed,
                                                    fontSize = 13.5.sp
                                                )
                                            )
                                        }

                                        // Progress Bar Horizontal
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFFEFF2EF))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(item.percentage.coerceIn(0.01f, 1f))
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(catColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SEKSI 2: RINCIAN ARUS KAS (LIST VIEW)
            // ==========================================
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0x0A000000),
                            spotColor = Color(0x14000000)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    color = MonzoSurface,
                    border = BorderStroke(1.dp, MonzoBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Rincian Arus Kas",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MonzoTextPrimary,
                                        fontSize = 16.5.sp
                                    )
                                )
                                Text(
                                    text = "Rincian masuk vs keluar per ${if (selectedPeriod == ChartPeriod.WEEKLY) "hari" else "pekan"}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextSecondary,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            Text(
                                text = "Ketuk untuk sorot",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MonzoTextTertiaryColor,
                                    fontSize = 10.5.sp
                                )
                            )
                        }

                        if (chartData.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada aktivitas transaksi.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MonzoTextTertiaryColor,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chartData.reversed().forEachIndexed { reverseIndex, item ->
                                    val originalIndex = chartData.size - 1 - reverseIndex
                                    val isSelected = selectedBarIndex == originalIndex
                                    val diff = item.income - item.expense
                                    val hasActivity = item.income > 0 || item.expense > 0

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                selectedBarIndex = if (isSelected) -1 else originalIndex
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) Color(0xFFE9F5F2) else Color(0xFFFAFCFA),
                                        border = BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) MonzoTeal else Color(0xFFEDF2EE)
                                        )
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
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                // Mini Status Icon
                                                Surface(
                                                    modifier = Modifier.size(34.dp),
                                                    shape = CircleShape,
                                                    color = when {
                                                        !hasActivity -> Color(0xFFEFF2EF)
                                                        diff > 0 -> MonzoIncomeGreen.copy(alpha = 0.14f)
                                                        diff < 0 -> MonzoExpenseRed.copy(alpha = 0.14f)
                                                        else -> Color(0xFFEFF2EF)
                                                    }
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = when {
                                                                diff >= 0 -> Icons.AutoMirrored.Filled.TrendingUp
                                                                else -> Icons.AutoMirrored.Filled.TrendingDown
                                                            },
                                                            contentDescription = null,
                                                            tint = when {
                                                                !hasActivity -> MonzoTextTertiaryColor
                                                                diff > 0 -> MonzoIncomeGreen
                                                                diff < 0 -> MonzoExpenseRed
                                                                else -> MonzoTextSecondary
                                                            },
                                                            modifier = Modifier.size(17.dp)
                                                        )
                                                    }
                                                }

                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = item.label,
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MonzoTextPrimary,
                                                            fontSize = 13.5.sp
                                                        )
                                                    )
                                                    Text(
                                                        text = when {
                                                            !hasActivity -> "Tidak ada mutasi"
                                                            diff > 0 -> "Surplus +${formatRupiah(diff, false)}"
                                                            diff < 0 -> "Defisit -${formatRupiah(-diff, false)}"
                                                            else -> "Seimbang"
                                                        },
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = when {
                                                                !hasActivity -> MonzoTextTertiaryColor
                                                                diff > 0 -> MonzoIncomeGreen
                                                                diff < 0 -> MonzoExpenseRed
                                                                else -> MonzoTextSecondary
                                                            },
                                                            fontWeight = FontWeight.Medium,
                                                            fontSize = 11.sp
                                                        )
                                                    )
                                                }
                                            }

                                            // Nominal Masuk & Keluar
                                            Column(
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                if (item.income > 0) {
                                                    Text(
                                                        text = "+${formatRupiah(item.income, false)}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = MonzoIncomeGreen,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                }
                                                if (item.expense > 0) {
                                                    Text(
                                                        text = "-${formatRupiah(item.expense, false)}",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = MonzoExpenseRed,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                }
                                                if (!hasActivity) {
                                                    Text(
                                                        text = "Rp 0",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            color = MonzoTextTertiaryColor,
                                                            fontSize = 12.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Spacer agar nyaman di-scroll di atas bottom nav
            item {
                Spacer(modifier = Modifier.height(130.dp))
            }
        }
    }
}

/**
 * Dual-Bar Chart Arus Kas Menggunakan Canvas
 * Menampilkan bar Pemasukan (Hijau) & bar Pengeluaran (Merah) berdampingan per titik data
 */
@Composable
private fun CashFlowBarChart(
    data: List<CashFlowBarData>,
    selectedIndex: Int,
    onBarSelected: (Int) -> Unit
) {
    if (data.isEmpty()) return

    val maxAmount = remember(data) {
        val highest = data.maxOfOrNull { max(it.income, it.expense) } ?: 0.0
        if (highest <= 0.0) 100000.0 else highest
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val totalSlots = data.size
            if (totalSlots == 0) return@Canvas

            val chartWidth = size.width
            val chartHeight = size.height - 24.dp.toPx() // Sediakan 24dp untuk label sumbu X
            val slotWidth = chartWidth / totalSlots
            val barWidth = (slotWidth * 0.32f).coerceAtMost(16.dp.toPx())
            val barSpacing = 3.dp.toPx()

            // 1. Gambar 3 Garis Grid Horizontal Tipis
            val gridColor = Color(0xFFECEFEA)
            val strokeW = 1.dp.toPx()
            for (g in 1..3) {
                val y = chartHeight * (g / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = strokeW,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // 2. Gambar Batang (Bars) Pemasukan & Pengeluaran
            data.forEachIndexed { index, item ->
                val centerX = slotWidth * index + slotWidth / 2f
                val incomeX = centerX - barWidth - (barSpacing / 2f)
                val expenseX = centerX + (barSpacing / 2f)

                // Tinggi Bar Pemasukan (Min 4.dp agar titik pijakan terlihat)
                val incomeHeightPx = if (item.income > 0) {
                    ((item.income / maxAmount) * (chartHeight * 0.9f)).toFloat().coerceAtLeast(4.dp.toPx())
                } else 2.dp.toPx()

                // Tinggi Bar Pengeluaran (Min 4.dp)
                val expenseHeightPx = if (item.expense > 0) {
                    ((item.expense / maxAmount) * (chartHeight * 0.9f)).toFloat().coerceAtLeast(4.dp.toPx())
                } else 2.dp.toPx()

                val incomeTopY = chartHeight - incomeHeightPx
                val expenseTopY = chartHeight - expenseHeightPx
                val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                val isCurrentSelected = index == selectedIndex

                // Highlight background jika bar dipilih
                if (isCurrentSelected) {
                    drawRoundRect(
                        color = Color(0xFFE9F2EC),
                        topLeft = Offset(slotWidth * index + 2.dp.toPx(), 0f),
                        size = Size(slotWidth - 4.dp.toPx(), chartHeight + 20.dp.toPx()),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }

                // Gambar Bar Pemasukan (Hijau)
                drawRoundRect(
                    color = if (item.income > 0) MonzoIncomeGreen else Color(0xFFD4E2D7),
                    topLeft = Offset(incomeX, incomeTopY),
                    size = Size(barWidth, incomeHeightPx),
                    cornerRadius = cornerRadius
                )

                // Gambar Bar Pengeluaran (Merah Coral)
                drawRoundRect(
                    color = if (item.expense > 0) MonzoExpenseRed else Color(0xFFE8D5D2),
                    topLeft = Offset(expenseX, expenseTopY),
                    size = Size(barWidth, expenseHeightPx),
                    cornerRadius = cornerRadius
                )

                // Gambar Label Sumbu X (Hari / Pekan)
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = if (isCurrentSelected) 0xFF147B96.toInt() else 0xFF6E8092.toInt()
                        textSize = 10.dp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isCurrentSelected
                        isAntiAlias = true
                    }
                    drawText(
                        item.label,
                        centerX,
                        chartHeight + 16.dp.toPx(),
                        paint
                    )
                }
            }
        }

        // Invisible touch interceptors for each column slot
        Row(modifier = Modifier.fillMaxSize()) {
            data.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            onBarSelected(if (selectedIndex == index) -1 else index)
                        }
                )
            }
        }
    }
}

private val MonzoTextTertiaryColor = Color(0xFFA4B0BD)
