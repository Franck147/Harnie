package com.harnie.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BalanceDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val currency: String,
    val available: Double,
    val frozen: Double
)