package com.harnie.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harnie.app.domain.repository.BalanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val balanceRepository: BalanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
        observeBalances()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            try {
                val balances = balanceRepository.getBalances()
                _uiState.value = DashboardUiState.Loaded(
                    displayName = "Usuario",
                    balances = balances,
                    recentOrdersCount = 0
                )
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Error al cargar")
            }
        }
    }

    private fun observeBalances() {
        viewModelScope.launch {
            balanceRepository.observeBalances()
                .catch { /* Silently reconnect on realtime errors */ }
                .collect { balances ->
                    val current = _uiState.value
                    if (current is DashboardUiState.Loaded) {
                        _uiState.value = current.copy(balances = balances)
                    }
                }
        }
    }

    fun refresh() { loadDashboard() }
}