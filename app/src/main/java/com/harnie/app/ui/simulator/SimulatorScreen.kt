package com.harnie.app.ui.simulator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.harnie.app.ui.components.HarnieCard

/**
 * Estructura de datos de reglas de plataformas.
 * - platformName: nombre de la plataforma (no modificable)
 * - buyCommissionPercentStr: comisión de compra en porcentaje (ej: "0.25")
 * - sellCommissionPercentStr: comisión de venta en porcentaje (ej: "0.25")
 * - applyVatSell: aplica IVA en venta (true/false)
 * - vatSellPercentStr: IVA sobre la venta (ej: "15.00")
 */
private data class PlatformRule(
    val platformName: String,
    val buyCommissionPercentStr: String,
    val sellCommissionPercentStr: String,
    val applyVatSell: Boolean,
    val vatSellPercentStr: String
)

private val defaultRules = listOf(
    PlatformRule("El Dorado Ecuador USD", "0.25", "0.25", true, "15.00"),
    PlatformRule("El Dorado Perú PEN", "0.25", "0.25", false, "0.00"),
    PlatformRule("Binance Ecuador USD Maker", "0.35", "0.35", true, "15.00"),
    PlatformRule("Binance Ecuador USD Taker", "0.0", "0.0", true, "15.0"),
    PlatformRule("Binance Perú PEN Maker", "0.25", "0.25", false, "0.0"),
    PlatformRule("Binance Perú PEN Taker", "0.0", "0.0", false, "0.0"),
    PlatformRule("Bybit Ecuador USD", "0.0", "0.0", true, "15.0"),
    PlatformRule("Bybit Perú PEN", "0.0", "0.0", false, "0.0")
)

private data class SimulationResult(
    val realBuyPrice: Double,
    val realSellPrice: Double,
    val profitPercent: Double,
    val meetsTarget: Boolean,
    val minSellPrice: Double,
    val maxBuyPrice: Double
)

private fun calculate(
    buyPlatform: PlatformRule,
    sellPlatform: PlatformRule,
    targetProfit: Double,
    publishedBuyPrice: Double,
    publishedSellPrice: Double
): SimulationResult {
    val buyComm = buyPlatform.buyCommissionPercentStr.toDoubleOrNull() ?: 0.0
    val sellComm = sellPlatform.sellCommissionPercentStr.toDoubleOrNull() ?: 0.0
    val vatSell = sellPlatform.vatSellPercentStr.toDoubleOrNull() ?: 0.0

    // Compra Real = Precio Compra Publicado * (1 + (Comisión Compra / 100))
    val realBuyPrice = publishedBuyPrice * (1 + (buyComm / 100.0))

    // Comisión Venta Total (Condicional)
    val totalSellCommission = if (sellPlatform.applyVatSell) {
        sellComm * (1 + (vatSell / 100.0))
    } else {
        sellComm
    }

    // Venta Real = Precio Venta Publicado * (1 - (Comisión Venta Total / 100))
    val realSellPrice = publishedSellPrice * (1 - (totalSellCommission / 100.0))

    // Rentabilidad Obtenida = ((Venta Real / Compra Real) - 1) * 100
    val profitPercent = if (realBuyPrice > 0.0) {
        ((realSellPrice / realBuyPrice) - 1.0) * 100.0
    } else 0.0

    val meetsTarget = profitPercent >= targetProfit

    // Precio mínimo de venta publicado para cumplir el objetivo
    val minSellPrice = if ((1.0 - totalSellCommission / 100.0) > 0.0) {
        realBuyPrice * (1.0 + targetProfit / 100.0) / (1.0 - totalSellCommission / 100.0)
    } else 0.0

    // Precio máximo de compra publicado para cumplir el objetivo
    val maxBuyPrice = if ((1.0 + buyComm / 100.0) * (1.0 + targetProfit / 100.0) > 0.0) {
        realSellPrice / ((1.0 + buyComm / 100.0) * (1.0 + targetProfit / 100.0))
    } else 0.0

    return SimulationResult(
        realBuyPrice = realBuyPrice,
        realSellPrice = realSellPrice,
        profitPercent = profitPercent,
        meetsTarget = meetsTarget,
        minSellPrice = minSellPrice,
        maxBuyPrice = maxBuyPrice
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    onBack: () -> Unit
) {
    var buyPlatformIndex by remember { mutableStateOf(1) }  // El Dorado Perú PEN
    var sellPlatformIndex by remember { mutableStateOf(7) } // Bybit Perú PEN
    var targetProfit by remember { mutableStateOf("1.50") }
    var buyPrice by remember { mutableStateOf("3.386") }
    var sellPrice by remember { mutableStateOf("3.406") }
    var result by remember { mutableStateOf<SimulationResult?>(null) }
    
    // Estado mutable en memoria de las reglas de comisiones
    var rulesList by remember { mutableStateOf(defaultRules) }
    var showRulesSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulador de Arbitraje") },
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
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Campo 1: Compra en ──
            FieldRow(icon = Icons.Default.ShoppingCart) {
                PlatformDropdown(
                    label = "Compra en",
                    selectedIndex = buyPlatformIndex,
                    rules = rulesList,
                    onSelected = { buyPlatformIndex = it }
                )
            }

            // ── Campo 2: Venta en ──
            FieldRow(icon = Icons.Default.Sell) {
                PlatformDropdown(
                    label = "Venta en",
                    selectedIndex = sellPlatformIndex,
                    rules = rulesList,
                    onSelected = { sellPlatformIndex = it }
                )
            }

            // ── Campo 3: Rentabilidad objetivo ──
            FieldRow(icon = Icons.Default.Percent) {
                OutlinedTextField(
                    value = targetProfit,
                    onValueChange = { targetProfit = it },
                    label = { Text("Rentabilidad objetivo") },
                    suffix = { Text("%") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Campo 4: Precio compra publicado ──
            FieldRow(icon = Icons.AutoMirrored.Filled.TrendingUp) {
                OutlinedTextField(
                    value = buyPrice,
                    onValueChange = { buyPrice = it },
                    label = { Text("Precio compra publicado") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Campo 5: Precio venta publicado ──
            FieldRow(icon = Icons.AutoMirrored.Filled.TrendingUp) {
                OutlinedTextField(
                    value = sellPrice,
                    onValueChange = { sellPrice = it },
                    label = { Text("Precio venta publicado") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Divisor + Botón Calcular ──
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Button(
                onClick = {
                    val target = targetProfit.toDoubleOrNull() ?: 0.0
                    val buy = buyPrice.toDoubleOrNull() ?: 0.0
                    val sell = sellPrice.toDoubleOrNull() ?: 0.0
                    result = calculate(
                        buyPlatform = rulesList[buyPlatformIndex],
                        sellPlatform = rulesList[sellPlatformIndex],
                        targetProfit = target,
                        publishedBuyPrice = buy,
                        publishedSellPrice = sell
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Calcular", style = MaterialTheme.typography.titleMedium)
            }

            // ── Botón Reglas ──
            OutlinedButton(
                onClick = { showRulesSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Reglas", style = MaterialTheme.typography.titleMedium)
            }

            // ── Resultados ──
            if (result != null) {
                val r = result!!

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, if (r.meetsTarget) Color(0xFF4CAF50) else Color(0xFFF44336)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Rentabilidad obtenida",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = String.format("%.2f %%", r.profitPercent),
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                            fontWeight = FontWeight.Black,
                            color = if (r.meetsTarget) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = String.format("Objetivo: %.2f %%", targetProfit.toDoubleOrNull() ?: 0.0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(12.dp))

                        // Pill Badge (CUMPLE / NO CUMPLE)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (r.meetsTarget) Color(0xFF1B5E20) else Color(0xFFB71C1C))
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (r.meetsTarget) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (r.meetsTarget) "CUMPLE / OPERAR" else "NO CUMPLE",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(12.dp))

                        // Key Value Rows - Section 1
                        val buyName = rulesList[buyPlatformIndex].platformName
                        val sellName = rulesList[sellPlatformIndex].platformName

                        DetailSimRow("Compra en", buyName)
                        DetailSimRow("Venta en", sellName)
                        DetailSimRow("Precio compra publicado", String.format("%.3f", buyPrice.toDoubleOrNull() ?: 0.0))
                        DetailSimRow("Precio venta publicado", String.format("%.3f", sellPrice.toDoubleOrNull() ?: 0.0))

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(12.dp))

                        // Key Value Rows - Section 2
                        DetailSimRow("Compra real", String.format("%.3f", r.realBuyPrice))
                        DetailSimRow("Venta real", String.format("%.3f", r.realSellPrice))

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))

                        // Bottom Split Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Column (Venta Mínima)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Venta mínima necesaria",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = String.format("%.3f", r.minSellPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (r.meetsTarget) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            )

                            // Right Column (Compra Máxima)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Compra máxima permitida",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = String.format("%.3f", r.maxBuyPrice),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (r.meetsTarget) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Modal de Reglas (BottomSheet) ──
    if (showRulesSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showRulesSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reglas de Comisiones e IVA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showRulesSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Fila de Encabezados
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Plataforma", modifier = Modifier.width(180.dp), fontWeight = FontWeight.Bold)
                            Text("Com. Compra (%)", modifier = Modifier.width(115.dp), fontWeight = FontWeight.Bold)
                            Text("Com. Venta (%)", modifier = Modifier.width(115.dp), fontWeight = FontWeight.Bold)
                            Text("Aplica IVA", modifier = Modifier.width(100.dp), fontWeight = FontWeight.Bold)
                            Text("IVA (%)", modifier = Modifier.width(90.dp), fontWeight = FontWeight.Bold)
                        }

                        // Filas de Datos
                        rulesList.forEachIndexed { index, rule ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Plataforma (Deshabilitada / Lectura única)
                                OutlinedTextField(
                                    value = rule.platformName,
                                    onValueChange = {},
                                    readOnly = true,
                                    enabled = false,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(180.dp)
                                )

                                // Comisión Compra (Input libre)
                                OutlinedTextField(
                                    value = rule.buyCommissionPercentStr,
                                    onValueChange = { newVal ->
                                        rulesList = rulesList.mapIndexed { i, r ->
                                            if (i == index) r.copy(buyCommissionPercentStr = newVal) else r
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(115.dp)
                                )

                                // Comisión Venta (Input libre)
                                OutlinedTextField(
                                    value = rule.sellCommissionPercentStr,
                                    onValueChange = { newVal ->
                                        rulesList = rulesList.mapIndexed { i, r ->
                                            if (i == index) r.copy(sellCommissionPercentStr = newVal) else r
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(115.dp)
                                )

                                // Aplica IVA Venta (Select Sí/No)
                                var isIvaExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = isIvaExpanded,
                                    onExpandedChange = { isIvaExpanded = it },
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    OutlinedTextField(
                                        value = if (rule.applyVatSell) "Sí" else "No",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isIvaExpanded) },
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = isIvaExpanded,
                                        onDismissRequest = { isIvaExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Sí") },
                                            onClick = {
                                                rulesList = rulesList.mapIndexed { i, r ->
                                                    if (i == index) r.copy(applyVatSell = true) else r
                                                }
                                                isIvaExpanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("No") },
                                            onClick = {
                                                rulesList = rulesList.mapIndexed { i, r ->
                                                    if (i == index) r.copy(applyVatSell = false) else r
                                                }
                                                isIvaExpanded = false
                                            }
                                        )
                                    }
                                }

                                // IVA Venta (Input libre, habilitado condicionalmente)
                                OutlinedTextField(
                                    value = rule.vatSellPercentStr,
                                    onValueChange = { newVal ->
                                        rulesList = rulesList.mapIndexed { i, r ->
                                            if (i == index) r.copy(vatSellPercentStr = newVal) else r
                                        }
                                    },
                                    enabled = rule.applyVatSell,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(90.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldRow(
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlatformDropdown(
    label: String,
    selectedIndex: Int,
    rules: List<PlatformRule>,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = rules[selectedIndex].platformName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            rules.forEachIndexed { index, rule ->
                DropdownMenuItem(
                    text = { Text(rule.platformName) },
                    onClick = {
                        onSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun DetailSimRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

