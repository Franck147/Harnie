package com.harnie.app.ui.history

import com.harnie.app.core.model.Currency
import com.harnie.app.core.model.TransactionStatus
import java.math.BigDecimal

data class HistoryItem(
    val id: String,
    val counterpartyName: String,
    val sourceCurrency: Currency,
    val targetCurrency: Currency,
    val amount: BigDecimal,
    val exchangeRate: BigDecimal,
    val status: TransactionStatus,
    val createdAt: String
)

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Loaded(
        val transactions: List<HistoryItem>,
        val filterCurrency: Currency? = null,
        val filterStatus: TransactionStatus? = null
    ) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}