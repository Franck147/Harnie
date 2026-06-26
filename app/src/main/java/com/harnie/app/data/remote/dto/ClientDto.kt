package com.harnie.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val name: String,
    @SerialName("last_name") val lastName: String,
    val country: String,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("document_type") val documentType: String? = null,
    @SerialName("document_number") val documentNumber: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
