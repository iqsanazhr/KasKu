package com.example.kasku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kasku.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // EXPENSE, INCOME, TRANSFER
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: Long,
    val accountId: Long,
    val accountName: String,
    val date: Long,
    val note: String = "",
    val receiptImagePath: String? = null,
    val rawReceiptItemsJson: String? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val type: String = "EXPENSE"
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // CASH, BANK, E_WALLET
    val balance: Double = 0.0,
    val iconName: String
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val categoryName: String,
    val monthlyLimit: Double,
    val monthYear: String // e.g. "2026-09"
)

@Entity(tableName = "ai_summaries")
data class AiSummaryEntity(
    @PrimaryKey val monthYear: String, // e.g. "2026-09"
    val overallHealthScore: Int,
    val executiveSummary: String,
    val burnRateWarning: String,
    val daysUntilBudgetRunsOut: Int?,
    val topSpendingCategoriesJson: String,
    val actionableTipsJson: String,
    val generatedAt: Long = System.currentTimeMillis()
)
