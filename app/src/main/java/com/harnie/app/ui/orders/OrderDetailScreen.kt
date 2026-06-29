package com.harnie.app.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harnie.app.core.util.toLocalDateDisplay
import com.harnie.app.core.util.toLocalTimeDisplay
import com.harnie.app.ui.components.HarnieCard
import com.harnie.app.ui.components.ShimmerBox
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: OrderViewModel = koinViewModel()
) {
    val order by viewModel.detailOrder.collectAsStateWithLifecycle()
    val loading by viewModel.detailLoading.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.loadOrderDetail(orderId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar orden") },
            text = { Text("Estas seguro de que quieres eliminar esta orden?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteOrder(orderId)
                        onDeleted()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de orden") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(orderId) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) {
                repeat(4) {
                    ShimmerBox(height = 80.dp, cornerRadius = 24.dp)
                }
            } else if (order == null) {
                Spacer(Modifier.height(32.dp))
                Text(
                    text = "Orden no encontrada",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val o = order!!

                // Encabezado
                HarnieCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ID: ${o.id.take(8).uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (o.createdAt != null) {
                            val date = o.createdAt.toLocalDateDisplay()
                            val time = o.createdAt.toLocalTimeDisplay()
                            Text(
                                text = "$date  $time",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Info principal
                HarnieCard {
                    Text(
                        text = "Informacion de la orden",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    DetailRow("Tipo", o.orderType.label)
                    DetailRow("Exchange", o.exchange?.replace("_", " ") ?: "-")
                    DetailRow("Pais", o.country ?: "-")
                }

                // Monto
                HarnieCard {
                    Text(
                        text = "Monto",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    DetailRow("Metodo de pago", o.paymentMethod ?: "-")
                    DetailRow(
                        "${o.sourceCurrency.code} ${if (o.orderType.label == "Compra") "enviado" else "recibido"}",
                        if (o.fiatAmount != null) String.format("%.2f", o.fiatAmount) else "-"
                    )
                    DetailRow(
                        "Precio por unidad",
                        if (o.pricePerUnit != null) String.format("%.4f", o.pricePerUnit) else "-"
                    )
                    DetailRow(
                        "Monto USDT ${if (o.orderType.label == "Compra") "recibido" else "enviado"}",
                        if (o.usdtAmount != null) String.format("%.2f", o.usdtAmount) else "-"
                    )
                    if (o.exchangeCommission != null && o.exchangeCommission > 0) {
                        DetailRow("Comision Exchange", String.format("%.2f", o.exchangeCommission))
                    }
                }

                // Datos del cliente
                if (o.clientName != null || o.clientLastName != null) {
                    HarnieCard {
                        Text(
                            text = "Cliente",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        DetailRow("Nombre", "${o.clientName ?: ""} ${o.clientLastName ?: ""}".trim())
                        if (!o.documentNumber.isNullOrBlank()) {
                            DetailRow(documentTypeLabel(o.documentType), o.documentNumber)
                        }
                    }
                }

                // Nota
                if (!o.note.isNullOrEmpty()) {
                    HarnieCard {
                        Text(
                            text = "Nota",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = o.note,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Botones de accion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onEdit(orderId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.padding(start = 8.dp))
                        Text("Editar")
                    }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.padding(start = 8.dp))
                        Text("Eliminar")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/** Etiqueta legible para el tipo de documento almacenado (enum o texto libre). */
private fun documentTypeLabel(raw: String?): String {
    val v = raw?.trim()?.uppercase()?.replace(" ", "_") ?: ""
    return when {
        v.startsWith("DNI") -> "DNI"
        v.startsWith("CARNET") -> "Carnet de extranjeria"
        v.startsWith("PASAPORTE") -> "Pasaporte"
        v.startsWith("CEDULA") || v.startsWith("CÉDULA") -> "Cedula"
        else -> "Documento"
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
