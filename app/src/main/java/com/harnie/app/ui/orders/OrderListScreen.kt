package com.harnie.app.ui.orders

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harnie.app.R
import com.harnie.app.core.model.Country
import com.harnie.app.core.model.Currency
import com.harnie.app.core.model.OrderType
import com.harnie.app.core.util.toLocalDateDisplay
import com.harnie.app.core.util.toLocalDateOnly
import com.harnie.app.core.util.toLocalTimeDisplay
import com.harnie.app.ui.components.HarnieCard
import com.harnie.app.ui.components.MonoText
import com.harnie.app.ui.components.ShimmerBox
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    onNavigateToCreateOrder: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: OrderViewModel = koinViewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()

    // Recargar la lista al volver a esta pantalla (p.ej. tras crear una orden)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshMyOrders()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var csvContent by remember { mutableStateOf("") }

    val saveCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null && csvContent.isNotEmpty()) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                // BOM UTF-8 para que Excel respete tildes y caracteres especiales
                stream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                stream.write(csvContent.toByteArray(Charsets.UTF_8))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val date = instant.toLocalDateTime(TimeZone.UTC).date
                            viewModel.filterByDate(date.toString())
                        }
                    }
                ) { Text("Seleccionar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Ordenes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateOrder,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva orden")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when (val state = listState) {
                is OrderListUiState.Loading -> {
                    Spacer(Modifier.height(8.dp))
                    repeat(5) {
                        ShimmerBox(height = 100.dp, cornerRadius = 24.dp)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                is OrderListUiState.Loaded -> {
                    // Filtro por tipo (Todos / Compra / Venta)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.filterType == null,
                            onClick = { viewModel.filterByType(null) },
                            label = { Text("Todos") }
                        )
                        OrderType.entries.forEach { type ->
                            FilterChip(
                                selected = state.filterType == type,
                                onClick = {
                                    viewModel.filterByType(
                                        if (state.filterType == type) null else type
                                    )
                                },
                                label = { Text(type.label) }
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Filtro por pais (Todos / Peru / Ecuador / Rusia)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.filterCountry == null,
                            onClick = { viewModel.filterByCountry(null) },
                            label = { Text("Todos") }
                        )
                        Country.entries.forEach { c ->
                            FilterChip(
                                selected = state.filterCountry == c,
                                onClick = {
                                    viewModel.filterByCountry(
                                        if (state.filterCountry == c) null else c
                                    )
                                },
                                label = { Text(c.flag) }
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // Filtro por fecha
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { showDatePicker = true },
                            label = {
                                Text(
                                    if (state.filterDate != null) {
                                        val parts = state.filterDate.split("-")
                                        "${parts[2]}/${parts[1]}/${parts[0]}"
                                    } else "Fecha"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        if (state.filterDate != null) {
                            IconButton(
                                onClick = { viewModel.filterByDate(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Limpiar fecha",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Filtro de montos pequenos (PEN < 1000, USD < 300)
                        FilterChip(
                            selected = state.filterBelowMin,
                            onClick = { viewModel.filterByBelowMin(!state.filterBelowMin) },
                            label = { Text("Montos pequeños") }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    val filtered = state.orders.filter { order ->
                        (state.filterType == null || order.orderType == state.filterType) &&
                        (state.filterCountry == null || order.country == state.filterCountry.name) &&
                        (state.filterDate == null || order.createdAt?.toLocalDateDisplay() == state.filterDate) &&
                        (!state.filterBelowMin || isBelowMinAmount(order))
                    }

                    // Boton exportar
                    if (filtered.isNotEmpty()) {
                        Button(
                            onClick = {
                                csvContent = buildCsvContent(filtered)
                                val fileName = buildString {
                                    append("ordenes")
                                    if (state.filterType != null) append("_${state.filterType.name.lowercase()}")
                                    if (state.filterCountry != null) append("_${state.filterCountry.name.lowercase()}")
                                    if (state.filterDate != null) append("_${state.filterDate}")
                                    append(".csv")
                                }
                                saveCsvLauncher.launch(fileName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Exportar CSV (${filtered.size} ordenes)")
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (filtered.isEmpty()) {
                        Spacer(Modifier.height(32.dp))
                        Text(
                            text = if (state.orders.isEmpty()) "No tienes ordenes registradas"
                                   else "No hay ordenes con estos filtros",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val grouped = groupOrdersByDate(filtered)

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            grouped.forEach { (dateLabel, orders) ->
                                item(key = dateLabel) {
                                    Text(
                                        text = dateLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(orders, key = { it.id }) { order ->
                                    OrderCard(
                                        order = order,
                                        onClick = { onNavigateToDetail(order.id) }
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }

                is OrderListUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Determina si una orden esta por debajo del monto minimo segun su moneda:
 * PEN < 1000, USD < 300. Otras monedas (p.ej. RUB) no aplican.
 */
private fun isBelowMinAmount(order: OrderItem): Boolean {
    val amount = order.fiatAmount ?: return false
    val threshold = when (order.sourceCurrency) {
        Currency.PEN -> 1000.0
        Currency.USD -> 300.0
        else -> return false
    }
    return amount < threshold
}

private fun groupOrdersByDate(orders: List<OrderItem>): List<Pair<String, List<OrderItem>>> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = kotlinx.datetime.LocalDate(today.year, today.monthNumber, today.dayOfMonth)
        .let {
            val epoch = it.toEpochDays() - 1
            kotlinx.datetime.LocalDate.fromEpochDays(epoch)
        }

    return orders
        .groupBy { order ->
            order.createdAt?.toLocalDateOnly()
        }
        .toSortedMap(compareByDescending { it })
        .map { (date, items) ->
            val label = when (date) {
                today -> "Hoy"
                yesterday -> "Ayer"
                null -> "Sin fecha"
                else -> "%02d/%02d/%04d".format(date.dayOfMonth, date.monthNumber, date.year)
            }
            label to items
        }
}

private fun buildCsvContent(orders: List<OrderItem>): String {
    val sep = ";"
    val header = listOf(
        "ID Orden Venta",
        "Monto USDT ENVIADO",
        "Precio PEN por USDT",
        "PEN Recibido",
        "METODO DE PAGO",
        "EXCHANGUE",
        "Nombre de cliente",
        "DNI O CARNET DE EXTRANJERIA",
        "ID Orden Compra",
        "PEN Enviado",
        "Precio PEN por USDT",
        "Monto USDT RECIBIDO",
        "METODO DE PAGO",
        "EXCHANGUE",
        "NOMBRE CLIENTE"
    ).joinToString(sep)

    fun esc(value: String?): String =
        value?.replace(";", " ")?.replace(",", " ")?.replace("\n", " ")?.trim() ?: ""

    fun money(value: Double?): String = value?.let { String.format("%.2f", it) } ?: ""
    fun price(value: Double?): String = value?.let { String.format("%.4f", it) } ?: ""

    val rows = orders.map { o ->
        val exchange = esc(o.exchange?.replace("_", " "))
        val cliente = esc(listOfNotNull(o.clientName, o.clientLastName).joinToString(" "))
        val metodo = esc(o.paymentMethod)

        if (o.orderType == OrderType.SELL) {
            // Venta: enviamos USDT y recibimos PEN -> columnas 1-8
            listOf(
                esc(o.id),                // ID Orden Venta
                money(o.usdtAmount),      // Monto USDT ENVIADO
                price(o.pricePerUnit),    // Precio PEN por USDT
                money(o.fiatAmount),      // PEN Recibido
                metodo,                   // METODO DE PAGO
                exchange,                 // EXCHANGUE
                cliente,                  // Nombre de cliente
                esc(o.documentNumber),    // DNI O CARNET DE EXTRANJERIA
                "", "", "", "", "", "", ""
            )
        } else {
            // Compra: enviamos PEN y recibimos USDT -> columnas 9-15
            listOf(
                "", "", "", "", "", "", "", "",
                esc(o.id),                // ID Orden Compra
                money(o.fiatAmount),      // PEN Enviado
                price(o.pricePerUnit),    // Precio PEN por USDT
                money(o.usdtAmount),      // Monto USDT RECIBIDO
                metodo,                   // METODO DE PAGO
                exchange,                 // EXCHANGUE
                cliente                   // NOMBRE CLIENTE
            )
        }.joinToString(sep)
    }
    // "sep=;" indica a Excel el separador a usar al abrir el archivo
    return (listOf("sep=$sep", header) + rows).joinToString("\n")
}

@Composable
private fun OrderCard(order: OrderItem, onClick: () -> Unit) {
    HarnieCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (order.orderType.label == "Compra") "Compra" else "Venta",
                style = MaterialTheme.typography.titleMedium,
                color = if (order.orderType.label == "Compra")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
            Text(
                text = order.exchange ?: "",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${order.sourceCurrency.flag} ${order.sourceCurrency.code}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.width(4.dp))
            Text("→", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_usdt),
                contentDescription = "USDT",
                tint = Color.Unspecified,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = "USDT",
                style = MaterialTheme.typography.bodyMedium
            )
            if (order.country != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = order.country,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (order.fiatAmount != null) {
                MonoText(text = "${order.sourceCurrency.symbol}${String.format("%.2f", order.fiatAmount)}")
            }
            if (order.usdtAmount != null) {
                MonoText(text = "USDT ${String.format("%.2f", order.usdtAmount)}")
            }
        }

        if (order.paymentMethod != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = order.paymentMethod,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (order.createdAt != null) {
            Spacer(Modifier.height(2.dp))
            val time = order.createdAt.toLocalTimeDisplay()
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
