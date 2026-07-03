package com.harnie.app.data.repository

import com.harnie.app.core.model.Currency
import com.harnie.app.core.model.OrderStatus
import com.harnie.app.core.model.OrderType
import com.harnie.app.core.model.TransactionStatus
import com.harnie.app.data.remote.dto.CreateOrderDto
import com.harnie.app.data.remote.dto.OrderDto
import com.harnie.app.data.remote.dto.TransactionDto
import com.harnie.app.domain.repository.OrderRepository
import com.harnie.app.ui.history.HistoryItem
import com.harnie.app.ui.orders.OrderItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal

class OrderRepositoryImpl(
    private val supabase: SupabaseClient
) : OrderRepository {

    override suspend fun getOpenOrders(): List<OrderItem> {
        val dtos = supabase.postgrest["orders"]
            .select { filter { eq("status", "OPEN") } }
            .decodeList<OrderDto>()
        return dtos.map { it.toOrderItem() }
    }

    override suspend fun getMyOrders(): List<OrderItem> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        val dtos = supabase.postgrest["orders"]
            .select {
                filter { eq("creator_id", userId) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<OrderDto>()
        return dtos.map { it.toOrderItem() }
    }

    override fun observeOrders(): Flow<List<OrderItem>> = flow {
        val channel = supabase.channel("orders-realtime")
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "orders"
        }
        channel.subscribe()
        changes.collect {
            emit(getMyOrders())
        }
    }

    override suspend fun createOrder(dto: CreateOrderDto) {
        supabase.postgrest["orders"].insert(dto)
    }

    override suspend fun updateOrder(id: String, dto: CreateOrderDto) {
        supabase.postgrest["orders"].update(dto) {
            filter { eq("id", id) }
        }
    }

    override suspend fun deleteOrder(id: String) {
        supabase.postgrest["orders"].delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun getOrderById(id: String): OrderItem? {
        val dtos = supabase.postgrest["orders"]
            .select { filter { eq("id", id) } }
            .decodeList<OrderDto>()
        return dtos.firstOrNull()?.toOrderItem()
    }

    override suspend fun getOrdersByClientId(clientId: String): List<OrderItem> {
        val dtos = supabase.postgrest["orders"]
            .select {
                filter { eq("client_id", clientId) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<OrderDto>()
        return dtos.map { it.toOrderItem() }
    }

    override suspend fun getMyTransactions(): List<HistoryItem> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        val dtos = supabase.postgrest["transactions"]
            .select {
                filter {
                    or {
                        eq("buyer_id", userId)
                        eq("seller_id", userId)
                    }
                }
            }
            .decodeList<TransactionDto>()
        return dtos.map { it.toHistoryItem(userId) }
    }

    private fun OrderDto.toOrderItem() = OrderItem(
        id = id,
        creatorName = "Trader",
        creatorRating = 0.0,
        orderType = OrderType.valueOf(orderType),
        sourceCurrency = Currency.fromCode(sourceCurrency),
        targetCurrency = Currency.fromCode(targetCurrency),
        exchangeRate = BigDecimal.valueOf(exchangeRate),
        amount = BigDecimal.valueOf(amount),
        minLimit = BigDecimal.valueOf(minLimit),
        maxLimit = BigDecimal.valueOf(maxLimit),
        status = OrderStatus.valueOf(status),
        exchange = exchange,
        country = country,
        paymentMethod = paymentMethod,
        fiatAmount = fiatAmount,
        pricePerUnit = pricePerUnit,
        usdtAmount = usdtAmount,
        exchangeCommission = exchangeCommission,
        documentType = documentType,
        documentNumber = documentNumber,
        clientPhone = clientPhone,
        clientEmail = clientEmail,
        clientId = clientId,
        clientName = clientName,
        clientLastName = clientLastName,
        note = note,
        shortId = shortId,
        createdAt = createdAt
    )

    private fun TransactionDto.toHistoryItem(currentUserId: String) = HistoryItem(
        id = id,
        counterpartyName = if (buyerId == currentUserId) "Vendedor" else "Comprador",
        sourceCurrency = Currency.fromCode(sourceCurrency),
        targetCurrency = Currency.fromCode(targetCurrency),
        amount = BigDecimal.valueOf(amount),
        exchangeRate = BigDecimal.valueOf(exchangeRate),
        status = TransactionStatus.valueOf(status),
        createdAt = createdAt
    )
}
