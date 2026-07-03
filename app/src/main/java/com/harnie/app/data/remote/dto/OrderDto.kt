package com.harnie.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    @SerialName("creator_id") val creatorId: String,
    @SerialName("order_type") val orderType: String,
    @SerialName("source_currency") val sourceCurrency: String,
    @SerialName("target_currency") val targetCurrency: String,
    @SerialName("exchange_rate") val exchangeRate: Double,
    val amount: Double,
    @SerialName("min_limit") val minLimit: Double,
    @SerialName("max_limit") val maxLimit: Double,
    val status: String = "OPEN",
    val terms: String? = null,
    val exchange: String? = null,
    val country: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("fiat_amount") val fiatAmount: Double? = null,
    @SerialName("price_per_unit") val pricePerUnit: Double? = null,
    @SerialName("usdt_amount") val usdtAmount: Double? = null,
    @SerialName("exchange_commission") val exchangeCommission: Double? = null,
    @SerialName("document_type") val documentType: String? = null,
    @SerialName("document_number") val documentNumber: String? = null,
    @SerialName("client_phone") val clientPhone: String? = null,
    @SerialName("client_email") val clientEmail: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("client_name") val clientName: String? = null,
    @SerialName("client_last_name") val clientLastName: String? = null,
    val note: String? = null,
    @SerialName("short_id") val shortId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CreateOrderDto(
    @SerialName("creator_id") val creatorId: String,
    @SerialName("order_type") val orderType: String,
    @SerialName("source_currency") val sourceCurrency: String,
    @SerialName("target_currency") val targetCurrency: String,
    @SerialName("exchange_rate") val exchangeRate: Double,
    val amount: Double,
    @SerialName("min_limit") val minLimit: Double,
    @SerialName("max_limit") val maxLimit: Double,
    val exchange: String,
    val country: String,
    @SerialName("payment_method") val paymentMethod: String,
    @SerialName("fiat_amount") val fiatAmount: Double,
    @SerialName("price_per_unit") val pricePerUnit: Double,
    @SerialName("usdt_amount") val usdtAmount: Double,
    @SerialName("exchange_commission") val exchangeCommission: Double? = null,
    @SerialName("document_type") val documentType: String? = null,
    @SerialName("document_number") val documentNumber: String? = null,
    @SerialName("client_phone") val clientPhone: String? = null,
    @SerialName("client_email") val clientEmail: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("client_name") val clientName: String? = null,
    @SerialName("client_last_name") val clientLastName: String? = null,
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
