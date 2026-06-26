package com.harnie.app.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harnie.app.core.model.Country
import com.harnie.app.ui.components.HarnieCard
import com.harnie.app.ui.components.MonoText
import com.harnie.app.ui.components.ShimmerBox
import com.harnie.app.ui.orders.OrderItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: String,
    onBack: () -> Unit,
    viewModel: ClientViewModel = koinViewModel()
) {
    val client by viewModel.selectedClient.collectAsStateWithLifecycle()
    val orders by viewModel.clientOrders.collectAsStateWithLifecycle()

    LaunchedEffect(clientId) {
        viewModel.loadClientOrders(clientId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(client?.fullName ?: "Cliente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info del cliente
            item {
                if (client == null) {
                    ShimmerBox(height = 100.dp, cornerRadius = 24.dp)
                } else {
                    val c = client!!
                    HarnieCard {
                        Text(
                            text = c.fullName,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        val countryLabel = try {
                            val co = Country.valueOf(c.country)
                            "${co.flag} ${co.displayName}"
                        } catch (_: Exception) { c.country }
                        DetailLine("Pais", countryLabel)
                        if (c.documentType != null) DetailLine("Documento", "${c.documentType}: ${c.documentNumber ?: "-"}")
                        if (c.phone != null) DetailLine("Celular", c.phone)
                        if (c.email != null) DetailLine("Correo", c.email)
                    }
                }
            }

            // Resumen de ordenes
            item {
                val buyCount = orders.count { it.orderType.name == "BUY" }
                val sellCount = orders.count { it.orderType.name == "SELL" }
                HarnieCard {
                    Text(
                        text = "Resumen de ordenes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text(
                                text = "$buyCount",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Compras", style = MaterialTheme.typography.labelMedium)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text(
                                text = "$sellCount",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text("Ventas", style = MaterialTheme.typography.labelMedium)
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            Text(
                                text = "${orders.size}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text("Total", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Historial de ordenes
            item {
                if (orders.isNotEmpty()) {
                    Text(
                        text = "Historial",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            items(orders, key = { it.id }) { order ->
                ClientOrderCard(order)
            }

            if (orders.isEmpty()) {
                item {
                    Text(
                        text = "Sin ordenes registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ClientOrderCard(order: OrderItem) {
    HarnieCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (order.orderType.label == "Compra") "Compra" else "Venta",
                style = MaterialTheme.typography.titleSmall,
                color = if (order.orderType.label == "Compra")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
            Text(
                text = order.exchange ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(Modifier.height(4.dp))

        Row {
            Text("${order.sourceCurrency.flag} ${order.sourceCurrency.code}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(4.dp))
            Text("→", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(4.dp))
            Text("${order.targetCurrency.flag} ${order.targetCurrency.code}", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (order.fiatAmount != null) {
                MonoText(text = "${order.sourceCurrency.symbol}${String.format("%.2f", order.fiatAmount)}")
            }
            if (order.createdAt != null) {
                val date = order.createdAt.take(10)
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
