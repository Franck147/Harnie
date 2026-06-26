package com.harnie.app.domain.repository

import com.harnie.app.ui.dashboard.BalanceItem
import kotlinx.coroutines.flow.Flow

interface BalanceRepository {
    suspend fun getBalances(): List<BalanceItem>
    fun observeBalances(): Flow<List<BalanceItem>>
}