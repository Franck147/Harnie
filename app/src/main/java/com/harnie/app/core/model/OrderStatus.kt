package com.harnie.app.core.model

enum class OrderStatus(val label: String) {
    OPEN("Abierta"),
    IN_PROGRESS("Pendiente"),
    COMPLETED("Confirmada"),
    CANCELLED("Cancelada"),
    DISPUTED("En disputa")
}