package produkcja.meble.mobile.ui.orders

import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Chair
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import produkcja.meble.mobile.ui.common.resolveImageUrl
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.OrderStatus
import produkcja.meble.mobile.ui.common.EmptyState
import produkcja.meble.mobile.ui.common.date
import produkcja.meble.mobile.ui.common.isMeaningful
import produkcja.meble.mobile.ui.theme.orderListBackgroundColor
import produkcja.meble.mobile.ui.theme.orderSectionStyle

@Composable
fun OrderList(
    orders: List<FurnitureOrder>,
    selectedStatus: OrderStatus? = OrderStatus.ACTIVE,
    onSelectStatus: (OrderStatus?) -> Unit = {},
    showOnlyActive: Boolean = true,
    onToggleShowActive: (Boolean) -> Unit = {},
    onOrderSelected: (FurnitureOrder) -> Unit,
    onEditOrder: ((FurnitureOrder) -> Unit)? = null,
    onDeleteOrder: ((FurnitureOrder) -> Unit)? = null,
    onToggleStatus: ((FurnitureOrder, Boolean) -> Unit)? = null,
    onUpdateOrderStatus: ((FurnitureOrder, OrderStatus) -> Unit)? = null,
) {
    val isDark = isSystemInDarkTheme()
    val orderStyle = orderSectionStyle(isDark)
    val backgroundColor = orderListBackgroundColor(isDark)

    var orderForStatusChange by remember { mutableStateOf<FurnitureOrder?>(null) }

    val displayedOrders = remember(orders, selectedStatus) {
        if (selectedStatus != null) {
            orders.filter { it.orderStatus == selectedStatus }
        } else {
            orders
        }
    }

    if (orderForStatusChange != null) {
        val targetOrder = orderForStatusChange!!
        AlertDialog(
            onDismissRequest = { orderForStatusChange = null },
            title = {
                Text(
                    text = "Zmień status zamówienia #${targetOrder.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OrderStatus.entries.forEach { statusOption ->
                        val isSelected = targetOrder.orderStatus == statusOption
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (onUpdateOrderStatus != null) {
                                        onUpdateOrderStatus(targetOrder, statusOption)
                                    } else if (onToggleStatus != null) {
                                        onToggleStatus(targetOrder, statusOption == OrderStatus.ACTIVE)
                                    }
                                    orderForStatusChange = null
                                }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (onUpdateOrderStatus != null) {
                                        onUpdateOrderStatus(targetOrder, statusOption)
                                    } else if (onToggleStatus != null) {
                                        onToggleStatus(targetOrder, statusOption == OrderStatus.ACTIVE)
                                    }
                                    orderForStatusChange = null
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
                TextButton(onClick = { orderForStatusChange = null }) {
                    Text("Anuluj")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val filterOptions = listOf(
                null to "Wszystkie",
                OrderStatus.ACTIVE to "Aktywne",
                OrderStatus.COMPLETED to "Zrealizowane",
                OrderStatus.CANCELLED to "Anulowane",
            )
            filterOptions.forEach { (status, label) ->
                val isSelected = selectedStatus == status
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onSelectStatus(status)
                        onToggleShowActive(status == OrderStatus.ACTIVE)
                    },
                    label = { Text(label) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = orderStyle.container,
                        selectedLabelColor = orderStyle.onContainer,
                        selectedLeadingIconColor = orderStyle.primary,
                    ),
                )
            }
        }

        if (displayedOrders.isEmpty()) {
            val emptyTitle = when (selectedStatus) {
                OrderStatus.ACTIVE -> "Brak aktywnych zamówień"
                OrderStatus.COMPLETED -> "Brak zrealizowanych zamówień"
                OrderStatus.CANCELLED -> "Brak anulowanych zamówień"
                null -> "Brak zamówień"
            }
            val emptyDesc = when (selectedStatus) {
                OrderStatus.ACTIVE -> "Nowe aktywne zamówienia pojawią się tutaj."
                OrderStatus.COMPLETED -> "Zrealizowane zamówienia pojawią się tutaj."
                OrderStatus.CANCELLED -> "Anulowane zamówienia pojawią się tutaj."
                null -> "Baza zamówień jest pusta."
            }
            EmptyState(
                title = emptyTitle,
                message = emptyDesc,
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconTint = orderStyle.primary,
                titleColor = orderStyle.primary,
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(displayedOrders, key = FurnitureOrder::id) { order ->
                    var isActionsExpanded by rememberSaveable { mutableStateOf(false) }

                    Card(
                        onClick = { onOrderSelected(order) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = orderStyle.cardBackground,
                        ),
                        border = BorderStroke(1.dp, orderStyle.cardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false),
                                ) {
                                    Text(
                                        text = "Zamówienie #${order.id}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    when (order.orderStatus) {
                                        OrderStatus.ACTIVE -> {
                                            Surface(
                                                color = orderStyle.container,
                                                shape = MaterialTheme.shapes.extraSmall,
                                            ) {
                                                Text(
                                                    text = "Aktywne",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = orderStyle.onContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                            }
                                        }
                                        OrderStatus.COMPLETED -> {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = MaterialTheme.shapes.extraSmall,
                                            ) {
                                                Text(
                                                    text = "Zrealizowane",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontWeight = FontWeight.SemiBold,
                                                )
                                            }
                                        }
                                        OrderStatus.CANCELLED -> {
                                            Surface(
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                shape = MaterialTheme.shapes.extraSmall,
                                            ) {
                                                Text(
                                                    text = "Anulowane",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    fontWeight = FontWeight.Bold,
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = date(order.deliveryDate),
                                        color = orderStyle.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    if (onUpdateOrderStatus != null || onToggleStatus != null || onEditOrder != null || onDeleteOrder != null) {
                                        IconButton(
                                            onClick = { isActionsExpanded = !isActionsExpanded },
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(
                                                imageVector = if (isActionsExpanded) Icons.Default.ExpandLess else Icons.Default.MoreVert,
                                                contentDescription = if (isActionsExpanded) "Zwiń opcje" else "Rozwiń opcje",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(order.address, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = order.phoneNumber,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val creatorUsername = order.createdBy?.username?.takeIf { it.isMeaningful() }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Dodał",
                                    modifier = Modifier.size(16.dp),
                                    tint = orderStyle.primary,
                                )
                                Text(
                                    text = "Dodał(a): ${creatorUsername ?: "Nieznany"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = orderStyle.primary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }

                            if (order.items.isNotEmpty()) {
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val displayItems = order.items.take(4)
                                    displayItems.forEach { item ->
                                        val firstImage = item.product?.allImages?.firstOrNull()
                                        if (firstImage != null) {
                                            val context = LocalContext.current
                                            val resolvedUrl = remember(firstImage) { resolveImageUrl(firstImage) ?: firstImage }
                                            val imageRequest = remember(resolvedUrl) {
                                                ImageRequest.Builder(context).data(resolvedUrl).crossfade(true).build()
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                                    .border(1.dp, orderStyle.cardBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                                            ) {
                                                AsyncImage(
                                                    model = imageRequest,
                                                    contentDescription = item.product?.model,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                )
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(orderStyle.container.copy(alpha = 0.25f))
                                                    .border(1.dp, orderStyle.cardBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    Icons.Default.Chair,
                                                    contentDescription = null,
                                                    tint = orderStyle.primary.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(24.dp),
                                                )
                                            }
                                        }
                                    }
                                    if (order.items.size > 4) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = orderStyle.container.copy(alpha = 0.5f),
                                            modifier = Modifier.size(46.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "+${order.items.size - 4}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = orderStyle.primary,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (onUpdateOrderStatus != null || onToggleStatus != null || onEditOrder != null || onDeleteOrder != null) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { isActionsExpanded = !isActionsExpanded }
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = null,
                                            tint = orderStyle.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = if (isActionsExpanded) "Zwiń akcje" else "Rozwiń akcje",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = orderStyle.primary,
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isActionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = orderStyle.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }

                                AnimatedVisibility(
                                    visible = isActionsExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut(),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        if (onUpdateOrderStatus != null || onToggleStatus != null) {
                                            OutlinedButton(
                                                onClick = { orderForStatusChange = order },
                                                modifier = Modifier.weight(1.1f),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                            ) {
                                                Icon(
                                                    imageVector = when (order.orderStatus) {
                                                        OrderStatus.ACTIVE -> Icons.Default.PlayCircleOutline
                                                        OrderStatus.COMPLETED -> Icons.Default.CheckCircle
                                                        OrderStatus.CANCELLED -> Icons.Default.Cancel
                                                    },
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Status", maxLines = 1)
                                            }
                                        }
                                        if (onEditOrder != null) {
                                            OutlinedButton(
                                                onClick = { onEditOrder(order) },
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Edytuj", maxLines = 1)
                                            }
                                        }
                                        if (onDeleteOrder != null) {
                                            OutlinedButton(
                                                onClick = { onDeleteOrder(order) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error,
                                                ),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text("Usuń", maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
