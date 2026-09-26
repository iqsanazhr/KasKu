package com.example.kasku.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kasku.data.local.dao.AccountDao
import com.example.kasku.data.local.dao.AiSummaryDao
import com.example.kasku.data.local.dao.BudgetDao
import com.example.kasku.data.local.dao.CategoryDao
import com.example.kasku.data.local.dao.TransactionDao
import com.example.kasku.data.local.entity.AccountEntity
import com.example.kasku.data.local.entity.AiSummaryEntity
import com.example.kasku.data.local.entity.BudgetEntity
import com.example.kasku.data.local.entity.CategoryEntity
import com.example.kasku.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        AiSummaryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KasKuDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun aiSummaryDao(): AiSummaryDao

    companion object {
        @Volatile
        private var INSTANCE: KasKuDatabase? = null

        fun getDatabase(context: Context): KasKuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KasKuDatabase::class.java,
                    "kasku_database.db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }

            private suspend fun seedInitialData(database: KasKuDatabase) {
                // Seed default categories
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
                database.categoryDao().insertAll(defaultCategories)
            }
        }
    }
}
