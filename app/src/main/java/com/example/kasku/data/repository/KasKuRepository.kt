package com.example.kasku.data.repository

import androidx.room.withTransaction
import com.example.kasku.data.local.KasKuDatabase
import com.example.kasku.data.local.entity.AccountEntity
import com.example.kasku.data.local.entity.CategoryEntity
import com.example.kasku.data.local.entity.TransactionEntity
import com.example.kasku.domain.model.Account
import com.example.kasku.domain.model.Category
import com.example.kasku.domain.model.ReceiptItem
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KasKuRepository(private val db: KasKuDatabase) {
    private val transactionDao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val accountDao = db.accountDao()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun ensureDefaultData() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaultCategories = listOf(
                CategoryEntity(name = "Makanan & Minuman", iconName = "restaurant", colorHex = 0xFFF59E0B, type = "EXPENSE"),
                CategoryEntity(name = "Belanja & Supermarket", iconName = "shopping_cart", colorHex = 0xFF3B82F6, type = "EXPENSE"),
                CategoryEntity(name = "Transportasi", iconName = "directions_car", colorHex = 0xFF10B981, type = "EXPENSE"),
                CategoryEntity(name = "Tagihan & Utilitas", iconName = "receipt_long", colorHex = 0xFFEF4444, type = "EXPENSE"),
                CategoryEntity(name = "Hiburan & Rekreasi", iconName = "movie", colorHex = 0xFF8B5CF6, type = "EXPENSE"),
                CategoryEntity(name = "Kesehatan", iconName = "medical_services", colorHex = 0xFFEC4899, type = "EXPENSE"),
                CategoryEntity(name = "Pendidikan & Buku", iconName = "school", colorHex = 0xFF6366F1, type = "EXPENSE"),
                CategoryEntity(name = "Gaji & Pendapatan", iconName = "payments", colorHex = 0xFF059669, type = "INCOME"),
                CategoryEntity(name = "Investasi & Dividen", iconName = "trending_up", colorHex = 0xFF0284C7, type = "INCOME"),
                CategoryEntity(name = "Freelance & Usaha", iconName = "work", colorHex = 0xFF7C3AED, type = "INCOME")
            )
            categoryDao.insertAll(defaultCategories)
        }
    }

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getTransactionsBetween(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsBetween(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getTransactionsBetweenSync(startDate: Long, endDate: Long): List<Transaction> =
        transactionDao.getTransactionsBetweenSync(startDate, endDate).map { it.toDomain() }

    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>> =
        transactionDao.getRecentTransactions(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getAllAccounts(): Flow<List<Account>> =
        accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun addTransaction(transaction: Transaction): Long = db.withTransaction {
        val rawItemsJson = if (transaction.rawReceiptItems.isNotEmpty()) {
            json.encodeToString(transaction.rawReceiptItems.map { ReceiptItemDto(it.name, it.quantity, it.price) })
        } else null

        val entity = TransactionEntity(
            title = transaction.title,
            amount = transaction.amount,
            type = transaction.type.name,
            categoryId = transaction.categoryId,
            categoryName = transaction.categoryName,
            categoryIcon = transaction.categoryIcon,
            categoryColor = transaction.categoryColor,
            accountId = transaction.accountId,
            accountName = transaction.accountName,
            date = transaction.date,
            note = transaction.note,
            receiptImagePath = transaction.receiptImagePath,
            rawReceiptItemsJson = rawItemsJson
        )
        val id = transactionDao.insertTransaction(entity)

        // Adjust account balance
        val delta = when (transaction.type) {
            TransactionType.INCOME -> transaction.amount
            TransactionType.EXPENSE -> -transaction.amount
            TransactionType.TRANSFER -> -transaction.amount
        }
        accountDao.updateBalance(transaction.accountId, delta)

        id
    }

    suspend fun deleteTransaction(transaction: Transaction) = db.withTransaction {
        transactionDao.deleteById(transaction.id)

        // Revert account balance
        val delta = when (transaction.type) {
            TransactionType.INCOME -> -transaction.amount
            TransactionType.EXPENSE -> transaction.amount
            TransactionType.TRANSFER -> transaction.amount
        }
        accountDao.updateBalance(transaction.accountId, delta)
    }

    suspend fun addCategory(category: Category): Long {
        return categoryDao.insertCategory(
            CategoryEntity(
                name = category.name,
                iconName = category.iconName,
                colorHex = category.colorHex,
                type = category.type.name
            )
        )
    }

    suspend fun addAccount(account: Account): Long {
        return accountDao.insertAccount(
            AccountEntity(
                name = account.name,
                type = account.type,
                balance = account.balance,
                iconName = account.iconName
            )
        )
    }

    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(
            AccountEntity(
                id = account.id,
                name = account.name,
                type = account.type,
                balance = account.balance,
                iconName = account.iconName
            )
        )
    }

    suspend fun deleteAccount(account: Account) {
        accountDao.deleteAccountById(account.id)
    }

    private fun TransactionEntity.toDomain(): Transaction {
        val items = if (!rawReceiptItemsJson.isNullOrBlank()) {
            try {
                json.decodeFromString<List<ReceiptItemDto>>(rawReceiptItemsJson).map {
                    ReceiptItem(it.name, it.quantity, it.price)
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()

        return Transaction(
            id = id,
            title = title,
            amount = amount,
            type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE },
            categoryId = categoryId,
            categoryName = categoryName,
            categoryIcon = categoryIcon,
            categoryColor = categoryColor,
            accountId = accountId,
            accountName = accountName,
            date = date,
            note = note,
            receiptImagePath = receiptImagePath,
            rawReceiptItems = items
        )
    }

    private fun CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            iconName = iconName,
            colorHex = colorHex,
            type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE }
        )
    }

    private fun AccountEntity.toDomain(): Account {
        return Account(
            id = id,
            name = name,
            type = type,
            balance = balance,
            iconName = iconName
        )
    }
}

@kotlinx.serialization.Serializable
private data class ReceiptItemDto(
    val name: String,
    val quantity: Int,
    val price: Double
)
