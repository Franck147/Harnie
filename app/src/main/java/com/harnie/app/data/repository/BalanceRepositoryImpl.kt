package com.harnie.app.data.repository

import com.harnie.app.core.model.Currency
import com.harnie.app.core.model.Money
import com.harnie.app.data.remote.dto.BalanceDto
import com.harnie.app.domain.repository.BalanceRepository
import com.harnie.app.ui.dashboard.BalanceItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BalanceRepositoryImpl(
    private val supabase: SupabaseClient
) : BalanceRepository {

    override suspend fun getBalances(): List<BalanceItem> {
        val userId = supabase.auth.currentUserOrNull()?.id ?: return emptyList()
        val dtos = supabase.postgrest["balances"]
            .select { filter { eq("user_id", userId) } }
            .decodeList<BalanceDto>()
        return dtos.map { it.toBalanceItem() }
    }

    override fun observeBalances(): Flow<List<BalanceItem>> = flow {
        val channel = supabase.channel("balances-realtime")
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "balances"
        }
        channel.subscribe()
        changes.collect {
            emit(getBalances())
        }
    }

    private fun BalanceDto.toBalanceItem(): BalanceItem {
        val curr = Currency.fromCode(currency)
        return BalanceItem(
            available = Money.of(available, curr),
            frozen = Money.of(frozen, curr)
        )
    }
}
