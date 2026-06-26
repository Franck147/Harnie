package com.harnie.app.data.repository

import com.harnie.app.data.remote.dto.ClientDto
import com.harnie.app.domain.repository.ClientRepository
import com.harnie.app.ui.clients.ClientItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

class ClientRepositoryImpl(
    private val supabase: SupabaseClient
) : ClientRepository {

    override suspend fun getMyClients(): List<ClientItem> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        val dtos = supabase.postgrest["clients"]
            .select {
                filter { eq("user_id", userId) }
                order("name", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }
            .decodeList<ClientDto>()
        return dtos.map { it.toClientItem() }
    }

    override suspend fun getClientById(id: String): ClientItem? {
        val dtos = supabase.postgrest["clients"]
            .select { filter { eq("id", id) } }
            .decodeList<ClientDto>()
        return dtos.firstOrNull()?.toClientItem()
    }

    override suspend fun createClient(dto: ClientDto) {
        supabase.postgrest["clients"].insert(dto)
    }

    override suspend fun updateClient(id: String, dto: ClientDto) {
        supabase.postgrest["clients"].update(dto) {
            filter { eq("id", id) }
        }
    }

    override suspend fun deleteClient(id: String) {
        supabase.postgrest["clients"].delete {
            filter { eq("id", id) }
        }
    }

    private fun ClientDto.toClientItem() = ClientItem(
        id = id,
        name = name,
        lastName = lastName,
        country = country,
        phone = phone,
        email = email,
        documentType = documentType,
        documentNumber = documentNumber
    )
}
