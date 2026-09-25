package com.example.kasku.domain.model

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class AiProvider {
    GEMINI,
    LM_STUDIO
}

data class Transaction(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: Long,
    val accountId: Long,
    val accountName: String,
    val date: Long, // timestamp in ms
    val note: String = "",
    val receiptImagePath: String? = null,
    val rawReceiptItems: List<ReceiptItem> = emptyList()
)

data class ReceiptItem(
    val name: String,
    val quantity: Int = 1,
    val price: Double
)

data class Category(
    val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val type: TransactionType = TransactionType.EXPENSE
)

data class Account(
    val id: Long = 0,
    val name: String,
    val type: String, // "CASH", "BANK", "E_WALLET"
    val balance: Double = 0.0,
    val iconName: String
)

data class Budget(
    val id: Long = 0,
    val categoryId: Long,
    val categoryName: String,
    val monthlyLimit: Double,
    val monthYear: String // e.g. "2026-09"
)

data class ReceiptScanResult(
    val storeName: String,
    val dateString: String?,
    val totalAmount: Double,
    val suggestedCategory: String,
    val items: List<ReceiptItem> = emptyList(),
    val notes: String = ""
)

data class AiFinancialSummary(
    val monthYear: String,
    val overallHealthScore: Int, // 0 - 100
    val executiveSummary: String,
    val burnRateWarning: String,
    val daysUntilBudgetRunsOut: Int?,
    val topSpendingCategories: List<String>,
    val actionableTips: List<String>,
    val generatedAt: Long = System.currentTimeMillis()
)
