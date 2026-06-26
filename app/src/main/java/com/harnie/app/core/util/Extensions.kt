package com.harnie.app.core.util

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.toDisplayString(decimals: Int = 2): String =
    setScale(decimals, RoundingMode.HALF_UP).toPlainString()

fun Double.toBigDecimalSafe(): BigDecimal =
    BigDecimal.valueOf(this)

fun String.toBigDecimalOrZero(): BigDecimal =
    toBigDecimalOrNull() ?: BigDecimal.ZERO