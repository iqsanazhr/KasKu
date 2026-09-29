package com.example.kasku.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Account
import com.example.kasku.domain.model.Category
import com.example.kasku.domain.model.ReceiptScanResult
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionsViewModel(
    private val repository: KasKuRepository,
    private val aiService: AiService,
    private val aiPreferences: AiPreferences
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TransactionFilter.ALL)
    val selectedFilter: StateFlow<TransactionFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isAiParsing = MutableStateFlow(false)
    val isAiParsing: StateFlow<Boolean> = _isAiParsing.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        repository.getAllTransactions(),
        _selectedFilter,
        _searchQuery
    ) { allTransactions, filter, query ->
        allTransactions.filter { tx ->
            val matchesFilter = when (filter) {
                TransactionFilter.ALL -> true
                TransactionFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
                TransactionFilter.INCOME -> tx.type == TransactionType.INCOME
            }
            val matchesSearch = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.categoryName.contains(query, ignoreCase = true) ||
                    tx.accountName.contains(query, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: TransactionFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun parseNaturalLanguage(
        input: String,
        onSuccess: (ReceiptScanResult) -> Unit,
        onError: (String) -> Unit
    ) {
        if (input.isBlank()) return
        viewModelScope.launch {
            _isAiParsing.value = true
            try {
                val provider = aiPreferences.aiProviderFlow.first()
                val key = aiPreferences.geminiApiKeyFlow.first()
                val gModel = aiPreferences.geminiModelFlow.first()
                val lmUrl = aiPreferences.lmStudioUrlFlow.first()
                val lmModel = aiPreferences.lmStudioModelFlow.first()

                val res = aiService.parseNaturalLanguageEntry(
                    text = input,
                    provider = provider,
                    geminiApiKey = key,
                    geminiModel = gModel,
                    lmStudioUrl = lmUrl,
                    lmStudioModel = lmModel
                )
                res.onSuccess { onSuccess(it) }
                    .onFailure { onError(it.localizedMessage ?: "Gagal memproses AI") }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Terjadi kesalahan")
            } finally {
                _isAiParsing.value = false
            }
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        accountId: Long,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val cat = categories.value.firstOrNull { it.id == categoryId } ?: categories.value.firstOrNull()
            val acc = accounts.value.firstOrNull { it.id == accountId } ?: accounts.value.firstOrNull()

            if (cat != null && acc != null) {
                repository.addTransaction(
                    Transaction(
                        title = title.trim(),
                        amount = amount,
                        type = type,
                        categoryId = cat.id,
                        categoryName = cat.name,
                        categoryIcon = cat.iconName,
                        categoryColor = cat.colorHex,
                        accountId = acc.id,
                        accountName = acc.name,
                        date = System.currentTimeMillis()
                    )
                )
                onComplete()
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    class Factory(
        private val repository: KasKuRepository,
        private val aiService: AiService,
        private val aiPreferences: AiPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionsViewModel(repository, aiService, aiPreferences) as T
        }
    }
}
