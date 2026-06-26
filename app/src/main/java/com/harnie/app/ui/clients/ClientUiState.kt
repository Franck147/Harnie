package com.harnie.app.ui.clients

data class ClientItem(
    val id: String,
    val name: String,
    val lastName: String,
    val country: String,
    val phone: String?,
    val email: String?,
    val documentType: String?,
    val documentNumber: String?,
    val buyCount: Int = 0,
    val sellCount: Int = 0
) {
    val fullName: String get() = "$name $lastName"
    val totalOrders: Int get() = buyCount + sellCount
}

sealed interface ClientListUiState {
    data object Loading : ClientListUiState
    data class Loaded(val clients: List<ClientItem>) : ClientListUiState
    data class Error(val message: String) : ClientListUiState
}

sealed interface SaveClientUiState {
    data object Idle : SaveClientUiState
    data object Saving : SaveClientUiState
    data object Success : SaveClientUiState
    data class Error(val message: String) : SaveClientUiState
}
