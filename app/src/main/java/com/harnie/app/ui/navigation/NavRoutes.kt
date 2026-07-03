package com.harnie.app.ui.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {
    @Serializable data object Auth : NavRoute
    @Serializable data object Dashboard : NavRoute
    @Serializable data object OrderList : NavRoute
    @Serializable data object CreateOrder : NavRoute
    @Serializable data class EditOrder(val orderId: String) : NavRoute
    @Serializable data class OrderDetail(val orderId: String) : NavRoute
    @Serializable data object Clients : NavRoute
    @Serializable data class ClientDetail(val clientId: String) : NavRoute
    @Serializable data object History : NavRoute
    @Serializable data object Simulator : NavRoute
}
