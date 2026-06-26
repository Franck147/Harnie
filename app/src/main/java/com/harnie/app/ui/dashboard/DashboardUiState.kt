package com.harnie.app.ui.dashboard

import com.harnie.app.core.model.Money

data class BalanceItem(
    val available: Money,
    val frozen: Money
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Loaded(
        val displayName: String,
        val balances: List<BalanceItem>,
        val recentOrdersCount: Int
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}