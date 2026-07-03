package com.harnie.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harnie.app.core.model.Country
import com.harnie.app.core.model.Currency
import com.harnie.app.core.model.Exchange
import com.harnie.app.core.model.OrderType
import com.harnie.app.data.remote.dto.CreateOrderDto
import com.harnie.app.domain.repository.AuthRepository
import com.harnie.app.domain.repository.ClientRepository
import com.harnie.app.domain.repository.OrderRepository
import com.harnie.app.ui.clients.ClientItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderViewModel(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    // --- Order list ---

    private val _listState = MutableStateFlow<OrderListUiState>(OrderListUiState.Loading)
    val listState: StateFlow<OrderListUiState> = _listState.asStateFlow()

    // --- Order detail ---

    private val _detailOrder = MutableStateFlow<OrderItem?>(null)
    val detailOrder: StateFlow<OrderItem?> = _detailOrder.asStateFlow()

    private val _detailLoading = MutableStateFlow(false)
    val detailLoading: StateFlow<Boolean> = _detailLoading.asStateFlow()

    // --- Create/Edit order form ---

    private val _createState = MutableStateFlow<CreateOrderUiState>(CreateOrderUiState.Idle)
    val createState: StateFlow<CreateOrderUiState> = _createState.asStateFlow()

    private val _editingOrderId = MutableStateFlow<String?>(null)
    val editingOrderId: StateFlow<String?> = _editingOrderId.asStateFlow()

    private val _orderType = MutableStateFlow(OrderType.BUY)
    val orderType: StateFlow<OrderType> = _orderType.asStateFlow()

    private val _exchange = MutableStateFlow(Exchange.EL_DORADO)
    val exchange: StateFlow<Exchange> = _exchange.asStateFlow()

    private val _country = MutableStateFlow(Country.PERU)
    val country: StateFlow<Country> = _country.asStateFlow()

    private val _selectedCurrency = MutableStateFlow("PEN")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _paymentMethod = MutableStateFlow("")
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _fiatAmount = MutableStateFlow("")
    val fiatAmount: StateFlow<String> = _fiatAmount.asStateFlow()

    private val _pricePerUnit = MutableStateFlow("")
    val pricePerUnit: StateFlow<String> = _pricePerUnit.asStateFlow()

    private val _usdtAmount = MutableStateFlow("")
    val usdtAmount: StateFlow<String> = _usdtAmount.asStateFlow()

    private val _exchangeCommission = MutableStateFlow("")
    val exchangeCommission: StateFlow<String> = _exchangeCommission.asStateFlow()

    private val _clientName = MutableStateFlow("")
    val clientName: StateFlow<String> = _clientName.asStateFlow()

    private val _clientLastName = MutableStateFlow("")
    val clientLastName: StateFlow<String> = _clientLastName.asStateFlow()

    private val _selectedClientId = MutableStateFlow<String?>(null)
    val selectedClientId: StateFlow<String?> = _selectedClientId.asStateFlow()

    private val _documentType = MutableStateFlow<String?>(null)
    private val _documentNumber = MutableStateFlow<String?>(null)

    private val _clientSuggestions = MutableStateFlow<List<ClientItem>>(emptyList())
    val clientSuggestions: StateFlow<List<ClientItem>> = _clientSuggestions.asStateFlow()

    private var _allClients: List<ClientItem> = emptyList()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    // Fecha personalizada (null = ahora / hoy)
    private val _orderDate = MutableStateFlow<String?>(null)
    val orderDate: StateFlow<String?> = _orderDate.asStateFlow()

    init {
        loadMyOrders()
        observeOrders()
        loadClients()
    }

    private fun loadClients() {
        viewModelScope.launch {
            try {
                _allClients = clientRepository.getMyClients()
                // Re-aplicar la busqueda actual ahora que ya hay clientes cargados
                if (_selectedClientId.value == null) {
                    searchClients(_clientName.value, _clientLastName.value)
                }
                // Re-enriquecer ordenes ya cargadas por si los clientes llegaron despues.
                val current = _listState.value
                if (current is OrderListUiState.Loaded) {
                    _listState.value = current.copy(orders = enrichWithClientDocs(current.orders))
                }
            } catch (_: Exception) { }
        }
    }

    /**
     * Completa el documento (DNI/carnet) de las ordenes que tienen cliente vinculado
     * pero no guardaron el documento, tomandolo del cliente registrado. Asi el CSV y el
     * detalle muestran el dato aunque la orden sea antigua o se haya creado sin adjuntarlo.
     */
    private fun enrichWithClientDocs(orders: List<OrderItem>): List<OrderItem> {
        if (_allClients.isEmpty()) return orders
        return orders.map { o ->
            if (!o.documentNumber.isNullOrBlank() || o.clientId.isNullOrBlank()) {
                o
            } else {
                val client = _allClients.firstOrNull { it.id == o.clientId }
                if (client?.documentNumber.isNullOrBlank()) {
                    o
                } else {
                    o.copy(
                        documentType = o.documentType ?: client?.documentType,
                        documentNumber = client?.documentNumber
                    )
                }
            }
        }
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            _listState.value = OrderListUiState.Loading
            try {
                val orders = enrichWithClientDocs(orderRepository.getMyOrders())
                _listState.value = OrderListUiState.Loaded(orders = orders)
            } catch (e: Exception) {
                _listState.value = OrderListUiState.Error(e.message ?: "Error")
            }
        }
    }

    /**
     * Recarga las ordenes sin mostrar el estado de carga ni perder los filtros.
     * Util al volver a la lista tras crear/editar una orden.
     */
    fun refreshMyOrders() {
        viewModelScope.launch {
            try {
                val orders = enrichWithClientDocs(orderRepository.getMyOrders())
                val current = _listState.value
                if (current is OrderListUiState.Loaded) {
                    _listState.value = current.copy(orders = orders)
                } else {
                    _listState.value = OrderListUiState.Loaded(orders = orders)
                }
            } catch (e: Exception) {
                if (_listState.value !is OrderListUiState.Loaded) {
                    _listState.value = OrderListUiState.Error(e.message ?: "Error")
                }
            }
        }
    }

    private fun observeOrders() {
        viewModelScope.launch {
            orderRepository.observeOrders()
                .catch { }
                .collect { orders ->
                    val current = _listState.value
                    if (current is OrderListUiState.Loaded) {
                        _listState.value = current.copy(orders = enrichWithClientDocs(orders))
                    }
                }
        }
    }

    fun filterByType(type: OrderType?) {
        val current = _listState.value
        if (current is OrderListUiState.Loaded) {
            _listState.value = current.copy(filterType = type)
        }
    }

    fun filterByCurrency(currency: Currency?) {
        val current = _listState.value
        if (current is OrderListUiState.Loaded) {
            _listState.value = current.copy(filterCurrency = currency)
        }
    }

    fun filterByCountry(country: Country?) {
        val current = _listState.value
        if (current is OrderListUiState.Loaded) {
            _listState.value = current.copy(filterCountry = country)
        }
    }

    fun filterByDate(date: String?) {
        val current = _listState.value
        if (current is OrderListUiState.Loaded) {
            _listState.value = current.copy(filterDate = date)
        }
    }

    fun filterByBelowMin(enabled: Boolean) {
        val current = _listState.value
        if (current is OrderListUiState.Loaded) {
            _listState.value = current.copy(filterBelowMin = enabled)
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            try {
                orderRepository.deleteOrder(orderId)
                loadMyOrders()
            } catch (_: Exception) { }
        }
    }

    fun loadOrderDetail(orderId: String) {
        viewModelScope.launch {
            _detailLoading.value = true
            try {
                _detailOrder.value = orderRepository.getOrderById(orderId)
            } catch (_: Exception) {
                _detailOrder.value = null
            }
            _detailLoading.value = false
        }
    }

    // --- Create/Edit order ---

    fun prepopulateForEdit(orderId: String) {
        viewModelScope.launch {
            _createState.value = CreateOrderUiState.Idle
            try {
                val order = orderRepository.getOrderById(orderId) ?: return@launch
                _editingOrderId.value = orderId
                _orderType.value = order.orderType
                _exchange.value = try {
                    Exchange.valueOf(order.exchange ?: "EL_DORADO")
                } catch (_: Exception) { Exchange.EL_DORADO }
                _country.value = try {
                    Country.valueOf(order.country ?: "PERU")
                } catch (_: Exception) { Country.PERU }
                _selectedCurrency.value = order.sourceCurrency.code
                _paymentMethod.value = order.paymentMethod ?: ""
                _fiatAmount.value = order.fiatAmount?.let { String.format("%.2f", it) } ?: ""
                _pricePerUnit.value = order.pricePerUnit?.let { String.format("%.4f", it) } ?: ""
                _usdtAmount.value = order.usdtAmount?.let { String.format("%.2f", it) } ?: ""
                _exchangeCommission.value = order.exchangeCommission?.let { String.format("%.2f", it) } ?: ""
                _clientName.value = order.clientName ?: ""
                _clientLastName.value = order.clientLastName ?: ""
                _selectedClientId.value = order.clientId
                _documentType.value = order.documentType
                _documentNumber.value = order.documentNumber
                _note.value = order.note ?: ""
            } catch (_: Exception) { }
        }
    }

    fun resetForm() {
        _editingOrderId.value = null
        _createState.value = CreateOrderUiState.Idle
        _orderType.value = OrderType.BUY
        _exchange.value = Exchange.EL_DORADO
        _country.value = Country.PERU
        _selectedCurrency.value = "PEN"
        _paymentMethod.value = ""
        _fiatAmount.value = ""
        _pricePerUnit.value = ""
        _usdtAmount.value = ""
        _exchangeCommission.value = ""
        _clientName.value = ""
        _clientLastName.value = ""
        _selectedClientId.value = null
        _documentType.value = null
        _documentNumber.value = null
        _clientSuggestions.value = emptyList()
        _note.value = ""
        _orderDate.value = null
    }

    fun onOrderTypeChange(type: OrderType) { _orderType.value = type }
    fun onExchangeChange(exchange: Exchange) { _exchange.value = exchange }

    fun onCountryChange(country: Country) {
        _country.value = country
        _paymentMethod.value = ""
        _selectedCurrency.value = when (country) {
            Country.PERU -> "PEN"
            Country.ECUADOR -> "USD"
            Country.RUSSIA -> "RUB"
        }
    }

    fun onCurrencyChange(currency: String) { _selectedCurrency.value = currency }
    fun onPaymentMethodChange(method: String) { _paymentMethod.value = method }
    fun onFiatAmountChange(v: String) { _fiatAmount.value = v }
    fun onPricePerUnitChange(v: String) { _pricePerUnit.value = v }
    fun onUsdtAmountChange(v: String) { _usdtAmount.value = v }
    fun onExchangeCommissionChange(v: String) { _exchangeCommission.value = v }
    fun onNoteChange(v: String) { _note.value = v }
    fun onOrderDateChange(date: String?) { _orderDate.value = date }

    fun onClientNameChange(v: String) {
        _clientName.value = v
        _selectedClientId.value = null
        _documentType.value = null
        _documentNumber.value = null
        searchClients(v, _clientLastName.value)
    }

    fun onClientLastNameChange(v: String) {
        _clientLastName.value = v
        _selectedClientId.value = null
        _documentType.value = null
        _documentNumber.value = null
        searchClients(_clientName.value, v)
    }

    private fun searchClients(name: String, lastName: String) {
        val n = name.trim().lowercase()
        val l = lastName.trim().lowercase()
        val query = "$n $l".trim()
        if (query.length < 2) {
            _clientSuggestions.value = emptyList()
            return
        }
        _clientSuggestions.value = _allClients.filter { client ->
            client.fullName.lowercase().contains(query) ||
            (n.isNotEmpty() && client.name.lowercase().contains(n)) ||
            (l.isNotEmpty() && client.lastName.lowercase().contains(l))
        }.take(5)
    }

    fun selectClient(client: ClientItem) {
        _clientName.value = client.name
        _clientLastName.value = client.lastName
        _selectedClientId.value = client.id
        _documentType.value = client.documentType
        _documentNumber.value = client.documentNumber
        _clientSuggestions.value = emptyList()
    }

    fun clearClientSuggestions() {
        _clientSuggestions.value = emptyList()
    }

    fun refreshClients() {
        loadClients()
    }

    /**
     * Tras registrar un cliente nuevo desde el formulario de la orden,
     * recarga la lista y lo selecciona automaticamente (sin refrescar la pagina).
     */
    fun onNewClientRegistered(name: String, lastName: String) {
        viewModelScope.launch {
            try {
                _allClients = clientRepository.getMyClients()
            } catch (_: Exception) { }

            val match = _allClients.firstOrNull {
                it.name.equals(name.trim(), ignoreCase = true) &&
                it.lastName.equals(lastName.trim(), ignoreCase = true)
            }
            if (match != null) {
                selectClient(match)
            } else {
                _clientName.value = name.trim()
                _clientLastName.value = lastName.trim()
                _clientSuggestions.value = emptyList()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val summary: StateFlow<String> = combine(
        _orderType, _exchange, _country, _selectedCurrency,
        _paymentMethod, _fiatAmount, _pricePerUnit, _usdtAmount,
        _exchangeCommission, _clientName, _clientLastName,
        _documentType, _documentNumber
    ) { values ->
        val type = values[0] as OrderType
        val exch = values[1] as Exchange
        val ctry = values[2] as Country
        val curr = values[3] as String
        val payment = values[4] as String
        val fiat = (values[5] as String).ifEmpty { "0" }
        val price = (values[6] as String).ifEmpty { "0" }
        val usdt = (values[7] as String).ifEmpty { "0" }
        val commission = values[8] as String
        val cName = (values[9] as String).trim()
        val cLastName = (values[10] as String).trim()
        val docType = values[11] as? String
        val docNumber = values[12] as? String

        val action = if (type == OrderType.BUY) "Compra" else "Venta"
        val fiatLabel = if (type == OrderType.BUY) "enviado" else "recibido"
        val clientFullName = "$cName $cLastName".trim()

        buildString {
            appendLine("Tipo: $action")
            appendLine("Exchange: ${exch.displayName}")
            appendLine("Pais: ${ctry.displayName}")
            appendLine("Moneda: $curr")
            appendLine("Metodo de pago: ${payment.ifEmpty { "-" }}")
            appendLine("$curr $fiatLabel: $fiat")
            appendLine("Precio: $price")
            appendLine("Monto USDT: $usdt")
            if (commission.isNotEmpty()) {
                appendLine("Comision Exchange: $commission")
            }
            appendLine("Cliente: ${clientFullName.ifEmpty { "-" }}")
            if (!docNumber.isNullOrBlank()) {
                val docLabel = when {
                    docType?.uppercase()?.startsWith("DNI") == true -> "DNI"
                    docType?.uppercase()?.startsWith("CARNET") == true -> "CE"
                    docType?.uppercase()?.startsWith("PASAPORTE") == true -> "Pasaporte"
                    docType?.uppercase()?.startsWith("CEDULA") == true || docType?.uppercase()?.startsWith("CÉDULA") == true -> "Cedula"
                    else -> "Doc"
                }
                appendLine("$docLabel: $docNumber")
            }
        }.trimEnd()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    /**
     * Convierte la etiqueta de tipo de documento del cliente (texto libre) al valor
     * del ENUM document_type de la tabla orders. Devuelve null si no hay equivalencia
     * (p. ej. "Cedula") para evitar que falle el insert por una violacion de enum.
     */
    private fun mapDocumentTypeToEnum(raw: String?): String? {
        val v = raw?.trim()?.lowercase() ?: return null
        return when {
            v.isEmpty() -> null
            v.startsWith("dni") -> "DNI"
            v.startsWith("carnet") -> "CARNET_EXTRANJERIA"
            v.startsWith("pasaporte") -> "PASAPORTE"
            else -> null
        }
    }

    fun submitOrder() {
        viewModelScope.launch {
            _createState.value = CreateOrderUiState.Submitting
            try {
                val userId = authRepository.getCurrentUserId()
                    ?: throw IllegalStateException("Usuario no autenticado")
                val fiat = _fiatAmount.value.toDoubleOrNull() ?: 0.0
                val price = _pricePerUnit.value.toDoubleOrNull() ?: 0.0
                val usdt = _usdtAmount.value.toDoubleOrNull() ?: 0.0
                val commission = _exchangeCommission.value.toDoubleOrNull()

                val curr = _selectedCurrency.value
                val sourceCurr = curr
                val targetCurr = if (curr == "PEN") "USD" else if (curr == "RUB") "USD" else "PEN"

                // Fallback: si no se toco la sugerencia de cliente (no hay documento cargado)
                // pero el nombre/apellido coincide exactamente con un cliente registrado,
                // recuperamos su documento para que aparezca en el CSV.
                if (_documentNumber.value.isNullOrBlank()) {
                    val name = _clientName.value.trim()
                    val lastName = _clientLastName.value.trim()
                    if (name.isNotEmpty()) {
                        val match = _allClients.firstOrNull {
                            it.name.equals(name, ignoreCase = true) &&
                            it.lastName.equals(lastName, ignoreCase = true)
                        }
                        if (match != null) {
                            _selectedClientId.value = match.id
                            _documentType.value = match.documentType
                            _documentNumber.value = match.documentNumber
                        }
                    }
                }

                val dto = CreateOrderDto(
                    creatorId = userId,
                    orderType = _orderType.value.name,
                    sourceCurrency = sourceCurr,
                    targetCurrency = targetCurr,
                    exchangeRate = price,
                    amount = fiat,
                    minLimit = fiat,
                    maxLimit = fiat,
                    exchange = _exchange.value.name,
                    country = _country.value.name,
                    paymentMethod = _paymentMethod.value,
                    fiatAmount = fiat,
                    pricePerUnit = price,
                    usdtAmount = usdt,
                    exchangeCommission = commission,
                    // orders.document_type es un ENUM ('DNI','CARNET_EXTRANJERIA','PASAPORTE');
                    // los clientes guardan etiquetas libres, asi que mapeamos a un valor valido o null.
                    documentType = mapDocumentTypeToEnum(_documentType.value),
                    documentNumber = _documentNumber.value,
                    clientId = _selectedClientId.value,
                    clientName = _clientName.value.trim().ifEmpty { null },
                    clientLastName = _clientLastName.value.trim().ifEmpty { null },
                    note = _note.value.ifEmpty { null },
                    createdAt = _orderDate.value?.let { "${it}T00:00:00-05:00" }
                )

                val editId = _editingOrderId.value
                if (editId != null) {
                    orderRepository.updateOrder(editId, dto)
                } else {
                    orderRepository.createOrder(dto)
                }
                _createState.value = CreateOrderUiState.Success
            } catch (e: Exception) {
                _createState.value = CreateOrderUiState.Error(e.message ?: "Error al guardar orden")
            }
        }
    }
}
