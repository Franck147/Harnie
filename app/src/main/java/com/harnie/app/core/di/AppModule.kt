package com.harnie.app.core.di

import com.harnie.app.core.network.SupabaseProvider
import com.harnie.app.data.repository.AuthRepositoryImpl
import com.harnie.app.data.repository.BalanceRepositoryImpl
import com.harnie.app.data.repository.ClientRepositoryImpl
import com.harnie.app.data.repository.OrderRepositoryImpl
import com.harnie.app.domain.repository.AuthRepository
import com.harnie.app.domain.repository.BalanceRepository
import com.harnie.app.domain.repository.ClientRepository
import com.harnie.app.domain.repository.OrderRepository
import com.harnie.app.ui.auth.AuthViewModel
import com.harnie.app.ui.clients.ClientViewModel
import com.harnie.app.ui.dashboard.DashboardViewModel
import com.harnie.app.ui.history.HistoryViewModel
import com.harnie.app.ui.orders.OrderViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { SupabaseProvider.client }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<BalanceRepository> { BalanceRepositoryImpl(get()) }
    single<OrderRepository> { OrderRepositoryImpl(get()) }
    single<ClientRepository> { ClientRepositoryImpl(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
    viewModel { OrderViewModel(get(), get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { ClientViewModel(get(), get(), get()) }
}