package produkcja.meble.mobile.ui.orders

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.OrderItem
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
) {
    val isDark = isSystemInDarkTheme()
    val orderStyle = orderSectionStyle(isDark)
    val backgroundColor = orderListBackgroundColor(isDark)

    val itemsTotal = order.items.sumOf { it.price * it.quantity }
    val servicesTotal = order.assemblyPrice + order.transportPrice + order.carryProductsPrice
    val subtotalBeforeDiscount = itemsTotal + servicesTotal
    val totalOrderPrice = (subtotalBeforeDiscount - order.discount).coerceAtLeast(0.0)
    val remainingToPay = (totalOrderPrice - order.deposit).coerceAtLeast(0.0)

    LazyColumn(
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
    ) {
        // Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (order.active) {
                        orderStyle.container.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
                    },
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (order.active) orderStyle.accent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
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
                                imageVector = if (order.active) Icons.Default.CheckCircle else Icons.Default.PauseCircle,
                                contentDescription = null,
                                tint = if (order.active) orderStyle.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp),
                            )
                            Text(
                                text = if (order.active) "Aktywne (w realizacji)" else "Nieaktywne (zakończone)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (order.active) orderStyle.primary else MaterialTheme.colorScheme.error,
                            )
                        }
                        Text(
                            text = if (order.active) {
                                "To zamówienie jest obecnie aktywne i widoczne w realizacji."
                            } else {
                                "To zamówienie zostało oznaczone jako zakończone lub wstrzymane."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    if (onToggleStatus != null) {
                        Spacer(Modifier.width(12.dp))
                        FilledTonalButton(
                            onClick = { onToggleStatus(order, !order.active) },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (order.active) MaterialTheme.colorScheme.errorContainer else orderStyle.container,
                                contentColor = if (order.active) MaterialTheme.colorScheme.onErrorContainer else orderStyle.primary,
                            ),
                        ) {
                            Text(if (order.active) "Zakończ" else "Wznów")
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
                    DetailRow(
                        label = "Czas realizacji",
                        value = "${order.fulfillmentTime} ${if (order.fulfillmentTime == 1) "dzień" else "dni"}",
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
            OrderItemCard(item = item, orderStyle = orderStyle)
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
}

@Composable
private fun OrderItemCard(item: OrderItem, orderStyle: SectionStyle) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = orderStyle.cardBackground,
        ),
        border = BorderStroke(1.dp, orderStyle.cardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val modelName = item.product?.model?.takeIf { it.isMeaningful() }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = modelName ?: "Produkt #${item.id}",
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
