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

    private val _clientSuggestions = MutableStateFlow<List<ClientItem>>(emptyList())
    val clientSuggestions: StateFlow<List<ClientItem>> = _clientSuggestions.asStateFlow()

    private var _allClients: List<ClientItem> = emptyList()

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note.asStateFlow()

    init {
        loadMyOrders()
        observeOrders()
        loadClients()
    }

    private fun loadClients() {
        viewModelScope.launch {
            try {
                _allClients = clientRepository.getMyClients()
            } catch (_: Exception) { }
        }
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            _listState.value = OrderListUiState.Loading
            try {
                val orders = orderRepository.getMyOrders()
                _listState.value = OrderListUiState.Loaded(orders = orders)
            } catch (e: Exception) {
                _listState.value = OrderListUiState.Error(e.message ?: "Error")
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
                        _listState.value = current.copy(orders = orders)
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
        _clientSuggestions.value = emptyList()
        _note.value = ""
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

    fun onClientNameChange(v: String) {
        _clientName.value = v
        _selectedClientId.value = null
        searchClients(v, _clientLastName.value)
    }

    fun onClientLastNameChange(v: String) {
        _clientLastName.value = v
        _selectedClientId.value = null
        searchClients(_clientName.value, v)
    }

    private fun searchClients(name: String, lastName: String) {
        val query = "$name $lastName".trim().lowercase()
        if (query.length < 2) {
            _clientSuggestions.value = emptyList()
            return
        }
        _clientSuggestions.value = _allClients.filter {
            it.fullName.lowercase().contains(query) ||
            it.name.lowercase().contains(name.lowercase()) ||
            it.lastName.lowercase().contains(lastName.lowercase())
        }.take(5)
    }

    fun selectClient(client: ClientItem) {
        _clientName.value = client.name
        _clientLastName.value = client.lastName
        _selectedClientId.value = client.id
        _clientSuggestions.value = emptyList()
    }

    fun clearClientSuggestions() {
        _clientSuggestions.value = emptyList()
    }

    fun refreshClients() {
        loadClients()
    }

    @Suppress("UNCHECKED_CAST")
    val summary: StateFlow<String> = combine(
        _orderType, _exchange, _country, _selectedCurrency,
        _paymentMethod, _fiatAmount, _pricePerUnit, _usdtAmount,
        _exchangeCommission
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

        val action = if (type == OrderType.BUY) "Compra" else "Venta"
        val fiatLabel = if (type == OrderType.BUY) "enviado" else "recibido"

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
        }.trimEnd()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

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
                    clientId = _selectedClientId.value,
                    clientName = _clientName.value.trim().ifEmpty { null },
                    clientLastName = _clientLastName.value.trim().ifEmpty { null },
                    note = _note.value.ifEmpty { null }
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
