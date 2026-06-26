package com.harnie.app.core.model

enum class Currency(
    val code: String,
    val symbol: String,
    val displayName: String,
    val flag: String,
    val decimals: Int = 2
) {
    PEN(code = "PEN", symbol = "S/", displayName = "Sol Peruano", flag = "🇵🇪"),
    USD(code = "USD", symbol = "$", displayName = "Dólar", flag = "🇺🇸"),
    RUB(code = "RUB", symbol = "₽", displayName = "Rublo Ruso", flag = "🇷🇺");

    companion object {
        fun fromCode(code: String): Currency =
            entries.first { it.code.equals(code, ignoreCase = true) }
    }
}