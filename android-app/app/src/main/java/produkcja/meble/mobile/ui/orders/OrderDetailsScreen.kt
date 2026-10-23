package produkcja.meble.mobile.ui.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.OrderItem
import produkcja.meble.mobile.ui.common.DetailRow
import produkcja.meble.mobile.ui.common.OptionalDetailRow
import produkcja.meble.mobile.ui.common.dateTime
import produkcja.meble.mobile.ui.common.isMeaningful
import produkcja.meble.mobile.ui.common.money

@Composable
fun OrderDetails(
    order: FurnitureOrder,
    onEditOrder: ((FurnitureOrder) -> Unit)? = null,
    onDeleteOrder: ((FurnitureOrder) -> Unit)? = null,
    onToggleStatus: ((FurnitureOrder, Boolean) -> Unit)? = null,
) {
    val itemsTotal = order.items.sumOf { it.price * it.quantity }
    val servicesTotal = order.assemblyPrice + order.transportPrice + order.carryProductsPrice
    val subtotalBeforeDiscount = itemsTotal + servicesTotal
    val totalOrderPrice = (subtotalBeforeDiscount - order.discount).coerceAtLeast(0.0)
    val remainingToPay = (totalOrderPrice - order.deposit).coerceAtLeast(0.0)

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (order.active) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                    },
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = if (order.active) Icons.Default.CheckCircle else Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = if (order.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp),
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = if (order.active) "Status: Aktywne" else "Status: Nieaktywne",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (order.active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer,
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (order.active) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    },
                                ) {
                                    Text(
                                        text = if (order.active) "W realizacji" else "Zakończone",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Text(
                                text = if (order.active) "Zamówienie widoczne jako aktywne" else "Zamówienie oznaczone jako nieaktywne",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (onToggleStatus != null) {
                        FilledTonalButton(
                            onClick = { onToggleStatus(order, !order.active) },
                            colors = if (order.active) {
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    contentColor = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(if (order.active) "Deaktywuj" else "Aktywuj")
                        }
                    }
                }
            }
        }

        // Action Buttons
        if (onEditOrder != null || onDeleteOrder != null) {
            item {
                var isActionsExpanded by rememberSaveable { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isActionsExpanded = !isActionsExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = if (isActionsExpanded) "Zwiń opcje zarządzania" else "Opcje zarządzania zamówieniem",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Icon(
                                imageVector = if (isActionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        AnimatedVisibility(
                            visible = isActionsExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            Column {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    if (onEditOrder != null) {
                                        OutlinedButton(
                                            onClick = { onEditOrder(order) },
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Edytuj")
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
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text("Usuń")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            DetailsCard("Dostawa") {
                DetailRow("Termin", dateTime(order.deliveryDate))
                DetailRow("Adres", order.address)
                DetailRow("Telefon", order.phoneNumber)
                DetailRow("Czas realizacji", "${order.fulfillmentTime} dni")
                DetailRow("Utworzono", dateTime(order.createdAt))
                val creatorUsername = order.createdBy?.username?.takeIf { it.isMeaningful() }
                DetailRow("Dodał(a)", creatorUsername ?: "Brak danych")
            }
        }

        if (order.items.isNotEmpty()) {
            item {
                Text(
                    text = "Produkty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(order.items, key = { "${it.id}-${it.product?.id}" }) { item ->
                OrderItemCard(item)
            }
        }

        item {
            DetailsCard("Rozliczenie") {
                DetailRow("Wartość produktów", money(itemsTotal))
                OptionalDetailRow("Montaż", order.assemblyPrice, order.assemblyRemarks)
                OptionalDetailRow("Transport", order.transportPrice, order.transportRemarks)
                OptionalDetailRow("Wniesienie", order.carryProductsPrice, order.carryProductsRemarks)
                OptionalDetailRow("Rabat", order.discount, order.discountRemarks, isDiscount = true)
                DetailRow("Suma do zapłaty", money(totalOrderPrice), isHighlight = true)
                OptionalDetailRow("Wpłacona zaliczka", order.deposit, isDiscount = true)
                DetailRow("Pozostało do zapłaty", money(remainingToPay), isHighlight = true)
            }
        }

        val remarksList = listOf(
            "Uwagi do kontaktu" to order.contactRemarks,
            "Uwagi do montażu" to order.assemblyRemarks,
            "Uwagi do transportu" to order.transportRemarks,
            "Uwagi do wniesienia" to order.carryProductsRemarks,
            "Uwagi ogólne" to order.remarks,
        )
        val meaningfulRemarks = remarksList.filter { (_, text) -> text.isMeaningful() }

        if (meaningfulRemarks.isNotEmpty()) {
            item {
                DetailsCard("Uwagi") {
                    meaningfulRemarks.forEach { (label, text) ->
                        DetailRow(label, text.trim())
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderItemCard(item: OrderItem) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val modelName = item.product?.model?.takeIf { it.isMeaningful() }
                Text(
                    text = modelName ?: "Produkt #${item.id}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (item.quantity > 1) {
                        "Ilość: ${item.quantity} × ${money(item.price)}"
                    } else {
                        "Ilość: 1 szt."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = money(item.price * item.quantity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DetailsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            content()
        }
    }
}
