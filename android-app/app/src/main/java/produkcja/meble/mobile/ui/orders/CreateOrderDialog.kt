package produkcja.meble.mobile.ui.orders

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.filled.Schedule
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import produkcja.meble.mobile.ui.theme.orderSectionStyle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import produkcja.meble.mobile.data.OrderStatus
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import produkcja.meble.mobile.data.CreateOrderRequest
import produkcja.meble.mobile.data.CreateProductRequest
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.FurnitureType
import produkcja.meble.mobile.data.OrderItemRequest
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.ui.common.money
import produkcja.meble.mobile.ui.common.resolveImageUrl
import produkcja.meble.mobile.ui.products.ProductFormDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

private object PostalCodeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(5)
        val out = when {
            trimmed.length <= 2 -> trimmed
            else -> trimmed.substring(0, 2) + "-" + trimmed.substring(2)
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, trimmed.length)
                return when {
                    trimmed.length <= 2 -> clamped
                    clamped < 2 -> clamped
                    else -> clamped + 1
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    trimmed.length <= 2 -> offset.coerceIn(0, trimmed.length)
                    offset <= 2 -> offset.coerceIn(0, trimmed.length)
                    else -> (offset - 1).coerceIn(0, trimmed.length)
                }
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

private data class ParsedAddress(
    val postalCode: String,
    val city: String,
    val street: String,
    val houseNumber: String,
)

private fun parseAddress(raw: String?): ParsedAddress {
    if (raw.isNullOrBlank()) {
        return ParsedAddress("", "", "", "")
    }
    val postalRegex = Regex("""\b(\d{2}-\d{3})\b""")
    val match = postalRegex.find(raw)
    var postalCode = ""
    var city = ""
    var streetPart = ""

    if (match != null) {
        postalCode = match.groupValues[1]
        val beforePostal = raw.substring(0, match.range.first).trim().trimEnd(',', ' ')
        val afterPostal = raw.substring(match.range.last + 1).trim().trimStart(',', ' ')
        streetPart = beforePostal
        city = afterPostal
    } else {
        val parts = raw.split(",").map { it.trim() }
        if (parts.size >= 2) {
            streetPart = parts[0]
            city = parts.drop(1).joinToString(", ")
        } else {
            streetPart = raw.trim()
        }
    }

    val numberRegex = Regex("""\s+(\d+[\w\s/-]*)$""")
    val numMatch = numberRegex.find(streetPart)
    var street = streetPart
    var houseNumber = ""
    if (numMatch != null) {
        houseNumber = numMatch.groupValues[1].trim()
        street = streetPart.substring(0, numMatch.range.first).trim()
    }

    return ParsedAddress(
        postalCode = postalCode,
        city = city,
        street = street,
        houseNumber = houseNumber,
    )
}

private fun String.parseAmount(): Double {
    val clean = this.replace(',', '.').replace(" ", "").trim()
    return clean.toDoubleOrNull() ?: 0.0
}

data class FormOrderItem(
    val tempId: String = UUID.randomUUID().toString(),
    val baseProduct: Product,
    val quantity: Int = 1,
    val customPrice: Double? = null,
    val customLength: Double? = null,
    val customDepth: Double? = null,
    val customHeight: Double? = null,
    val customColour: String = "",
    val customFabric: String = "",
    val customMaterial: String = "",
    val customEquipment: String = "",
    val customRemarks: String = "",
) {
    val effectivePrice: Double get() = customPrice ?: baseProduct.price
    val effectiveLength: Double get() = customLength ?: baseProduct.length
    val effectiveDepth: Double get() = customDepth ?: baseProduct.depth
    val effectiveHeight: Double get() = customHeight ?: baseProduct.height
    val effectiveColour: String get() = customColour.ifBlank { baseProduct.colour }
    val effectiveFabric: String get() = customFabric.ifBlank { baseProduct.fabric }
    val effectiveMaterial: String get() = customMaterial.ifBlank { baseProduct.material }
    val effectiveEquipment: String get() = customEquipment.ifBlank { baseProduct.equipment }
    val effectiveRemarks: String get() = customRemarks.ifBlank { baseProduct.remarks }

    val isCustomized: Boolean
        get() = (customPrice != null && customPrice != baseProduct.price) ||
                (customLength != null && customLength != baseProduct.length) ||
                (customDepth != null && customDepth != baseProduct.depth) ||
                (customHeight != null && customHeight != baseProduct.height) ||
                (customColour.isNotBlank() && customColour != baseProduct.colour) ||
                (customFabric.isNotBlank() && customFabric != baseProduct.fabric) ||
                (customMaterial.isNotBlank() && customMaterial != baseProduct.material) ||
                (customEquipment.isNotBlank() && customEquipment != baseProduct.equipment) ||
                (customRemarks.isNotBlank() && customRemarks != baseProduct.remarks)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFormDialog(
    products: List<Product>,
    initialOrder: FurnitureOrder? = null,
    onDismiss: () -> Unit,
    onCreateProduct: ((
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        mimeType: String?,
        additionalImagesBytes: List<ByteArray>,
        onSuccess: (Product) -> Unit,
        onError: (String) -> Unit,
    ) -> Unit)? = null,
    onSave: (
        request: CreateOrderRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) -> Unit,
) {
    val initialParsedAddress = remember(initialOrder) { parseAddress(initialOrder?.address) }
    var street by rememberSaveable { mutableStateOf(initialParsedAddress.street) }
    var houseNumber by rememberSaveable { mutableStateOf(initialParsedAddress.houseNumber) }
    var postalCode by rememberSaveable { mutableStateOf(initialParsedAddress.postalCode.filter { it.isDigit() }.take(5)) }
    var city by rememberSaveable { mutableStateOf(initialParsedAddress.city) }
    var phoneNumber by rememberSaveable { mutableStateOf(initialOrder?.phoneNumber.orEmpty()) }
    var fulfillmentTime by rememberSaveable {
        mutableStateOf(initialOrder?.fulfillmentTime?.takeIf { it > 0 }?.toString().orEmpty())
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
        } ?: LocalDate.now().plusWeeks(2)
    }
    var deliveryDate by rememberSaveable {
        mutableStateOf(parsedInitialDate)
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // Order status
    var orderStatus by rememberSaveable { mutableStateOf(initialOrder?.orderStatus ?: OrderStatus.ACTIVE) }

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
    var showCreateProductDialog by rememberSaveable { mutableStateOf(false) }
    var itemToCustomize by remember { mutableStateOf<FormOrderItem?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var formFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var clearFeedbackJob by remember { mutableStateOf<Job?>(null) }

    val showFeedback: (String) -> Unit = { msg ->
        formFeedbackMessage = msg
        clearFeedbackJob?.cancel()
        clearFeedbackJob = coroutineScope.launch {
            delay(3000)
            if (formFeedbackMessage == msg) {
                formFeedbackMessage = null
            }
        }
    }

    // Selected order items list with support for customized parameters
    val orderItems = remember(initialOrder) {
        mutableStateListOf<FormOrderItem>().apply {
            initialOrder?.items?.forEach { item ->
                val prod = item.product ?: return@forEach
                add(
                    FormOrderItem(
                        baseProduct = prod,
                        quantity = item.quantity,
                        customPrice = item.price.takeIf { it > 0.0 && it != prod.price },
                        customLength = item.length?.takeIf { it != prod.length },
                        customDepth = item.depth?.takeIf { it != prod.depth },
                        customHeight = item.height?.takeIf { it != prod.height },
                        customColour = item.colour.takeIf { it != prod.colour }.orEmpty(),
                        customFabric = item.fabric.takeIf { it != prod.fabric }.orEmpty(),
                        customMaterial = item.material.takeIf { it != prod.material }.orEmpty(),
                        customEquipment = item.equipment.takeIf { it != prod.equipment }.orEmpty(),
                        customRemarks = item.remarks.takeIf { it != prod.remarks }.orEmpty(),
                    )
                )
            }
        }
    }

    // Validation & loading states
    var isSubmitting by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = !isSubmitting) {
        when {
            showDatePicker -> showDatePicker = false
            showProductPicker -> showProductPicker = false
            showCreateProductDialog -> showCreateProductDialog = false
            itemToCustomize != null -> itemToCustomize = null
            else -> onDismiss()
        }
    }

    // Live reactive computations: reading Compose states inside derivedStateOf
    val maxLeadTimeWeeks by remember {
        derivedStateOf {
            orderItems.mapNotNull { it.baseProduct.leadTimeWeeks?.takeIf { w -> w > 0 } }.maxOrNull()
        }
    }

    var userEditedFulfillmentTime by rememberSaveable { mutableStateOf(initialOrder != null) }
    var userEditedDeliveryDate by rememberSaveable { mutableStateOf(initialOrder != null) }

    LaunchedEffect(maxLeadTimeWeeks) {
        val maxWeeks = maxLeadTimeWeeks
        if (maxWeeks != null) {
            if (!userEditedFulfillmentTime) {
                fulfillmentTime = maxWeeks.toString()
            }
            if (!userEditedDeliveryDate) {
                deliveryDate = LocalDate.now().plusWeeks(maxWeeks.toLong())
            }
        }
    }

    val itemsSubtotal by remember {
        derivedStateOf {
            orderItems.sumOf { it.effectivePrice * it.quantity }
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

    val isPostalCodeValid = postalCode.length == 5
    val isCityValid = city.trim().isNotBlank()
    val isStreetValid = street.trim().isNotBlank()
    val isHouseNumberValid = houseNumber.trim().isNotBlank()
    val isAddressValid = isPostalCodeValid && isCityValid && isStreetValid && isHouseNumberValid
    val isPhoneValid = phoneNumber.trim().isNotBlank()
    val isDeliveryDateValid = initialOrder != null || !deliveryDate.isBefore(LocalDate.now())
    val hasItems = orderItems.isNotEmpty()

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
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

                    // Status Selection
                    val isDark = isSystemInDarkTheme()
                    val orderStyle = orderSectionStyle(isDark)

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Status zamówienia",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = orderStatus.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (orderStatus) {
                                        OrderStatus.ACTIVE -> orderStyle.primary
                                        OrderStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                                        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                    },
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                OrderStatus.entries.forEach { statusOption ->
                                    val isSelected = orderStatus == statusOption
                                    val (activeColor, activeContainer) = when (statusOption) {
                                        OrderStatus.ACTIVE -> Pair(
                                            orderStyle.primary,
                                            orderStyle.container.copy(alpha = 0.7f),
                                        )
                                        OrderStatus.COMPLETED -> Pair(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        )
                                        OrderStatus.CANCELLED -> Pair(
                                            MaterialTheme.colorScheme.error,
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                        )
                                    }
                                    val icon = when (statusOption) {
                                        OrderStatus.ACTIVE -> Icons.Default.PlayCircleOutline
                                        OrderStatus.COMPLETED -> Icons.Default.CheckCircle
                                        OrderStatus.CANCELLED -> Icons.Default.Cancel
                                    }

                                    Surface(
                                        selected = isSelected,
                                        onClick = { if (!isSubmitting) orderStatus = statusOption },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) activeContainer else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(68.dp),
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 4.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(22.dp),
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = statusOption.displayName,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            }
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
                                        text = "${orderItems.sumOf { it.quantity }} szt.",
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

                            AnimatedVisibility(
                                visible = formFeedbackMessage != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Text(
                                            text = formFeedbackMessage.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }
                                }
                            }

                            if (orderItems.isEmpty()) {
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
                                    orderItems.forEachIndexed { index, item ->
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                            ),
                                            border = if (item.isCustomized) {
                                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                            } else null,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        ) {
                                                            Text(
                                                                text = item.baseProduct.model,
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = FontWeight.Bold,
                                                            )
                                                            if (item.isCustomized) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                                ) {
                                                                    Text(
                                                                        text = "Dostosowany",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Text(
                                                            text = "${money(item.effectivePrice)} / szt.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        )
                                                        Text(
                                                            text = "Suma: ${money(item.effectivePrice * item.quantity)}",
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
                                                                if (item.quantity > 1) {
                                                                    orderItems[index] = item.copy(quantity = item.quantity - 1)
                                                                    showFeedback("Zmniejszono ilość „${item.baseProduct.model}” do ${item.quantity - 1} szt.")
                                                                } else {
                                                                    val removedModel = item.baseProduct.model
                                                                    orderItems.removeAt(index)
                                                                    showFeedback("Usunięto mebel „$removedModel” z zamówienia")
                                                                }
                                                            },
                                                            modifier = Modifier.size(32.dp),
                                                        ) {
                                                            Icon(
                                                                if (item.quantity > 1) Icons.Default.Remove else Icons.Default.Delete,
                                                                contentDescription = "Zmniejsz ilość",
                                                                modifier = Modifier.size(18.dp),
                                                                tint = if (item.quantity > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                                            )
                                                        }
                                                        Text(
                                                            text = item.quantity.toString(),
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp),
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                orderItems[index] = item.copy(quantity = item.quantity + 1)
                                                                showFeedback("Zwiększono ilość „${item.baseProduct.model}” do ${item.quantity + 1} szt.")
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

                                                // Extra customization badges preview
                                                if (item.isCustomized) {
                                                    Spacer(Modifier.height(6.dp))
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f))
                                                            .padding(8.dp),
                                                    ) {
                                                        val dims = "${item.effectiveLength.toPlainString()} × ${item.effectiveDepth.toPlainString()} × ${item.effectiveHeight.toPlainString()} cm"
                                                        Text("Wymiary: $dims", style = MaterialTheme.typography.bodySmall)
                                                        if (item.effectiveFabric.isNotBlank()) {
                                                            Text("Tkanina: ${item.effectiveFabric}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                        if (item.effectiveColour.isNotBlank()) {
                                                            Text("Kolor: ${item.effectiveColour}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                        if (item.effectiveMaterial.isNotBlank()) {
                                                            Text("Materiał: ${item.effectiveMaterial}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                        if (item.effectiveEquipment.isNotBlank()) {
                                                            Text("Wyposażenie: ${item.effectiveEquipment}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                        if (item.effectiveRemarks.isNotBlank()) {
                                                            Text("Uwagi: ${item.effectiveRemarks}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                    }
                                                }

                                                // Customize button
                                                Spacer(Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End,
                                                ) {
                                                    FilledTonalButton(
                                                        onClick = { itemToCustomize = item },
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(34.dp),
                                                    ) {
                                                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = if (item.isCustomized) "Edytuj parametry / cenę" else "Dostosuj mebel / cenę",
                                                            style = MaterialTheme.typography.labelMedium,
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Action buttons: Pick from catalog OR Add new to catalog & order
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                FilledTonalButton(
                                    onClick = { showProductPicker = true },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Z katalogu")
                                }

                                if (onCreateProduct != null) {
                                    OutlinedButton(
                                        onClick = { showCreateProductDialog = true },
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("+ Nowy")
                                    }
                                }
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
                                    { Text("Termin dostawy nie może być z przeszłości") }
                                } else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                            )

                            // Adres dostawy rozbity na 4 pola wymagane
                            Text(
                                text = "Adres dostawy *",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OutlinedTextField(
                                    value = street,
                                    onValueChange = { street = it },
                                    label = { Text("Ulica *") },
                                    placeholder = { Text("np. Marszałkowska") },
                                    isError = attemptedSubmit && !isStreetValid,
                                    supportingText = if (attemptedSubmit && !isStreetValid) {
                                        { Text("Wymagane") }
                                    } else null,
                                    modifier = Modifier.weight(2f),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = houseNumber,
                                    onValueChange = { houseNumber = it },
                                    label = { Text("Nr *") },
                                    placeholder = { Text("np. 10/4") },
                                    isError = attemptedSubmit && !isHouseNumberValid,
                                    supportingText = if (attemptedSubmit && !isHouseNumberValid) {
                                        { Text("Wymagane") }
                                    } else null,
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true,
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                OutlinedTextField(
                                    value = postalCode,
                                    onValueChange = { input ->
                                        postalCode = input.filter { it.isDigit() }.take(5)
                                    },
                                    visualTransformation = PostalCodeVisualTransformation,
                                    label = { Text("Kod pocztowy *") },
                                    placeholder = { Text("00-000") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = attemptedSubmit && !isPostalCodeValid,
                                    supportingText = if (attemptedSubmit && !isPostalCodeValid) {
                                        { Text("Format: 00-000") }
                                    } else null,
                                    modifier = Modifier.weight(1.3f),
                                    singleLine = true,
                                )
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("Miejscowość *") },
                                    placeholder = { Text("np. Warszawa") },
                                    isError = attemptedSubmit && !isCityValid,
                                    supportingText = if (attemptedSubmit && !isCityValid) {
                                        { Text("Wymagane") }
                                    } else null,
                                    modifier = Modifier.weight(2f),
                                    singleLine = true,
                                )
                            }

                            // Telefon
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Numer telefonu *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                isError = attemptedSubmit && !isPhoneValid,
                                supportingText = if (attemptedSubmit && !isPhoneValid) {
                                    { Text("Numer telefonu jest wymagany") }
                                } else null,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Czas realizacji
                            OutlinedTextField(
                                value = fulfillmentTime,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) {
                                        fulfillmentTime = input
                                        userEditedFulfillmentTime = true
                                        val weeks = input.toIntOrNull()
                                        if (!userEditedDeliveryDate && weeks != null && weeks > 0) {
                                            deliveryDate = LocalDate.now().plusWeeks(weeks.toLong())
                                        }
                                    }
                                },
                                label = { Text("Czas realizacji (tyg.)") },
                                supportingText = {
                                    if (maxLeadTimeWeeks != null) {
                                        Text("Najdłuższy czas mebli w zamówieniu: $maxLeadTimeWeeks tyg.")
                                    } else {
                                        Text("Szacowany czas realizacji w tygodniach")
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Uwagi do kontaktu
                            OutlinedTextField(
                                value = contactRemarks,
                                onValueChange = { contactRemarks = it },
                                label = { Text("Uwagi do kontaktu z klientem") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    // Section 3: Dodatkowe koszty i rabaty (rozwijana sekcja)
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
                                        text = "Montaż, transport, rabaty i zaliczka",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = if (showAdditionalCosts) "Kliknij, aby zwinąć" else "Kliknij, aby skonfigurować koszty dodatkowe",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { showAdditionalCosts = !showAdditionalCosts }) {
                                    Icon(
                                        imageVector = if (showAdditionalCosts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
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
                                    HorizontalDivider()

                                    // Montaż
                                    Text("Montaż", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = assemblyPrice,
                                            onValueChange = { assemblyPrice = it },
                                            label = { Text("Cena montażu (zł)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(0.4f),
                                        )
                                        OutlinedTextField(
                                            value = assemblyRemarks,
                                            onValueChange = { assemblyRemarks = it },
                                            label = { Text("Uwagi do montażu") },
                                            modifier = Modifier.weight(0.6f),
                                        )
                                    }

                                    // Transport
                                    Text("Transport", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = transportPrice,
                                            onValueChange = { transportPrice = it },
                                            label = { Text("Cena transportu (zł)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(0.4f),
                                        )
                                        OutlinedTextField(
                                            value = transportRemarks,
                                            onValueChange = { transportRemarks = it },
                                            label = { Text("Uwagi do transportu") },
                                            modifier = Modifier.weight(0.6f),
                                        )
                                    }

                                    // Wniesienie
                                    Text("Wniesienie", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = carryProductsPrice,
                                            onValueChange = { carryProductsPrice = it },
                                            label = { Text("Cena wniesienia (zł)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(0.4f),
                                        )
                                        OutlinedTextField(
                                            value = carryProductsRemarks,
                                            onValueChange = { carryProductsRemarks = it },
                                            label = { Text("Uwagi do wniesienia") },
                                            modifier = Modifier.weight(0.6f),
                                        )
                                    }

                                    // Rabat
                                    Text("Rabat", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = discount,
                                            onValueChange = { discount = it },
                                            label = { Text("Kwota rabatu (zł)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(0.4f),
                                        )
                                        OutlinedTextField(
                                            value = discountRemarks,
                                            onValueChange = { discountRemarks = it },
                                            label = { Text("Powód / uwagi do rabatu") },
                                            modifier = Modifier.weight(0.6f),
                                        )
                                    }

                                    // Zaliczka
                                    Text("Zaliczka", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = deposit,
                                        onValueChange = { deposit = it },
                                        label = { Text("Wpłacona zaliczka (zł)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth(),
                                    )

                                    // Uwagi ogólne
                                    Text("Uwagi ogólne", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    OutlinedTextField(
                                        value = remarks,
                                        onValueChange = { remarks = it },
                                        label = { Text("Ogólne uwagi do zamówienia") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    }

                    // Section 4: Podsumowanie finansowe na żywo
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                "Podsumowanie finansowe",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Wartość mebli:", style = MaterialTheme.typography.bodyMedium)
                                Text(money(itemsSubtotal))
                            }

                            if (servicesSubtotal > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Usługi dodatkowe:", style = MaterialTheme.typography.bodyMedium)
                                    Text("+${money(servicesSubtotal)}")
                                }
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

                            val formattedPostalCode = if (postalCode.length > 2) "${postalCode.substring(0, 2)}-${postalCode.substring(2)}" else postalCode
                            val combinedAddress = "${street.trim()} ${houseNumber.trim()}, $formattedPostalCode ${city.trim()}"
                            val request = CreateOrderRequest(
                                deliveryDate = "${deliveryDate}T12:00:00",
                                phoneNumber = phoneNumber.trim(),
                                fulfillmentTime = fulfillmentTime.toIntOrNull() ?: maxLeadTimeWeeks ?: 2,
                                contactRemarks = contactRemarks.trim().takeIf { it.isNotBlank() },
                                address = combinedAddress,
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
                                active = (orderStatus == OrderStatus.ACTIVE),
                                status = orderStatus.name,
                                orderItemRequests = orderItems.map { item ->
                                    OrderItemRequest(
                                        productId = item.baseProduct.id,
                                        quantity = item.quantity,
                                        price = item.effectivePrice,
                                        length = item.effectiveLength,
                                        depth = item.effectiveDepth,
                                        height = item.effectiveHeight,
                                        colour = item.effectiveColour.takeIf { it.isNotBlank() },
                                        material = item.effectiveMaterial.takeIf { it.isNotBlank() },
                                        fabric = item.effectiveFabric.takeIf { it.isNotBlank() },
                                        equipment = item.effectiveEquipment.takeIf { it.isNotBlank() },
                                        remarks = item.effectiveRemarks.takeIf { it.isNotBlank() },
                                    )
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
                            userEditedDeliveryDate = true
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

    // Product Picker Dialog (Select existing product from catalog)
    if (showProductPicker) {
        ProductPickerDialog(
            products = products,
            orderItems = orderItems,
            onAddProduct = { product ->
                val existingIndex = orderItems.indexOfFirst { it.baseProduct.id == product.id && !it.isCustomized }
                if (existingIndex >= 0) {
                    val existing = orderItems[existingIndex]
                    orderItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                } else {
                    orderItems.add(FormOrderItem(baseProduct = product, quantity = 1))
                }
                showFeedback("Dodano mebel „${product.model}” do zamówienia")
            },
            onDismiss = { showProductPicker = false },
        )
    }

    // Create New Product On-The-Fly Dialog
    if (showCreateProductDialog && onCreateProduct != null) {
        ProductFormDialog(
            initialProduct = null,
            existingProducts = products,
            onDismiss = { showCreateProductDialog = false },
            onSave = { req, imgBytes, fileName, mime, extraBytes, onSucc, onErr ->
                onCreateProduct(
                    req,
                    imgBytes,
                    fileName,
                    mime,
                    extraBytes,
                    /* onSuccess = */ { createdProduct ->
                        // Automatically add the new product to order items!
                        orderItems.add(FormOrderItem(baseProduct = createdProduct, quantity = 1))
                        showCreateProductDialog = false
                        showFeedback("Utworzono i dodano „${createdProduct.model}” do zamówienia")
                        onSucc("Dodano mebel \"${createdProduct.model}\" do bazy i zamówienia.")
                    },
                    /* onError = */ { err ->
                        onErr(err)
                    },
                )
            },
        )
    }

    // Customize Item Dialog
    itemToCustomize?.let { item ->
        CustomizeOrderItemDialog(
            item = item,
            onCreateProduct = onCreateProduct,
            onDismiss = { itemToCustomize = null },
            onApply = { updatedItem ->
                val idx = orderItems.indexOfFirst { it.tempId == item.tempId }
                if (idx >= 0) {
                    orderItems[idx] = updatedItem
                }
                itemToCustomize = null
            },
        )
    }
}

@Composable
fun CreateOrderDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onCreateProduct: ((
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        mimeType: String?,
        additionalImagesBytes: List<ByteArray>,
        onSuccess: (Product) -> Unit,
        onError: (String) -> Unit,
    ) -> Unit)? = null,
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
        onCreateProduct = onCreateProduct,
        onSave = onSave,
    )
}

@Composable
private fun CustomizeOrderItemDialog(
    item: FormOrderItem,
    onCreateProduct: ((
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        mimeType: String?,
        additionalImagesBytes: List<ByteArray>,
        onSuccess: (Product) -> Unit,
        onError: (String) -> Unit,
    ) -> Unit)? = null,
    onDismiss: () -> Unit,
    onApply: (FormOrderItem) -> Unit,
) {
    var priceText by rememberSaveable {
        mutableStateOf(item.effectivePrice.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() })
    }
    var lengthText by rememberSaveable {
        mutableStateOf(item.effectiveLength.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() })
    }
    var depthText by rememberSaveable {
        mutableStateOf(item.effectiveDepth.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() })
    }
    var heightText by rememberSaveable {
        mutableStateOf(item.effectiveHeight.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() })
    }
    var colour by rememberSaveable { mutableStateOf(item.effectiveColour) }
    var fabric by rememberSaveable { mutableStateOf(item.effectiveFabric) }
    var material by rememberSaveable { mutableStateOf(item.effectiveMaterial) }
    var equipment by rememberSaveable { mutableStateOf(item.effectiveEquipment) }
    var remarks by rememberSaveable { mutableStateOf(item.effectiveRemarks) }

    var isSavingToCatalog by rememberSaveable { mutableStateOf(false) }
    var catalogSaveMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var catalogSaveError by rememberSaveable { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Dostosuj mebel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = item.baseProduct.model,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij")
                    }
                }

                HorizontalDivider()

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Catalog base info card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val context = LocalContext.current
                            val resolvedImage = resolveImageUrl(item.baseProduct.imageUrl)
                            if (resolvedImage != null) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(resolvedImage)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Wartości katalogowe:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "Cena: ${money(item.baseProduct.price)} | Wymiary: ${item.baseProduct.length.toPlainString()} × ${item.baseProduct.depth.toPlainString()} × ${item.baseProduct.height.toPlainString()} cm",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                )
                                if (item.baseProduct.fabric.isNotBlank() || item.baseProduct.material.isNotBlank()) {
                                    Text(
                                        text = listOfNotNull(
                                            item.baseProduct.fabric.takeIf { it.isNotBlank() }?.let { "Tkanina: $it" },
                                            item.baseProduct.material.takeIf { it.isNotBlank() }?.let { "Materiał: $it" },
                                        ).joinToString(" | "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }

                    // Cena jednostkowa dla zamówienia
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Cena jednostkowa mebla (zł) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        supportingText = { Text("Możesz zmienić cenę dla tego konkretnego zamówienia") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Wymiary mebla
                    Text(
                        text = "Wymiary (dł. × gł./szer. × wys. w cm)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = lengthText,
                            onValueChange = { lengthText = it },
                            label = { Text("Długość") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = depthText,
                            onValueChange = { depthText = it },
                            label = { Text("Gł./Szer.") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("Wysokość") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    // Tkanina
                    OutlinedTextField(
                        value = fabric,
                        onValueChange = { fabric = it },
                        label = { Text("Tkanina / Obicie") },
                        supportingText = { Text("np. Monolith 77, Velvet, Skóra ekologiczna") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Kolor
                    OutlinedTextField(
                        value = colour,
                        onValueChange = { colour = it },
                        label = { Text("Kolorystyka") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Materiał
                    OutlinedTextField(
                        value = material,
                        onValueChange = { material = it },
                        label = { Text("Materiał wykonania") },
                        supportingText = { Text("np. Drewno dębowe, Płyta laminowana, Metal") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Wyposażenie
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("Wyposażenie dodatkowe") },
                        supportingText = { Text("np. Pojemnik na pościel, automat DL") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Uwagi do mebla
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Indywidualne uwagi do mebla") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Reset button to restore defaults from base product
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                priceText = item.baseProduct.price.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                                lengthText = item.baseProduct.length.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                                depthText = item.baseProduct.depth.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                                heightText = item.baseProduct.height.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }
                                colour = item.baseProduct.colour
                                fabric = item.baseProduct.fabric
                                material = item.baseProduct.material
                                equipment = item.baseProduct.equipment
                                remarks = item.baseProduct.remarks
                            },
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Przywróć katalogowe")
                        }
                    }

                    // Save as new catalog product option
                    if (onCreateProduct != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Zapis do bazy katalogowej",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Chcesz zapisać ten zmodyfikowany wariant mebla również jako stałą nową pozycję w katalogu produktów?",
                                    style = MaterialTheme.typography.bodySmall,
                                )

                                if (catalogSaveMessage != null) {
                                    Text(
                                        text = catalogSaveMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                if (catalogSaveError != null) {
                                    Text(
                                        text = catalogSaveError.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }

                                FilledTonalButton(
                                    onClick = {
                                        isSavingToCatalog = true
                                        catalogSaveMessage = null
                                        catalogSaveError = null

                                        val newModelName = "${item.baseProduct.model} (${fabric.ifBlank { colour.ifBlank { "Wariant" } }})"
                                        val req = CreateProductRequest(
                                            price = priceText.parseAmount(),
                                            length = lengthText.parseAmount(),
                                            depth = depthText.parseAmount(),
                                            height = heightText.parseAmount(),
                                            colour = colour.trim(),
                                            model = newModelName,
                                            type = item.baseProduct.type,
                                            material = material.trim(),
                                            fabric = fabric.trim(),
                                            remarks = remarks.trim(),
                                            equipment = equipment.trim(),
                                            imageUrl = item.baseProduct.imageUrl,
                                            additionalImages = item.baseProduct.additionalImages,
                                        )

                                        onCreateProduct(
                                            req,
                                            null,
                                            null,
                                            null,
                                            emptyList(),
                                            /* onSuccess = */ { createdProd ->
                                                isSavingToCatalog = false
                                                catalogSaveMessage = "Zapisano w bazie: \"${createdProd.model}\""
                                                // Update current item to point to the new base product
                                                onApply(
                                                    item.copy(
                                                        baseProduct = createdProd,
                                                        customPrice = null,
                                                        customLength = null,
                                                        customDepth = null,
                                                        customHeight = null,
                                                        customColour = "",
                                                        customFabric = "",
                                                        customMaterial = "",
                                                        customEquipment = "",
                                                        customRemarks = "",
                                                    )
                                                )
                                            },
                                            /* onError = */ { err ->
                                                isSavingToCatalog = false
                                                catalogSaveError = err
                                            },
                                        )
                                    },
                                    enabled = !isSavingToCatalog,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    if (isSavingToCatalog) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Zapisywanie...")
                                    } else {
                                        Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Zapisz jako nowy produkt w katalogu")
                                    }
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
                    OutlinedButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsedPrice = priceText.parseAmount()
                            val parsedLen = lengthText.parseAmount()
                            val parsedDep = depthText.parseAmount()
                            val parsedHei = heightText.parseAmount()

                            val updatedItem = item.copy(
                                customPrice = parsedPrice.takeIf { it > 0.0 && it != item.baseProduct.price },
                                customLength = parsedLen.takeIf { it > 0.0 && it != item.baseProduct.length },
                                customDepth = parsedDep.takeIf { it > 0.0 && it != item.baseProduct.depth },
                                customHeight = parsedHei.takeIf { it > 0.0 && it != item.baseProduct.height },
                                customColour = colour.trim().takeIf { it != item.baseProduct.colour }.orEmpty(),
                                customFabric = fabric.trim().takeIf { it != item.baseProduct.fabric }.orEmpty(),
                                customMaterial = material.trim().takeIf { it != item.baseProduct.material }.orEmpty(),
                                customEquipment = equipment.trim().takeIf { it != item.baseProduct.equipment }.orEmpty(),
                                customRemarks = remarks.trim().takeIf { it != item.baseProduct.remarks }.orEmpty(),
                            )
                            onApply(updatedItem)
                        },
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Zastosuj dla zamówienia")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductPickerDialog(
    products: List<Product>,
    orderItems: List<FormOrderItem>,
    onAddProduct: (Product) -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler { onDismiss() }

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
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
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
                    Column {
                        Text(
                            text = "Wybierz meble z katalogu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        val totalCount = orderItems.sumOf { it.quantity }
                        Text(
                            text = if (totalCount > 0) "W zamówieniu: $totalCount szt." else "Brak mebli w zamówieniu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij")
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Szukaj modelu, typu, koloru...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(Modifier.height(12.dp))

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Brak produktów w katalogu" else "Nie znaleziono mebli",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            val currentCountInOrder = orderItems.filter { it.baseProduct.id == product.id }.sumOf { it.quantity }

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                                    val context = LocalContext.current
                                    val resolvedImage = resolveImageUrl(product.imageUrl)
                                    if (resolvedImage != null) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(resolvedImage)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                        )
                                        Spacer(Modifier.width(10.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Text(
                                                text = product.model,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                            if (currentCountInOrder > 0) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(4.dp),
                                                ) {
                                                    Text(
                                                        text = "W zamówieniu: $currentCountInOrder szt.",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${FurnitureType.displayLabel(product.type)} • ${money(product.price)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = "${product.length.toPlainString()} × ${product.depth.toPlainString()} × ${product.height.toPlainString()} cm",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline,
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 2.dp),
                                        ) {
                                            Icon(
                                                Icons.Default.Schedule,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary,
                                            )
                                            Text(
                                                text = "Czas: ${product.leadTimeWeeks?.takeIf { it > 0 }?.let { "$it tyg." } ?: "nie określono"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium,
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onAddProduct(product) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(36.dp),
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Dodaj")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    val totalOrderCount = orderItems.sumOf { it.quantity }
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            if (totalOrderCount > 0) "Gotowe (w zamówieniu: $totalOrderCount szt.)"
                            else "Gotowe"
                        )
                    }
                }
            }
        }
    }
}

private fun Double.toPlainString(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()
