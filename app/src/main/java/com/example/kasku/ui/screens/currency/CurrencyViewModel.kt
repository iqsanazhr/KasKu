package com.example.kasku.ui.screens.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasku.data.remote.currency.CurrencyApiService
import com.example.kasku.data.remote.currency.CurrencyResponse
import com.example.kasku.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel untuk Mengelola Kurs Mata Uang Dunia via Retrofit REST API.
 * Mengimplementasikan:
 * - Materi 5: Networking & API (Retrofit 2 asinkron dengan Coroutines)
 * - Materi 6: Arsitektur MVVM dengan UiState (Idle, Loading, Success, Error)
 */
class CurrencyViewModel(
    private val apiService: CurrencyApiService = CurrencyApiService.create()
) : ViewModel() {

    private val _currencyState = MutableStateFlow<UiState<CurrencyResponse>>(UiState.Idle)
    val currencyState: StateFlow<UiState<CurrencyResponse>> = _currencyState.asStateFlow()

    init {
        fetchRates()
    }

    fun fetchRates(base: String = "USD") {
        viewModelScope.launch {
            _currencyState.value = UiState.Loading
            try {
                val response = apiService.getExchangeRates(base)
                com.example.kasku.ui.components.CurrencyConfig.updateRates(response.rates)
                _currencyState.value = UiState.Success(response)
            } catch (e: Exception) {
                _currencyState.value = UiState.Error(e.localizedMessage ?: "Gagal memuat kurs mata uang via Retrofit")
            }
        }
    }
}
