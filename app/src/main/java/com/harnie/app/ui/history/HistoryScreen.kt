package com.harnie.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harnie.app.core.model.TransactionStatus
import com.harnie.app.ui.components.CurrencyChipRow
import com.harnie.app.ui.components.HarnieCard
import com.harnie.app.ui.components.MonoText
import com.harnie.app.ui.components.ShimmerBox
import com.harnie.app.ui.theme.InfoBlue
import com.harnie.app.ui.theme.LossRed
import com.harnie.app.ui.theme.ProfitGreen
import com.harnie.app.ui.theme.WarningAmber
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    Spacer(Modifier.height(8.dp))
                    repeat(6) {
                        ShimmerBox(height = 72.dp, cornerRadius = 24.dp)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                is HistoryUiState.Loaded -> {
                    CurrencyChipRow(
                        selected = state.filterCurrency,
                        onSelected = { currency ->
                            viewModel.filterByCurrency(
                                if (state.filterCurrency == currency) null else currency
                            )
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TransactionStatus.entries.forEach { status ->
                            FilterChip(
                                selected = state.filterStatus == status,
                                onClick = {
                                    viewModel.filterByStatus(
                                        if (state.filterStatus == status) null else status
                                    )
                                },
                                label = { Text(status.label) }
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    val filtered = state.transactions.filter { tx ->
                        (state.filterCurrency == null ||
                                tx.sourceCurrency == state.filterCurrency ||
                                tx.targetCurrency == state.filterCurrency) &&
                        (state.filterStatus == null || tx.status == state.filterStatus)
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filtered, key = { it.id }) { tx ->
                            TransactionCard(tx)
                        }
                    }
                }

                is HistoryUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(tx: HistoryItem) {
    val statusColor = when (tx.status) {
        TransactionStatus.PENDING -> WarningAmber
        TransactionStatus.CONFIRMED -> ProfitGreen
        TransactionStatus.REJECTED -> LossRed
        TransactionStatus.EXPIRED -> InfoBlue
    }

    HarnieCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tx.counterpartyName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = tx.status.label,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor
            )
        }

        Spacer(Modifier.height(4.dp))

        Row {
            Text("${tx.sourceCurrency.flag} ${tx.sourceCurrency.code}")
            Spacer(Modifier.width(4.dp))
            Text("→")
            Spacer(Modifier.width(4.dp))
            Text("${tx.targetCurrency.flag} ${tx.targetCurrency.code}")
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MonoText(text = tx.amount.toPlainString())
            Text(
                text = tx.createdAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}