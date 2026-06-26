package com.harnie.app.core.model

enum class TransactionStatus(val label: String) {
    PENDING("Pendiente"),
    CONFIRMED("Confirmada"),
    REJECTED("Rechazada"),
    EXPIRED("Expirada")
}