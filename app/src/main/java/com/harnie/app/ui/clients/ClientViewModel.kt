package com.harnie.app.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harnie.app.core.model.Country
import com.harnie.app.data.remote.dto.ClientDto
import com.harnie.app.domain.repository.AuthRepository
import com.harnie.app.domain.repository.ClientRepository
import com.harnie.app.domain.repository.OrderRepository
import com.harnie.app.ui.orders.OrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClientViewModel(
    private val clientRepository: ClientRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<ClientListUiState>(ClientListUiState.Loading)
    val listState: StateFlow<ClientListUiState> = _listState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveClientUiState>(SaveClientUiState.Idle)
    val saveState: StateFlow<SaveClientUiState> = _saveState.asStateFlow()

    private val _clientOrders = MutableStateFlow<List<OrderItem>>(emptyList())
    val clientOrders: StateFlow<List<OrderItem>> = _clientOrders.asStateFlow()

    private val _selectedClient = MutableStateFlow<ClientItem?>(null)
    val selectedClient: StateFlow<ClientItem?> = _selectedClient.asStateFlow()

    // Form fields
    private val _editingClientId = MutableStateFlow<String?>(null)
    val editingClientId: StateFlow<String?> = _editingClientId.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    private val _country = MutableStateFlow(Country.PERU)
    val country: StateFlow<Country> = _country.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _documentType = MutableStateFlow("")
    val documentType: StateFlow<String> = _documentType.asStateFlow()

    private val _documentNumber = MutableStateFlow("")
    val documentNumber: StateFlow<String> = _documentNumber.asStateFlow()

    init {
        loadClients()
    }

    fun loadClients() {
        viewModelScope.launch {
            _listState.value = ClientListUiState.Loading
            try {
                val clients = clientRepository.getMyClients()
                val allOrders = orderRepository.getMyOrders()

                val enriched = clients.map { client ->
                    val clientOrders = allOrders.filter { it.clientId == client.id }
                    client.copy(
                        buyCount = clientOrders.count { it.orderType.name == "BUY" },
                        sellCount = clientOrders.count { it.orderType.name == "SELL" }
                    )
                }
                _listState.value = ClientListUiState.Loaded(enriched)
            } catch (e: Exception) {
                _listState.value = ClientListUiState.Error(e.message ?: "Error al cargar clientes")
            }
        }
    }

    fun loadClientOrders(clientId: String) {
        viewModelScope.launch {
            try {
                val client = clientRepository.getClientById(clientId)
                _selectedClient.value = client
                _clientOrders.value = orderRepository.getOrdersByClientId(clientId)
            } catch (_: Exception) {
                _clientOrders.value = emptyList()
            }
        }
    }

    fun onNameChange(v: String) { _name.value = v }
    fun onLastNameChange(v: String) { _lastName.value = v }
    fun onCountryChange(c: Country) {
        _country.value = c
        _documentType.value = ""
    }
    fun onPhoneChange(v: String) { _phone.value = v }
    fun onEmailChange(v: String) { _email.value = v }
    fun onDocumentTypeChange(v: String) { _documentType.value = v }
    fun onDocumentNumberChange(v: String) { _documentNumber.value = v }

    fun resetForm() {
        _editingClientId.value = null
        _saveState.value = SaveClientUiState.Idle
        _name.value = ""
        _lastName.value = ""
        _country.value = Country.PERU
        _phone.value = ""
        _email.value = ""
        _documentType.value = ""
        _documentNumber.value = ""
    }

    fun prepopulateForEdit(client: ClientItem) {
        _editingClientId.value = client.id
        _saveState.value = SaveClientUiState.Idle
        _name.value = client.name
        _lastName.value = client.lastName
        _country.value = try {
            Country.valueOf(client.country)
        } catch (_: Exception) { Country.PERU }
        _phone.value = client.phone ?: ""
        _email.value = client.email ?: ""
        _documentType.value = client.documentType ?: ""
        _documentNumber.value = client.documentNumber ?: ""
    }

    fun saveClient() {
        viewModelScope.launch {
            _saveState.value = SaveClientUiState.Saving
            try {
                val userId = authRepository.getCurrentUserId()
                    ?: throw IllegalStateException("Usuario no autenticado")

                val dto = ClientDto(
                    userId = userId,
                    name = _name.value.trim(),
                    lastName = _lastName.value.trim(),
                    country = _country.value.name,
                    phone = _phone.value.trim().ifEmpty { null },
                    email = _email.value.trim().ifEmpty { null },
                    documentType = _documentType.value.ifEmpty { null },
                    documentNumber = _documentNumber.value.trim().ifEmpty { null }
                )

                val editId = _editingClientId.value
                if (editId != null) {
                    clientRepository.updateClient(editId, dto)
                } else {
                    clientRepository.createClient(dto)
                }
                _saveState.value = SaveClientUiState.Success
                loadClients()
            } catch (e: Exception) {
                _saveState.value = SaveClientUiState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            try {
                clientRepository.deleteClient(clientId)
                loadClients()
            } catch (_: Exception) { }
        }
    }
}
