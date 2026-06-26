package com.harnie.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val phone: String? = null,
    @SerialName("kyc_status") val kycStatus: String = "NONE",
    @SerialName("preferred_currency") val preferredCurrency: String = "USD",
    @SerialName("total_trades") val totalTrades: Int = 0,
    val rating: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true
)