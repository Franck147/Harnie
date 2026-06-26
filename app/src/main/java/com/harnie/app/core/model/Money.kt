package com.harnie.app.core.model

import java.math.BigDecimal
import java.math.RoundingMode

data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    fun formatted(): String {
        val scaled = amount.setScale(currency.decimals, RoundingMode.HALF_UP)
        return "${currency.symbol} $scaled"
    }

    fun formattedWithCode(): String {
        val scaled = amount.setScale(currency.decimals, RoundingMode.HALF_UP)
        return "$scaled ${currency.code}"
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add different currencies" }
        return copy(amount = amount + other.amount)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract different currencies" }
        return copy(amount = amount - other.amount)
    }

    companion object {
        fun zero(currency: Currency) = Money(BigDecimal.ZERO, currency)

        fun of(value: Double, currency: Currency) =
            Money(BigDecimal.valueOf(value), currency)

        fun of(value: String, currency: Currency) =
            Money(BigDecimal(value), currency)
    }
}