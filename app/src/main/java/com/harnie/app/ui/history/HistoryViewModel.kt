package com.harnie.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harnie.app.core.model.Currency
import com.harnie.app.core.model.TransactionStatus
import com.harnie.app.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            try {
                val transactions = orderRepository.getMyTransactions()
                _uiState.value = HistoryUiState.Loaded(transactions = transactions)
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Error al cargar historial")
            }
        }
    }

    fun filterByCurrency(currency: Currency?) {
        val current = _uiState.value
        if (current is HistoryUiState.Loaded) {
            _uiState.value = current.copy(filterCurrency = currency)
        }
    }

    fun filterByStatus(status: TransactionStatus?) {
        val current = _uiState.value
        if (current is HistoryUiState.Loaded) {
            _uiState.value = current.copy(filterStatus = status)
        }
    }

    fun refresh() { loadHistory() }
}