package com.example.kasku.ui.screens.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import com.example.kasku.ui.components.formatRupiah
import com.example.kasku.ui.theme.CeramicBackground
import com.example.kasku.ui.theme.MonzoBorder
import com.example.kasku.ui.theme.MonzoSurface
import com.example.kasku.ui.theme.MonzoCoral
import com.example.kasku.ui.theme.MonzoTeal
import com.example.kasku.ui.theme.MonzoTextPrimary
import com.example.kasku.ui.theme.MonzoTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen Detail Transaksi.
 * Memenuhi kriteria standar materi ke-7: Navigation Compose (Transfer Data Antar Layar via NavArgument).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Long,
    repository: KasKuRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by repository.getAllTransactions().collectAsState(initial = emptyList())
    val transaction = transactions.find { it.id == transactionId }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(CeramicBackground),
        containerColor = CeramicBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Transaksi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MonzoTextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MonzoTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CeramicBackground
                )
            )
        }
    ) { innerPadding ->
        if (transaction == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MonzoTeal)
            }
        } else {
            val isExpense = transaction.type == TransactionType.EXPENSE
            val amountColor = if (isExpense) MonzoCoral else MonzoTeal
            val amountPrefix = if (isExpense) "- " else "+ "
            val dateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy - HH:mm", Locale("id", "ID")) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Amount Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MonzoSurface),
                    border = BorderStroke(1.dp, MonzoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonzoTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "$amountPrefix${formatRupiah(transaction.amount)}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = amountColor,
                                fontSize = 32.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = CircleShape,
                            color = amountColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (isExpense) "Pengeluaran" else "Pemasukan",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = amountColor
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Detail Information List
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MonzoSurface),
                    border = BorderStroke(1.dp, MonzoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailRow(
                            icon = Icons.Default.Category,
                            label = "Kategori",
                            value = transaction.categoryName
                        )

                        DetailRow(
                            icon = Icons.Default.Wallet,
                            label = "Sumber Dompet",
                            value = transaction.accountName
                        )

                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Waktu Transaksi",
                            value = dateFormatter.format(Date(transaction.date))
                        )

                        if (transaction.note.isNotBlank()) {
                            DetailRow(
                                icon = Icons.Default.Notes,
                                label = "Catatan",
                                value = transaction.note
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Delete Action Button
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MonzoCoral
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Hapus Transaksi",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Confirm Delete Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(text = "Hapus Transaksi?", fontWeight = FontWeight.Bold) },
                    text = { Text("Transaksi '${transaction.title}' akan dihapus secara permanen dari riwayat.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                scope.launch {
                                    repository.deleteTransaction(transaction)
                                    com.example.kasku.ui.components.TopNotif.showInfo(
                                        title = "Transaksi Dihapus",
                                        message = "Catatan transaksi telah dihapus"
                                    )
                                    onNavigateBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MonzoCoral)
                        ) {
                            Text("Hapus", color = Color.White)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(onClick = { showDeleteDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MonzoTeal.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MonzoTeal,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.size(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MonzoTextSecondary,
                    fontSize = 11.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MonzoTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
