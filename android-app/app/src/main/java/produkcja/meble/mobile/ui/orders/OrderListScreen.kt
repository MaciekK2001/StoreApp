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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import produkcja.meble.mobile.ui.common.EmptyState
import produkcja.meble.mobile.ui.common.date
import produkcja.meble.mobile.ui.common.isMeaningful

@Composable
fun OrderList(
    orders: List<FurnitureOrder>,
    showOnlyActive: Boolean = true,
    onToggleShowActive: (Boolean) -> Unit = {},
    onOrderSelected: (FurnitureOrder) -> Unit,
    onEditOrder: ((FurnitureOrder) -> Unit)? = null,
    onDeleteOrder: ((FurnitureOrder) -> Unit)? = null,
    onToggleStatus: ((FurnitureOrder, Boolean) -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = showOnlyActive,
                onClick = { onToggleShowActive(true) },
                label = { Text("Tylko aktywne") },
                leadingIcon = if (showOnlyActive) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
            )
            FilterChip(
                selected = !showOnlyActive,
                onClick = { onToggleShowActive(false) },
                label = { Text("Wszystkie zamówienia") },
                leadingIcon = if (!showOnlyActive) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
            )
        }

        if (orders.isEmpty()) {
            val emptyTitle = if (showOnlyActive) "Brak aktywnych zamówień" else "Brak zamówień"
            val emptyDesc = if (showOnlyActive) "Nowe aktywne zamówienia pojawią się tutaj." else "Baza zamówień jest pusta."
            EmptyState(emptyTitle, emptyDesc)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(orders, key = FurnitureOrder::id) { order ->
                    var isActionsExpanded by rememberSaveable { mutableStateOf(false) }

                    Card(
                        onClick = { onOrderSelected(order) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
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
                                    if (!order.active) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = MaterialTheme.shapes.extraSmall,
                                        ) {
                                            Text(
                                                text = "Nieaktywne",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = date(order.deliveryDate),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    if (onToggleStatus != null || onEditOrder != null || onDeleteOrder != null) {
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
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = "Dodał(a): ${creatorUsername ?: "Nieznany"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            if (order.remarks.isMeaningful()) {
                                Text(
                                    text = order.remarks.trim(),
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }

                            if (onToggleStatus != null || onEditOrder != null || onDeleteOrder != null) {
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
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = if (isActionsExpanded) "Zwiń akcje" else "Rozwiń akcje",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isActionsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
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
                                        if (onToggleStatus != null) {
                                            OutlinedButton(
                                                onClick = { onToggleStatus(order, !order.active) },
                                                modifier = Modifier.weight(1.1f),
                                                colors = if (order.active) {
                                                    ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                } else {
                                                    ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.primary,
                                                    )
                                                },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                                            ) {
                                                Icon(
                                                    imageVector = if (order.active) Icons.Default.PauseCircle else Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = if (order.active) "Zakończ" else "Aktywuj",
                                                    maxLines = 1,
                                                )
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
