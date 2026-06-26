package com.harnie.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.harnie.app.core.model.Currency

@Composable
fun CurrencyChip(
    currency: Currency,
    selected: Boolean,
    onSelected: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = { onSelected(currency) },
        label = {
            Text(
                text = currency.flag,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = currency.code,
                style = MaterialTheme.typography.labelLarge
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}

@Composable
fun CurrencyChipRow(
    currencies: List<Currency> = Currency.entries,
    selected: Currency?,
    onSelected: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(modifier = modifier) {
        currencies.forEach { currency ->
            CurrencyChip(
                currency = currency,
                selected = currency == selected,
                onSelected = onSelected
            )
            Spacer(Modifier.width(8.dp))
        }
    }
}