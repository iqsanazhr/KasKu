package com.example.kasku.ui.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kasku.data.preferences.AiPreferences
import com.example.kasku.data.remote.AiService
import com.example.kasku.data.repository.KasKuRepository
import com.example.kasku.domain.model.Account
import com.example.kasku.domain.model.Category
import com.example.kasku.domain.model.ReceiptItem
import com.example.kasku.domain.model.ReceiptScanResult
import com.example.kasku.domain.model.Transaction
import com.example.kasku.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.kasku.ui.common.UiState

class ReceiptScannerViewModel(
    private val repository: KasKuRepository,
    private val aiService: AiService,
    private val aiPreferences: AiPreferences
) : ViewModel() {

    // Standardized M3/MVVM UiState (Loading, Success, Error, Idle)
    private val _scanUiState = MutableStateFlow<UiState<ReceiptScanResult>>(UiState.Idle)
    val scanUiState: StateFlow<UiState<ReceiptScanResult>> = _scanUiState.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResult = MutableStateFlow<ReceiptScanResult?>(null)
    val scanResult: StateFlow<ReceiptScanResult?> = _scanResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveSuccessMessage = MutableStateFlow<String?>(null)
    val saveSuccessMessage: StateFlow<String?> = _saveSuccessMessage.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<Account>> = repository.getAllAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun scanReceiptImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _scanUiState.value = UiState.Loading
            _isScanning.value = true
            _errorMessage.value = null
            _saveSuccessMessage.value = null

            try {
                val provider = aiPreferences.aiProviderFlow.first()
                val geminiKey = aiPreferences.geminiApiKeyFlow.first()
                val geminiModel = aiPreferences.geminiModelFlow.first()
                val lmUrl = aiPreferences.lmStudioUrlFlow.first()
                val lmModel = aiPreferences.lmStudioModelFlow.first()

                val result = aiService.scanReceipt(
                    imageBytes = imageBytes,
                    mimeType = "image/jpeg",
                    provider = provider,
                    geminiApiKey = geminiKey,
                    geminiModel = geminiModel,
                    lmStudioUrl = lmUrl,
                    lmStudioModel = lmModel
                )

                result.onSuccess { extracted ->
                    _scanUiState.value = UiState.Success(extracted)
                    _scanResult.value = extracted
                }.onFailure { err ->
                    val errorMsg = "Gagal memindai: ${err.localizedMessage}"
                    _scanUiState.value = UiState.Error(errorMsg)
                    _errorMessage.value = errorMsg
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.localizedMessage}"
                _scanUiState.value = UiState.Error(errorMsg)
                _errorMessage.value = errorMsg
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun saveScannedTransaction(
        storeName: String,
        amount: Double,
        categoryId: Long,
        accountId: Long,
        items: List<ReceiptItem>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val cat = categories.value.firstOrNull { it.id == categoryId } ?: categories.value.firstOrNull()
            val acc = accounts.value.firstOrNull { it.id == accountId } ?: accounts.value.firstOrNull()

            if (cat != null && acc != null) {
                repository.addTransaction(
                    Transaction(
                        title = storeName.ifBlank { "Belanja Struk" },
                        amount = amount,
                        type = TransactionType.EXPENSE,
                        categoryId = cat.id,
                        categoryName = cat.name,
                        categoryIcon = cat.iconName,
                        categoryColor = cat.colorHex,
                        accountId = acc.id,
                        accountName = acc.name,
                        date = System.currentTimeMillis(),
                        rawReceiptItems = items
                    )
                )
                _saveSuccessMessage.value = "Transaksi berhasil disimpan ke ${acc.name}!"
                onSuccess()
            } else {
                _errorMessage.value = "Pilih kategori dan akun dompet terlebih dahulu."
            }
        }
    }

    fun clearScan() {
        _scanResult.value = null
        _errorMessage.value = null
        _saveSuccessMessage.value = null
    }

    fun clearMessages() {
        _errorMessage.value = null
        _saveSuccessMessage.value = null
    }

    class Factory(
        private val repository: KasKuRepository,
        private val aiService: AiService,
        private val aiPreferences: AiPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReceiptScannerViewModel(repository, aiService, aiPreferences) as T
        }
    }
}
