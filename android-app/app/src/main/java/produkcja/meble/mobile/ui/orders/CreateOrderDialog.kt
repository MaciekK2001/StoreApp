package produkcja.meble.mobile.ui.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import produkcja.meble.mobile.data.CreateOrderRequest
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.OrderItemRequest
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.ui.common.money
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun String.parseAmount(): Double {
    val clean = this.replace(',', '.').replace(" ", "").trim()
    return clean.toDoubleOrNull() ?: 0.0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormDialog(
    products: List<Product>,
    initialOrder: FurnitureOrder? = null,
    onDismiss: () -> Unit,
    onSave: (
        request: CreateOrderRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) -> Unit,
) {
    // Delivery & customer fields
    var address by rememberSaveable { mutableStateOf(initialOrder?.address.orEmpty()) }
    var phoneNumber by rememberSaveable { mutableStateOf(initialOrder?.phoneNumber.orEmpty()) }
    var fulfillmentTime by rememberSaveable {
        mutableStateOf(initialOrder?.fulfillmentTime?.toString() ?: "14")
    }
    var contactRemarks by rememberSaveable { mutableStateOf(initialOrder?.contactRemarks.orEmpty()) }

    // Delivery date state (default: parsed date or today + 14 days)
    val parsedInitialDate = remember(initialOrder) {
        initialOrder?.deliveryDate?.let { dateStr ->
            try {
                if (dateStr.contains("T")) {
                    LocalDate.parse(dateStr.substringBefore("T"))
                } else {
                    LocalDate.parse(dateStr)
                }
            } catch (_: Exception) {
                null
            }
        } ?: LocalDate.now().plusDays(14)
    }
    var deliveryDate by rememberSaveable {
        mutableStateOf(parsedInitialDate)
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // Active status
    var active by rememberSaveable { mutableStateOf(initialOrder?.active ?: true) }

    // Additional options
    var assemblyPrice by rememberSaveable {
        mutableStateOf(initialOrder?.assemblyPrice?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }
    var assemblyRemarks by rememberSaveable { mutableStateOf(initialOrder?.assemblyRemarks.orEmpty()) }
    var transportPrice by rememberSaveable {
        mutableStateOf(initialOrder?.transportPrice?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }
    var transportRemarks by rememberSaveable { mutableStateOf(initialOrder?.transportRemarks.orEmpty()) }
    var carryProductsPrice by rememberSaveable {
        mutableStateOf(initialOrder?.carryProductsPrice?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }
    var carryProductsRemarks by rememberSaveable { mutableStateOf(initialOrder?.carryProductsRemarks.orEmpty()) }
    var discount by rememberSaveable {
        mutableStateOf(initialOrder?.discount?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }
    var discountRemarks by rememberSaveable { mutableStateOf(initialOrder?.discountRemarks.orEmpty()) }
    var deposit by rememberSaveable {
        mutableStateOf(initialOrder?.deposit?.takeIf { it > 0.0 }?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }.orEmpty())
    }
    var remarks by rememberSaveable { mutableStateOf(initialOrder?.remarks.orEmpty()) }

    val hasInitialAdditionalCosts = remember(initialOrder) {
        initialOrder != null && (
            initialOrder.assemblyPrice > 0.0 ||
                initialOrder.assemblyRemarks.isNotBlank() ||
                initialOrder.transportPrice > 0.0 ||
                initialOrder.transportRemarks.isNotBlank() ||
                initialOrder.carryProductsPrice > 0.0 ||
                initialOrder.carryProductsRemarks.isNotBlank() ||
                initialOrder.discount > 0.0 ||
                initialOrder.discountRemarks.isNotBlank() ||
                initialOrder.deposit > 0.0 ||
                initialOrder.remarks.isNotBlank()
        )
    }

    var showAdditionalCosts by rememberSaveable { mutableStateOf(hasInitialAdditionalCosts) }
    var showProductPicker by rememberSaveable { mutableStateOf(false) }

    // Selected products: Map of productId -> quantity
    val selectedItems = remember(initialOrder) {
        mutableStateMapOf<Long, Int>().apply {
            initialOrder?.items?.forEach { item ->
                item.product?.id?.let { pid ->
                    put(pid, item.quantity)
                }
            }
        }
    }

    // Validation & loading states
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }

    val productsById = remember(products) { products.associateBy { it.id } }

    // Live reactive computations: reading Compose states inside derivedStateOf
    val itemsSubtotal by remember {
        derivedStateOf {
            selectedItems.entries.sumOf { (id, qty) ->
                val price = productsById[id]?.price ?: 0.0
                price * qty
            }
        }
    }

    val parsedAssemblyPrice by remember { derivedStateOf { assemblyPrice.parseAmount() } }
    val parsedTransportPrice by remember { derivedStateOf { transportPrice.parseAmount() } }
    val parsedCarryPrice by remember { derivedStateOf { carryProductsPrice.parseAmount() } }
    val parsedDiscount by remember { derivedStateOf { discount.parseAmount() } }
    val parsedDeposit by remember { derivedStateOf { deposit.parseAmount() } }

    val servicesSubtotal by remember {
        derivedStateOf {
            parsedAssemblyPrice + parsedTransportPrice + parsedCarryPrice
        }
    }

    val subtotalBeforeDiscount by remember {
        derivedStateOf {
            itemsSubtotal + servicesSubtotal
        }
    }

    val totalOrderPrice by remember {
        derivedStateOf {
            (subtotalBeforeDiscount - parsedDiscount).coerceAtLeast(0.0)
        }
    }

    val remainingToPay by remember {
        derivedStateOf {
            (totalOrderPrice - parsedDeposit).coerceAtLeast(0.0)
        }
    }

    val isAddressValid = address.trim().isNotBlank()
    val isPhoneValid = phoneNumber.trim().isNotBlank()
    val isDeliveryDateValid = initialOrder != null || !deliveryDate.isBefore(LocalDate.now())
    val hasItems = selectedItems.isNotEmpty()

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.93f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = if (initialOrder == null) "Nowe zamówienie" else "Edycja zamówienia #${initialOrder.id}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (initialOrder == null) "Wypełnij dane klienta i dodaj meble" else "Zaktualizuj dane klienta lub pozycje zamówienia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij")
                    }
                }

                HorizontalDivider()

                // Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Error banner
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }

                    // Status Switch
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (active) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            },
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (active) "Status: Aktywne" else "Status: Nieaktywne",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = if (active) {
                                        "Zamówienie widoczne jako aktywne w realizacji"
                                    } else {
                                        "Zamówienie oznaczone jako zakończone / nieaktywne"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Switch(
                                checked = active,
                                onCheckedChange = { active = it },
                                enabled = !isSubmitting,
                            )
                        }
                    }

                    // Section 1: Wybrane produkty
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Produkty w zamówieniu *",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (hasItems) {
                                    Text(
                                        text = "${selectedItems.values.sum()} szt.",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }

                            if (attemptedSubmit && !hasItems) {
                                Text(
                                    text = "Wymagany jest przynajmniej jeden produkt w zamówieniu.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            if (selectedItems.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp),
                                        )
                                        .padding(18.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            modifier = Modifier.size(36.dp),
                                            tint = MaterialTheme.colorScheme.outline,
                                        )
                                        Text(
                                            text = "Brak wybranych mebli",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    selectedItems.entries.forEach { (id, qty) ->
                                        val product = productsById[id]
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                            ),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = product?.model ?: "Produkt #$id",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold,
                                                    )
                                                    Text(
                                                        text = "${money(product?.price ?: 0.0)} / szt.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    Text(
                                                        text = "Suma: ${money((product?.price ?: 0.0) * qty)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                    )
                                                }

                                                // Stepper +/-
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    IconButton(
                                                        onClick = {
                                                            if (qty > 1) {
                                                                selectedItems[id] = qty - 1
                                                            } else {
                                                                selectedItems.remove(id)
                                                            }
                                                        },
                                                        modifier = Modifier.size(32.dp),
                                                    ) {
                                                        Icon(
                                                            if (qty > 1) Icons.Default.Remove else Icons.Default.Delete,
                                                            contentDescription = "Zmniejsz ilość",
                                                            modifier = Modifier.size(18.dp),
                                                            tint = if (qty > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                                        )
                                                    }
                                                    Text(
                                                        text = qty.toString(),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp),
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            selectedItems[id] = qty + 1
                                                        },
                                                        modifier = Modifier.size(32.dp),
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Add,
                                                            contentDescription = "Zwiększ ilość",
                                                            modifier = Modifier.size(18.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            FilledTonalButton(
                                onClick = { showProductPicker = true },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Wybierz meble z katalogu")
                            }
                        }
                    }

                    // Section 2: Dane dostawy i klienta
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Dane dostawy i klienta",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            // Data dostawy
                            OutlinedTextField(
                                value = deliveryDate.format(dateFormatter),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Termin dostawy *") },
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = "Wybierz datę")
                                    }
                                },
                                isError = attemptedSubmit && !isDeliveryDateValid,
                                supportingText = if (attemptedSubmit && !isDeliveryDateValid) {
                                    { Text("Termin dostawy musi być datą dzisiejszą lub przyszłą.") }
                                } else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                            )

                            // Adres
                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Adres dostawy *") },
                                placeholder = { Text("np. ul. Marszałkowska 10/5, Warszawa") },
                                isError = attemptedSubmit && !isAddressValid,
                                supportingText = if (attemptedSubmit && !isAddressValid) {
                                    { Text("Adres jest wymagany.") }
                                } else null,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Telefon
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Telefon kontaktowy *") },
                                placeholder = { Text("np. 600 123 456") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                isError = attemptedSubmit && !isPhoneValid,
                                supportingText = if (attemptedSubmit && !isPhoneValid) {
                                    { Text("Numer telefonu jest wymagany.") }
                                } else null,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Czas realizacji
                            OutlinedTextField(
                                value = fulfillmentTime,
                                onValueChange = { fulfillmentTime = it.filter(Char::isDigit) },
                                label = { Text("Czas realizacji (dni)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Uwagi do kontaktu
                            OutlinedTextField(
                                value = contactRemarks,
                                onValueChange = { contactRemarks = it },
                                label = { Text("Uwagi do kontaktu") },
                                placeholder = { Text("np. Dzwonić po godz. 16:00") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // Section 3: Dodatkowe koszty, montaż, transport, rabat
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAdditionalCosts = !showAdditionalCosts },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = "Koszty dodatkowe i rabat",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = "Montaż, transport, wniesienie, rabat, zaliczka",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { showAdditionalCosts = !showAdditionalCosts }) {
                                    Icon(
                                        if (showAdditionalCosts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = showAdditionalCosts,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    // Montaż
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        OutlinedTextField(
                                            value = assemblyPrice,
                                            onValueChange = { assemblyPrice = it },
                                            label = { Text("Montaż (zł)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                        )
                                        OutlinedTextField(
                                            value = assemblyRemarks,
                                            onValueChange = { assemblyRemarks = it },
                                            label = { Text("Uwagi do montażu") },
                                            modifier = Modifier.weight(1.5f),
                                        )
                                    }

                                    // Transport
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        OutlinedTextField(
                                            value = transportPrice,
                                            onValueChange = { transportPrice = it },
                                            label = { Text("Transport (zł)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                        )
                                        OutlinedTextField(
                                            value = transportRemarks,
                                            onValueChange = { transportRemarks = it },
                                            label = { Text("Uwagi do transportu") },
                                            modifier = Modifier.weight(1.5f),
                                        )
                                    }

                                    // Wniesienie
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        OutlinedTextField(
                                            value = carryProductsPrice,
                                            onValueChange = { carryProductsPrice = it },
                                            label = { Text("Wniesienie (zł)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                        )
                                        OutlinedTextField(
                                            value = carryProductsRemarks,
                                            onValueChange = { carryProductsRemarks = it },
                                            label = { Text("Uwagi do wniesienia") },
                                            modifier = Modifier.weight(1.5f),
                                        )
                                    }

                                    // Rabat
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        OutlinedTextField(
                                            value = discount,
                                            onValueChange = { discount = it },
                                            label = { Text("Rabat (zł)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                        )
                                        OutlinedTextField(
                                            value = discountRemarks,
                                            onValueChange = { discountRemarks = it },
                                            label = { Text("Uwagi do rabatu") },
                                            modifier = Modifier.weight(1.5f),
                                        )
                                    }

                                    // Zaliczka
                                    OutlinedTextField(
                                        value = deposit,
                                        onValueChange = { deposit = it },
                                        label = { Text("Wpłacona zaliczka (zł)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                    // Uwagi ogólne
                                    OutlinedTextField(
                                        value = remarks,
                                        onValueChange = { remarks = it },
                                        label = { Text("Uwagi ogólne do zamówienia") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }

                    // Section 4: Podsumowanie kwotowe na żywo
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Podsumowanie zamówienia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 4.dp),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Wartość mebli:", style = MaterialTheme.typography.bodyMedium)
                                Text(money(itemsSubtotal), fontWeight = FontWeight.SemiBold)
                            }

                            if (servicesSubtotal > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Usługi dodatkowe:", style = MaterialTheme.typography.bodyMedium)
                                    Text("+${money(servicesSubtotal)}")
                                }
                            }

                            if (servicesSubtotal > 0.0 && parsedDiscount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Suma przed rabatem:", style = MaterialTheme.typography.bodyMedium)
                                    Text(money(subtotalBeforeDiscount))
                                }
                            }

                            if (parsedDiscount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Rabat:", style = MaterialTheme.typography.bodyMedium)
                                    Text("-${money(parsedDiscount)}", color = MaterialTheme.colorScheme.error)
                                }
                            }

                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                                modifier = Modifier.padding(vertical = 2.dp),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "Łączna wartość zamówienia:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    money(totalOrderPrice),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }

                            if (parsedDeposit > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Wpłacona zaliczka:", style = MaterialTheme.typography.bodyMedium)
                                    Text("-${money(parsedDeposit)}")
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        "Pozostało do zapłaty:",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        money(remainingToPay),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                    ) {
                        Text("Anuluj")
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = {
                            attemptedSubmit = true
                            if (!isAddressValid || !isPhoneValid || !isDeliveryDateValid || !hasItems) {
                                errorMessage = "Proszę poprawić błędy w formularzu i dodać produkty."
                                return@Button
                            }

                            errorMessage = null
                            isSubmitting = true

                            val request = CreateOrderRequest(
                                deliveryDate = "${deliveryDate}T12:00:00",
                                phoneNumber = phoneNumber.trim(),
                                fulfillmentTime = fulfillmentTime.toIntOrNull() ?: 14,
                                contactRemarks = contactRemarks.trim().takeIf { it.isNotBlank() },
                                address = address.trim(),
                                assemblyPrice = parsedAssemblyPrice,
                                assemblyRemarks = assemblyRemarks.trim().takeIf { it.isNotBlank() },
                                discount = parsedDiscount,
                                discountRemarks = discountRemarks.trim().takeIf { it.isNotBlank() },
                                transportPrice = parsedTransportPrice,
                                transportRemarks = transportRemarks.trim().takeIf { it.isNotBlank() },
                                remarks = remarks.trim().takeIf { it.isNotBlank() },
                                carryProductsPrice = parsedCarryPrice,
                                carryProductsRemarks = carryProductsRemarks.trim().takeIf { it.isNotBlank() },
                                deposit = parsedDeposit,
                                active = active,
                                orderItemRequests = selectedItems.map { (id, qty) ->
                                    OrderItemRequest(productId = id, quantity = qty)
                                },
                            )

                            onSave(
                                request,
                                /* onSuccess = */ {
                                    isSubmitting = false
                                },
                                /* onError = */ { err ->
                                    isSubmitting = false
                                    errorMessage = err
                                },
                            )
                        },
                        enabled = !isSubmitting,
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (initialOrder == null) "Tworzenie..." else "Zapisywanie...")
                        } else {
                            Text(if (initialOrder == null) "Utwórz zamówienie" else "Zapisz zmiany")
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val initialMillis = deliveryDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            deliveryDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("Wybierz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Product Picker Dialog
    if (showProductPicker) {
        ProductPickerDialog(
            products = products,
            selectedItemCounts = selectedItems,
            onAddProduct = { product ->
                val current = selectedItems[product.id] ?: 0
                selectedItems[product.id] = current + 1
            },
            onDismiss = { showProductPicker = false },
        )
    }
}

@Composable
fun CreateOrderDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onSave: (
        request: CreateOrderRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) -> Unit,
) {
    OrderFormDialog(
        products = products,
        initialOrder = null,
        onDismiss = onDismiss,
        onSave = onSave,
    )
}

@Composable
private fun ProductPickerDialog(
    products: List<Product>,
    selectedItemCounts: Map<Long, Int>,
    onAddProduct: (Product) -> Unit,
    onDismiss: () -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            val q = searchQuery.trim().lowercase()
            products.filter { p ->
                p.model.lowercase().contains(q) ||
                    p.type.lowercase().contains(q) ||
                    p.material.lowercase().contains(q) ||
                    p.colour.lowercase().contains(q)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Wybierz produkt z katalogu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij")
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Szukaj modelu, typu, materiału...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(Modifier.height(12.dp))

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Katalog produktów jest pusty" else "Nie znaleziono produktów dla „$searchQuery”",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filteredProducts, key = Product::id) { product ->
                            val count = selectedItemCounts[product.id] ?: 0
                            Card(
                                onClick = { onAddProduct(product) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (count > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainer,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = product.model,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = "${product.type} • ${product.material} • ${product.colour}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = "${product.length.toInt()} × ${product.depth.toInt()} × ${product.height.toInt()} cm",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = money(product.price),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        FilledTonalButton(
                                            onClick = { onAddProduct(product) },
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (count > 0) "Dodaj kolejny ($count)" else "Wybierz")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gotowe")
                }
            }
        }
    }
}
