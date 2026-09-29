package com.example.kasku.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kasku.data.preferences.UserPreferences
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Account
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val userName: String = "",
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val allAccounts: List<Account> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val allTransactions: List<Transaction> = emptyList(),
    val totalTransactionsCount: Int = 0,
    val expenseRatio: Float = 0f
)

class DashboardViewModel(
    private val repository: KasKuRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _isBalanceVisible = MutableStateFlow(true)
    val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        userPreferences.userNameFlow,
        repository.getAllAccounts(),
        repository.getAllTransactions()
    ) { name, accounts, transactions ->
        val balance = accounts.sumOf { it.balance }
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val ratio = if (income > 0) (expense / income).toFloat().coerceIn(0f, 1f) else 0f

        DashboardUiState(
            userName = name,
            totalBalance = balance,
            totalIncome = income,
            totalExpense = expense,
            allAccounts = accounts,
            recentTransactions = transactions.take(5),
            allTransactions = transactions,
            totalTransactionsCount = transactions.size,
            expenseRatio = ratio
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
    }

    class Factory(
        private val repository: KasKuRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository, userPreferences) as T
        }
    }
}
