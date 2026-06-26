package com.harnie.app.domain.repository

import com.harnie.app.data.remote.dto.CreateOrderDto
import com.harnie.app.ui.history.HistoryItem
import com.harnie.app.ui.orders.OrderItem
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun getOpenOrders(): List<OrderItem>
    suspend fun getMyOrders(): List<OrderItem>
    fun observeOrders(): Flow<List<OrderItem>>
    suspend fun createOrder(dto: CreateOrderDto)
    suspend fun updateOrder(id: String, dto: CreateOrderDto)
    suspend fun deleteOrder(id: String)
    suspend fun getOrderById(id: String): OrderItem?
    suspend fun getOrdersByClientId(clientId: String): List<OrderItem>
    suspend fun getMyTransactions(): List<HistoryItem>
}
