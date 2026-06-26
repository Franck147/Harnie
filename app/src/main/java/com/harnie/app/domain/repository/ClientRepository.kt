package com.harnie.app.domain.repository

import com.harnie.app.data.remote.dto.ClientDto
import com.harnie.app.ui.clients.ClientItem

interface ClientRepository {
    suspend fun getMyClients(): List<ClientItem>
    suspend fun getClientById(id: String): ClientItem?
    suspend fun createClient(dto: ClientDto)
    suspend fun updateClient(id: String, dto: ClientDto)
    suspend fun deleteClient(id: String)
}
