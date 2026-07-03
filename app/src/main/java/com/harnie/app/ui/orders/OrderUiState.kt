package com.harnie.app.ui.orders

import com.harnie.app.core.model.Country
import com.harnie.app.core.model.Currency
import com.harnie.app.core.model.OrderStatus
import com.harnie.app.core.model.OrderType
import java.math.BigDecimal

data class OrderItem(
    val id: String,
    val creatorName: String,
    val creatorRating: Double,
    val orderType: OrderType,
    val sourceCurrency: Currency,
    val targetCurrency: Currency,
    val exchangeRate: BigDecimal,
    val amount: BigDecimal,
    val minLimit: BigDecimal,
    val maxLimit: BigDecimal,
    val status: OrderStatus,
    val exchange: String? = null,
    val country: String? = null,
    val paymentMethod: String? = null,
    val fiatAmount: Double? = null,
    val pricePerUnit: Double? = null,
    val usdtAmount: Double? = null,
    val exchangeCommission: Double? = null,
    val documentType: String? = null,
    val documentNumber: String? = null,
    val clientPhone: String? = null,
    val clientEmail: String? = null,
    val clientId: String? = null,
    val clientName: String? = null,
    val clientLastName: String? = null,
    val note: String? = null,
    val shortId: String? = null,
    val createdAt: String? = null
)

sealed interface OrderListUiState {
    data object Loading : OrderListUiState
    data class Loaded(
        val orders: List<OrderItem>,
        val filterType: OrderType? = null,
        val filterCurrency: Currency? = null,
        val filterCountry: Country? = null,
        val filterDate: String? = null,
        val filterBelowMin: Boolean = false
    ) : OrderListUiState
    data class Error(val message: String) : OrderListUiState
}

sealed interface CreateOrderUiState {
    data object Idle : CreateOrderUiState
    data object Submitting : CreateOrderUiState
    data object Success : CreateOrderUiState
    data class Error(val message: String) : CreateOrderUiState
}
