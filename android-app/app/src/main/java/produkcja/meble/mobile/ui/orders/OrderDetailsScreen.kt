package produkcja.meble.mobile.ui.orders

import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import produkcja.meble.mobile.ui.common.FullScreenImageDialog
import produkcja.meble.mobile.ui.common.resolveImageUrl
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.OrderItem
import produkcja.meble.mobile.data.OrderStatus
import produkcja.meble.mobile.ui.common.DetailRow
import produkcja.meble.mobile.ui.common.OptionalDetailRow
import produkcja.meble.mobile.ui.common.dateTime
import produkcja.meble.mobile.ui.common.isMeaningful
import produkcja.meble.mobile.ui.common.money
import produkcja.meble.mobile.ui.theme.*

@Composable
fun OrderDetails(
    order: FurnitureOrder,
    onEditOrder: ((FurnitureOrder) -> Unit)? = null,
    onDeleteOrder: ((FurnitureOrder) -> Unit)? = null,
    onToggleStatus: ((FurnitureOrder, Boolean) -> Unit)? = null,
    onUpdateOrderStatus: ((FurnitureOrder, OrderStatus) -> Unit)? = null,
) {
    val isDark = isSystemInDarkTheme()
    val orderStyle = orderSectionStyle(isDark)
    val backgroundColor = orderListBackgroundColor(isDark)

    var showStatusDialog by rememberSaveable { mutableStateOf(false) }
    var fullScreenImage by remember { mutableStateOf<FullScreenImageData?>(null) }

    BackHandler(enabled = fullScreenImage != null) {
        fullScreenImage = null
    }
    BackHandler(enabled = showStatusDialog) {
        showStatusDialog = false
    }

    val itemsTotal = order.items.sumOf { it.price * it.quantity }
    val servicesTotal = order.assemblyPrice + order.transportPrice + order.carryProductsPrice
    val subtotalBeforeDiscount = itemsTotal + servicesTotal
    val totalOrderPrice = (subtotalBeforeDiscount - order.discount).coerceAtLeast(0.0)
    val remainingToPay = (totalOrderPrice - order.deposit).coerceAtLeast(0.0)

    if (showStatusDialog) {
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = {
                Text(
                    text = "Zmień status zamówienia #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OrderStatus.entries.forEach { statusOption ->
                        val isSelected = order.orderStatus == statusOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (onUpdateOrderStatus != null) {
                                        onUpdateOrderStatus(order, statusOption)
                                    } else if (onToggleStatus != null) {
                                        onToggleStatus(order, statusOption == OrderStatus.ACTIVE)
                                    }
                                    showStatusDialog = false
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (onUpdateOrderStatus != null) {
                                        onUpdateOrderStatus(order, statusOption)
                                    } else if (onToggleStatus != null) {
                                        onToggleStatus(order, statusOption == OrderStatus.ACTIVE)
                                    }
                                    showStatusDialog = false
                                },
                            )
                            Text(
                                text = statusOption.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) {
                    Text("Anuluj")
                }
            },
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        // Status Card
        item {
            val (statusTitle, statusIcon, statusColor, statusBg) = when (order.orderStatus) {
                OrderStatus.ACTIVE -> StatusPresentation(
                    title = "Aktywne",
                    icon = Icons.Default.CheckCircle,
                    color = orderStyle.primary,
                    container = orderStyle.container.copy(alpha = 0.65f),
                )
                OrderStatus.COMPLETED -> StatusPresentation(
                    title = "Zrealizowane",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary,
                    container = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                )
                OrderStatus.CANCELLED -> StatusPresentation(
                    title = "Anulowane",
                    icon = Icons.Default.Cancel,
                    color = MaterialTheme.colorScheme.error,
                    container = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = statusBg,
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    statusColor.copy(alpha = 0.5f),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = statusTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                            )
                        }
                    }

                    if (onUpdateOrderStatus != null || onToggleStatus != null) {
                        Spacer(Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick = { showStatusDialog = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = statusColor.copy(alpha = 0.18f),
                                contentColor = statusColor,
                            ),
                        ) {
                            Text("Zmień status")
                        }
                    }
                }
            }
        }

        // Customer & Delivery Information
        item {
            DetailsCard(title = "Dane klienta i dostawy", orderStyle = orderStyle) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(label = "Adres dostawy", value = order.address)
                    DetailRow(label = "Numer telefonu", value = order.phoneNumber)
                    DetailRow(label = "Termin dostawy", value = dateTime(order.deliveryDate))
                    val weeks = order.fulfillmentTime
                    val weeksText = when {
                        weeks <= 0 -> "Nie określono"
                        weeks == 1 -> "1 tydzień"
                        weeks % 10 in 2..4 && (weeks % 100 !in 12..14) -> "$weeks tygodnie"
                        else -> "$weeks tygodni"
                    }
                    DetailRow(
                        label = "Czas realizacji",
                        value = weeksText,
                    )
                    OptionalDetailRow(label = "Uwagi do kontaktu", value = order.contactRemarks)
                }
            }
        }

        // Ordered Items List
        item {
            Text(
                text = "Zamówione meble (${order.items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = orderStyle.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        items(order.items, key = { it.id }) { item ->
            OrderItemCard(
                item = item,
                orderStyle = orderStyle,
                onImageClick = { images, index, title ->
                    fullScreenImage = FullScreenImageData(images, index, title)
                },
            )
        }

        // Additional Services & Options (Collapsible)
        val hasAdditionalCosts = order.assemblyPrice > 0.0 ||
            order.assemblyRemarks.isNotBlank() ||
            order.transportPrice > 0.0 ||
            order.transportRemarks.isNotBlank() ||
            order.carryProductsPrice > 0.0 ||
            order.carryProductsRemarks.isNotBlank() ||
            order.discount > 0.0 ||
            order.discountRemarks.isNotBlank() ||
            order.deposit > 0.0 ||
            order.remarks.isNotBlank()

        if (hasAdditionalCosts) {
            item {
                var expanded by rememberSaveable { mutableStateOf(true) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = orderStyle.cardBackground,
                    ),
                    border = BorderStroke(1.dp, orderStyle.cardBorder),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Dodatkowe usługi i uwagi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = orderStyle.primary,
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = orderStyle.primary,
                            )
                        }

                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(bottom = 4.dp),
                                    color = orderStyle.cardBorder.copy(alpha = 0.6f),
                                )

                                if (order.assemblyPrice > 0.0 || order.assemblyRemarks.isNotBlank()) {
                                    DetailRow(
                                        label = "Montaż",
                                        value = if (order.assemblyPrice > 0.0) money(order.assemblyPrice) else "W cenie",
                                    )
                                    OptionalDetailRow(label = "Uwagi do montażu", value = order.assemblyRemarks)
                                }

                                if (order.transportPrice > 0.0 || order.transportRemarks.isNotBlank()) {
                                    DetailRow(
                                        label = "Transport",
                                        value = if (order.transportPrice > 0.0) money(order.transportPrice) else "W cenie",
                                    )
                                    OptionalDetailRow(label = "Uwagi do transportu", value = order.transportRemarks)
                                }

                                if (order.carryProductsPrice > 0.0 || order.carryProductsRemarks.isNotBlank()) {
                                    DetailRow(
                                        label = "Wniesienie",
                                        value = if (order.carryProductsPrice > 0.0) money(order.carryProductsPrice) else "W cenie",
                                    )
                                    OptionalDetailRow(label = "Uwagi do wniesienia", value = order.carryProductsRemarks)
                                }

                                if (order.discount > 0.0 || order.discountRemarks.isNotBlank()) {
                                    DetailRow(label = "Rabat", value = "-${money(order.discount)}")
                                    OptionalDetailRow(label = "Uwagi do rabatu", value = order.discountRemarks)
                                }

                                OptionalDetailRow(label = "Uwagi ogólne", value = order.remarks)
                            }
                        }
                    }
                }
            }
        }

        // Financial Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = orderStyle.container.copy(alpha = 0.7f),
                ),
                border = BorderStroke(1.dp, orderStyle.accent.copy(alpha = 0.5f)),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Podsumowanie finansowe",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = orderStyle.primary,
                    )

                    HorizontalDivider(color = orderStyle.accent.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Wartość mebli:", style = MaterialTheme.typography.bodyMedium)
                        Text(money(itemsTotal), fontWeight = FontWeight.Medium)
                    }

                    if (servicesTotal > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Usługi dodatkowe:", style = MaterialTheme.typography.bodyMedium)
                            Text("+${money(servicesTotal)}", fontWeight = FontWeight.Medium)
                        }
                    }

                    if (order.discount > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Rabat:", style = MaterialTheme.typography.bodyMedium)
                            Text("-${money(order.discount)}", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                    }

                    HorizontalDivider(color = orderStyle.accent.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Łączna wartość:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            money(totalOrderPrice),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = orderStyle.primary,
                        )
                    }

                    if (order.deposit > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Wpłacona zaliczka:", style = MaterialTheme.typography.bodyMedium)
                            Text("-${money(order.deposit)}", fontWeight = FontWeight.Medium)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "Pozostało do zapłaty:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = orderStyle.primary,
                            )
                            Text(
                                money(remainingToPay),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = orderStyle.primary,
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Edit / Delete
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (onEditOrder != null) {
                    FilledTonalButton(
                        onClick = { onEditOrder(order) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = orderStyle.container,
                            contentColor = orderStyle.primary,
                        ),
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edytuj zamówienie")
                    }
                }

                if (onDeleteOrder != null) {
                    OutlinedButton(
                        onClick = { onDeleteOrder(order) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Usuń")
                    }
                }
            }
        }
    }

    fullScreenImage?.let { data ->
        FullScreenImageDialog(
            images = data.images,
            initialIndex = data.initialIndex,
            title = data.title,
            onDismiss = { fullScreenImage = null },
        )
    }
}

private data class FullScreenImageData(
    val images: List<String>,
    val initialIndex: Int,
    val title: String,
)

private data class StatusPresentation(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val container: Color,
)

@Composable
private fun OrderItemCard(
    item: OrderItem,
    orderStyle: SectionStyle,
    onImageClick: ((images: List<String>, initialIndex: Int, title: String) -> Unit)? = null,
) {
    val allImages = item.product?.allImages.orEmpty()
    val displayModel = item.product?.model?.takeIf { it.isMeaningful() } ?: "Produkt #${item.id}"
    var selectedImageIndex by rememberSaveable(item.id, allImages) { mutableIntStateOf(0) }
    val safeIndex = if (allImages.isNotEmpty()) selectedImageIndex.coerceIn(0, allImages.lastIndex) else 0

    Card(
        colors = CardDefaults.cardColors(
            containerColor = orderStyle.cardBackground,
        ),
        border = BorderStroke(1.dp, orderStyle.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                // Zdjecie produktu lub ikona zastepcza
                if (allImages.isNotEmpty()) {
                    val context = LocalContext.current
                    val currentRawUrl = allImages[safeIndex]
                    val resolvedUrl = remember(currentRawUrl) {
                        resolveImageUrl(currentRawUrl) ?: currentRawUrl
                    }
                    val imageRequest = remember(resolvedUrl) {
                        ImageRequest.Builder(context)
                            .data(resolvedUrl)
                            .crossfade(true)
                            .build()
                    }

                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(1.dp, orderStyle.cardBorder.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                            .clickable(enabled = onImageClick != null) {
                                onImageClick?.invoke(allImages, safeIndex, displayModel)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        SubcomposeAsyncImage(
                            model = imageRequest,
                            contentDescription = "Zdjęcie: $displayModel",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                            },
                            error = {
                                Icon(
                                    imageVector = Icons.Default.ImageNotSupported,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(28.dp),
                                )
                            },
                        )

                        if (allImages.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 6.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier.align(Alignment.BottomEnd),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Icon(
                                        Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp),
                                    )
                                    Text(
                                        text = "${allImages.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(orderStyle.container.copy(alpha = 0.25f))
                            .border(1.dp, orderStyle.cardBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chair,
                            contentDescription = null,
                            tint = orderStyle.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }

                // Kolumna szczegolow produktu
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = displayModel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
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
                                text = if (item.quantity > 1) {
                                    "Ilość: ${item.quantity} × ${money(item.price)}"
                                } else {
                                    "Ilość: 1 szt. • ${money(item.price)}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Text(
                            text = money(item.price * item.quantity),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = orderStyle.primary,
                        )
                    }
                }
            }

            // Pasek miniaturek dla wielu zdjec
            if (allImages.size > 1) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    allImages.forEachIndexed { idx, rawUrl ->
                        val isCurrent = idx == safeIndex
                        val context = LocalContext.current
                        val resolvedUrl = remember(rawUrl) { resolveImageUrl(rawUrl) ?: rawUrl }
                        val thumbRequest = remember(resolvedUrl) {
                            ImageRequest.Builder(context).data(resolvedUrl).crossfade(true).build()
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) orderStyle.primary else orderStyle.cardBorder.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(6.dp),
                                )
                                .clickable {
                                    selectedImageIndex = idx
                                    onImageClick?.invoke(allImages, idx, displayModel)
                                },
                        ) {
                            AsyncImage(
                                model = thumbRequest,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }

            if (item.isCustomized) {
                Spacer(Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
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
        }
    }
}

@Composable
private fun DetailsCard(title: String, orderStyle: SectionStyle, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = orderStyle.cardBackground,
        ),
        border = BorderStroke(1.dp, orderStyle.cardBorder),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = orderStyle.primary,
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = orderStyle.cardBorder.copy(alpha = 0.6f),
            )
            content()
        }
    }
}

private fun Double.toPlainString(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()
