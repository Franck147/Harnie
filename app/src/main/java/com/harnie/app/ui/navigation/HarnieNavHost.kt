package com.harnie.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.harnie.app.ui.auth.AuthScreen
import com.harnie.app.ui.clients.ClientDetailScreen
import com.harnie.app.ui.clients.ClientsScreen
import com.harnie.app.ui.dashboard.DashboardScreen
import com.harnie.app.ui.history.HistoryScreen
import com.harnie.app.ui.orders.CreateOrderScreen
import com.harnie.app.ui.orders.OrderDetailScreen
import com.harnie.app.ui.orders.OrderListScreen

@Composable
fun HarnieNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Auth,
        modifier = modifier
    ) {
        composable<NavRoute.Auth> {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(NavRoute.Dashboard) {
                        popUpTo(NavRoute.Auth) { inclusive = true }
                    }
                }
            )
        }

        composable<NavRoute.Dashboard> {
            DashboardScreen(
                onNavigateToOrders = { navController.navigate(NavRoute.OrderList) },
                onNavigateToClients = { navController.navigate(NavRoute.Clients) },
                onNavigateToCreateOrder = { navController.navigate(NavRoute.CreateOrder) }
            )
        }

        composable<NavRoute.OrderList> {
            OrderListScreen(
                onNavigateToCreateOrder = { navController.navigate(NavRoute.CreateOrder) },
                onNavigateToDetail = { orderId ->
                    navController.navigate(NavRoute.OrderDetail(orderId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<NavRoute.CreateOrder> {
            CreateOrderScreen(
                onOrderCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable<NavRoute.EditOrder> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.EditOrder>()
            CreateOrderScreen(
                editOrderId = route.orderId,
                onOrderCreated = {
                    navController.popBackStack(NavRoute.OrderList, inclusive = false)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<NavRoute.OrderDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.OrderDetail>()
            OrderDetailScreen(
                orderId = route.orderId,
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() },
                onEdit = { orderId ->
                    navController.navigate(NavRoute.EditOrder(orderId))
                }
            )
        }

        composable<NavRoute.Clients> {
            ClientsScreen(
                onBack = { navController.popBackStack() },
                onClientDetail = { clientId ->
                    navController.navigate(NavRoute.ClientDetail(clientId))
                }
            )
        }

        composable<NavRoute.ClientDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<NavRoute.ClientDetail>()
            ClientDetailScreen(
                clientId = route.clientId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<NavRoute.History> {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
