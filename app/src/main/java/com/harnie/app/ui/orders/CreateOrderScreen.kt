package com.harnie.app.ui.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harnie.app.R
import com.harnie.app.core.model.Country
import com.harnie.app.core.model.Exchange
import com.harnie.app.core.model.OrderType
import com.harnie.app.ui.clients.ClientViewModel
import com.harnie.app.ui.clients.SaveClientUiState
import com.harnie.app.ui.components.HarnieCard
import com.harnie.app.ui.components.ShimmerBox
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateOrderScreen(
    editOrderId: String? = null,
    onOrderCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: OrderViewModel = koinViewModel()
) {
    val isEditMode = editOrderId != null

    LaunchedEffect(editOrderId) {
        if (editOrderId != null) {
            viewModel.prepopulateForEdit(editOrderId)
        } else {
            viewModel.resetForm()
        }
    }

    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val orderType by viewModel.orderType.collectAsStateWithLifecycle()
    val exchange by viewModel.exchange.collectAsStateWithLifecycle()
    val country by viewModel.country.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val fiatAmount by viewModel.fiatAmount.collectAsStateWithLifecycle()
    val pricePerUnit by viewModel.pricePerUnit.collectAsStateWithLifecycle()
    val usdtAmount by viewModel.usdtAmount.collectAsStateWithLifecycle()
    val exchangeCommission by viewModel.exchangeCommission.collectAsStateWithLifecycle()
    val clientName by viewModel.clientName.collectAsStateWithLifecycle()
    val clientLastName by viewModel.clientLastName.collectAsStateWithLifecycle()
    val selectedClientId by viewModel.selectedClientId.collectAsStateWithLifecycle()
    val clientSuggestions by viewModel.clientSuggestions.collectAsStateWithLifecycle()
    val note by viewModel.note.collectAsStateWithLifecycle()
    val orderDate by viewModel.orderDate.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    // Recargar la lista de clientes al volver a esta pantalla (p.ej. tras registrar uno nuevo)
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshClients()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var showClientForm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Calcular la fecha a mostrar
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val displayDate = if (orderDate != null) {
        val parts = orderDate!!.split("-")
        "%s/%s/%s".format(parts[2], parts[1], parts[0])
    } else {
        "%02d/%02d/%04d".format(today.dayOfMonth, today.monthNumber, today.year)
    }

    // DatePicker dialog para elegir fecha pasada
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
                            // Si es hoy, limpiar para usar DEFAULT now()
                            if (date == today) {
                                viewModel.onOrderDateChange(null)
                            } else {
                                viewModel.onOrderDateChange(date.toString())
                            }
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

    LaunchedEffect(createState) {
        when (createState) {
            is CreateOrderUiState.Success -> onOrderCreated()
            is CreateOrderUiState.Error -> snackbarHostState.showSnackbar(
                (createState as CreateOrderUiState.Error).message
            )
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar orden" else "Registrar orden") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. ID y Fecha/Hora ──
            HarnieCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Editando orden" else "Nueva orden",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AssistChip(
                            onClick = { showDatePicker = true },
                            label = { Text(displayDate) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Elegir fecha",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        if (orderDate != null) {
                            IconButton(
                                onClick = { viewModel.onOrderDateChange(null) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Usar fecha de hoy",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. Tipo de operacion ──
            SectionTitle("Tipo de operacion")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderType.entries.forEach { type ->
                    FilterChip(
                        selected = orderType == type,
                        onClick = { viewModel.onOrderTypeChange(type) },
                        label = { Text(type.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == OrderType.BUY)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = if (type == OrderType.BUY)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }

            // ── 3. Exchange ──
            SectionTitle("Exchange")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Exchange.entries.forEach { ex ->
                    FilterChip(
                        selected = exchange == ex,
                        onClick = { viewModel.onExchangeChange(ex) },
                        label = { Text(ex.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                }
            }

            // ── 4. Pais ──
            SectionTitle("Pais")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Country.entries.forEach { c ->
                    FilterChip(
                        selected = country == c,
                        onClick = { viewModel.onCountryChange(c) },
                        label = { Text("${c.flag} ${c.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            // ── 5. Moneda (solo Peru) ──
            if (country == Country.PERU) {
                SectionTitle("Moneda")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PEN" to "Soles", "USD" to "Dolares").forEach { (code, label) ->
                        FilterChip(
                            selected = selectedCurrency == code,
                            onClick = { viewModel.onCurrencyChange(code) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // ── 6. Monto ──
            HarnieCard {
                Text(
                    text = "Monto",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(12.dp))

                // 6.1 Metodo de pago
                val paymentMethods = when (country) {
                    Country.PERU -> listOf(
                        "Yape", "Plin", "Transferencia", "BCP",
                        "Interbank", "BBVA", "Efectivo", "Otro"
                    )
                    Country.ECUADOR -> listOf(
                        "Banco Pichincha", "Banco Guayaquil",
                        "Transferencia de otros bancos"
                    )
                    Country.RUSSIA -> listOf("Transferencia")
                }

                DropdownSelector(
                    label = "Metodo de pago",
                    options = paymentMethods,
                    selected = paymentMethod,
                    onSelected = viewModel::onPaymentMethodChange
                )

                Spacer(Modifier.height(12.dp))

                // 6.4 Fiat amount + price labels
                val isBuy = orderType == OrderType.BUY
                val actionSuffix = if (isBuy) "enviado" else "recibido"

                // Precio por USDT segun la moneda seleccionada del pais
                val priceLabel = "Precio $selectedCurrency por USDT"

                // El USDT se recibe en compra y se envia en venta
                val usdtLabel = if (isBuy) "Monto USDT RECIBIDO" else "Monto USDT ENVIADO"

                val fiatLabel: String = when {
                    country == Country.PERU && selectedCurrency == "PEN" -> "PEN $actionSuffix"
                    country == Country.PERU && selectedCurrency == "USD" -> "USD $actionSuffix"
                    country == Country.ECUADOR -> "USD $actionSuffix"
                    else -> "RUB $actionSuffix"
                }

                NumericField(fiatLabel, fiatAmount, viewModel::onFiatAmountChange)
                Spacer(Modifier.height(8.dp))
                NumericField(priceLabel, pricePerUnit, viewModel::onPricePerUnitChange)
                Spacer(Modifier.height(8.dp))

                // 6.5 Monto USDT
                NumericField(
                    label = usdtLabel,
                    value = usdtAmount,
                    onValueChange = viewModel::onUsdtAmountChange,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_usdt),
                            contentDescription = "USDT",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                Spacer(Modifier.height(8.dp))

                // 6.6 Comision Exchange
                NumericField(
                    "Comision por Exchange (opcional)",
                    exchangeCommission,
                    viewModel::onExchangeCommissionChange
                )
            }

            // ── 7. Datos del cliente (opcional) ──
            HarnieCard {
                Text(
                    text = "Datos del cliente (opcional)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = clientName,
                    onValueChange = viewModel::onClientNameChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = clientLastName,
                    onValueChange = viewModel::onClientLastNameChange,
                    label = { Text("Apellido") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                // Sugerencias de clientes registrados
                if (clientSuggestions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    clientSuggestions.forEach { client ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectClient(client)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = client.fullName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                val countryLabel = try {
                                    val c = Country.valueOf(client.country)
                                    "${c.flag} ${c.displayName}"
                                } catch (_: Exception) { client.country }
                                Text(
                                    text = countryLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (selectedClientId != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Cliente seleccionado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showClientForm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Registrar nuevo cliente")
                }
            }

            // ── 8. Notas y resumen ──
            HarnieCard {
                Text(
                    text = "Notas y resumen",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(12.dp))

                // 8.1 Nota
                OutlinedTextField(
                    value = note,
                    onValueChange = viewModel::onNoteChange,
                    label = { Text("Nota (opcional)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // 8.2 Resumen
                Text(
                    text = "Resumen de la operacion",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            // ── Boton registrar ──
            if (createState is CreateOrderUiState.Submitting) {
                ShimmerBox(height = 52.dp, cornerRadius = 16.dp)
            } else {
                Button(
                    onClick = viewModel::submitOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (isEditMode) "Guardar cambios" else "Registrar orden",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // BottomSheet para registrar nuevo cliente
    if (showClientForm) {
        val clientViewModel: ClientViewModel = koinViewModel()
        val clientSaveState by clientViewModel.saveState.collectAsStateWithLifecycle()
        val cName by clientViewModel.name.collectAsStateWithLifecycle()
        val cLastName by clientViewModel.lastName.collectAsStateWithLifecycle()
        val cCountry by clientViewModel.country.collectAsStateWithLifecycle()
        val cPhone by clientViewModel.phone.collectAsStateWithLifecycle()
        val cEmail by clientViewModel.email.collectAsStateWithLifecycle()
        val cDocType by clientViewModel.documentType.collectAsStateWithLifecycle()
        val cDocNumber by clientViewModel.documentNumber.collectAsStateWithLifecycle()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        LaunchedEffect(clientSaveState) {
            if (clientSaveState is SaveClientUiState.Success) {
                viewModel.onNewClientRegistered(cName, cLastName)
                showClientForm = false
                clientViewModel.resetForm()
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                showClientForm = false
                clientViewModel.resetForm()
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nuevo cliente", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = {
                        showClientForm = false
                        clientViewModel.resetForm()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                OutlinedTextField(
                    value = cName,
                    onValueChange = clientViewModel::onNameChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cLastName,
                    onValueChange = clientViewModel::onLastNameChange,
                    label = { Text("Apellido") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pais", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Country.entries.forEach { c ->
                        FilterChip(
                            selected = cCountry == c,
                            onClick = { clientViewModel.onCountryChange(c) },
                            label = { Text("${c.flag} ${c.displayName}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }

                val cDocTypes = when (cCountry) {
                    Country.ECUADOR -> listOf("Cédula", "Carnet de extranjeria")
                    else -> listOf("DNI", "Carnet de extranjeria", "Pasaporte")
                }
                DropdownSelector(
                    label = "Tipo de documento",
                    options = cDocTypes,
                    selected = cDocType,
                    onSelected = clientViewModel::onDocumentTypeChange
                )

                OutlinedTextField(
                    value = cDocNumber,
                    onValueChange = clientViewModel::onDocumentNumberChange,
                    label = { Text("Numero de documento") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                val cPhonePrefix = when (cCountry) {
                    Country.PERU -> "+51"
                    Country.ECUADOR -> "+593"
                    Country.RUSSIA -> "+7"
                }
                OutlinedTextField(
                    value = cPhone,
                    onValueChange = clientViewModel::onPhoneChange,
                    label = { Text("Numero de celular") },
                    prefix = { Text("$cPhonePrefix ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = cEmail,
                    onValueChange = clientViewModel::onEmailChange,
                    label = { Text("Correo electronico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = clientViewModel::saveClient,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = cName.isNotBlank() && cLastName.isNotBlank()
                ) {
                    Text("Registrar cliente", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun NumericField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
