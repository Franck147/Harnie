package com.harnie.app.core.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.toDisplayString(decimals: Int = 2): String =
    setScale(decimals, RoundingMode.HALF_UP).toPlainString()

fun Double.toBigDecimalSafe(): BigDecimal =
    BigDecimal.valueOf(this)

fun String.toBigDecimalOrZero(): BigDecimal =
    toBigDecimalOrNull() ?: BigDecimal.ZERO

/**
 * Convierte un timestamp ISO-8601 de la base de datos (UTC) a la fecha/hora
 * local del dispositivo. Devuelve null si no se puede parsear.
 */
fun String.toLocalDateTimeOrNull(): LocalDateTime? {
    return try {
        Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault())
    } catch (_: Exception) {
        try {
            // Timestamp sin zona horaria: se asume UTC
            Instant.parse(replace(" ", "T") + "Z")
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } catch (_: Exception) {
            null
        }
    }
}

/** Fecha local (solo dia) a partir de un timestamp UTC. */
fun String.toLocalDateOnly(): LocalDate? = toLocalDateTimeOrNull()?.date

/** Fecha local en formato ISO yyyy-MM-dd (fallback al string crudo). */
fun String.toLocalDateDisplay(): String {
    val dt = toLocalDateTimeOrNull() ?: return take(10)
    return "%04d-%02d-%02d".format(dt.year, dt.monthNumber, dt.dayOfMonth)
}

/** Hora local en formato HH:mm:ss (fallback al string crudo). */
fun String.toLocalTimeDisplay(): String {
    val dt = toLocalDateTimeOrNull() ?: return substringAfter("T").take(8)
    return "%02d:%02d:%02d".format(dt.hour, dt.minute, dt.second)
}