package com.example.kasku.ui.common

/**
 * Standard Unidirectional Data Flow (UDF) UI State
 * Memenuhi kriteria standar materi ke-6: Arsitektur Aplikasi (MVVM + UiState).
 */
sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
