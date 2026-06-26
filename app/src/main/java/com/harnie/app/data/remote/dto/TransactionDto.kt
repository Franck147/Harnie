package com.harnie.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("buyer_id") val buyerId: String,
    @SerialName("seller_id") val sellerId: String,
    val amount: Double,
    @SerialName("exchange_rate") val exchangeRate: Double,
    @SerialName("source_currency") val sourceCurrency: String,
    @SerialName("target_currency") val targetCurrency: String,
    val status: String = "PENDING",
    @SerialName("created_at") val createdAt: String
)