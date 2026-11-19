package produkcja.meble.mobile.ui.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import produkcja.meble.mobile.data.OrderFilter
import produkcja.meble.mobile.data.OrderSortOption
import produkcja.meble.mobile.ui.common.number
import produkcja.meble.mobile.ui.theme.orderSectionStyle

@Composable
fun OrderFilterCard(
    filter: OrderFilter,
    onFilterChange: (OrderFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val orderStyle = orderSectionStyle(isDark)

    val metallicTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = orderStyle.primary,
        unfocusedBorderColor = orderStyle.cardBorder,
        focusedLabelColor = orderStyle.primary,
        unfocusedLabelColor = if (isDark) Color(0xFF8FA4BA) else Color(0xFF5A738E),
        cursorColor = orderStyle.primary,
        focusedLeadingIconColor = orderStyle.primary,
        unfocusedLeadingIconColor = orderStyle.primary.copy(alpha = 0.75f),
        focusedTrailingIconColor = orderStyle.primary,
        unfocusedTrailingIconColor = if (isDark) Color(0xFF8FA4BA) else Color(0xFF5A738E),
        focusedTextColor = if (isDark) Color(0xFFE2EBF5) else Color(0xFF0F2236),
        unfocusedTextColor = if (isDark) Color(0xFFD6E4F6) else Color(0xFF1B314B),
        focusedContainerColor = if (isDark) Color(0xFF121B27) else Color(0xFFF7FAFD),
        unfocusedContainerColor = if (isDark) Color(0xFF152230) else Color(0xFFF2F6FA),
    )

    Card(
        modifier = modifier.animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = orderStyle.cardBackground,
        ),
        border = BorderStroke(1.dp, orderStyle.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            // Nagłówek filtrów
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = orderStyle.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Filtry i sortowanie",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = orderStyle.primary,
                    )
                    if (!filter.isEmpty) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = orderStyle.primary,
                            contentColor = orderStyle.onPrimary,
                        ) {
                            Text(
                                text = "${filter.activeCount}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!filter.isEmpty) {
                        IconButton(
                            onClick = { onFilterChange(OrderFilter()) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Wyczyść filtry",
                                tint = orderStyle.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                            tint = orderStyle.primary,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val filterScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .heightIn(max = 450.dp)
                        .verticalScroll(filterScrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    HorizontalDivider(color = orderStyle.cardBorder.copy(alpha = 0.5f))

                    // Wyszukiwanie tekstowe
                    OutlinedTextField(
                        value = filter.query,
                        onValueChange = { onFilterChange(filter.copy(query = it)) },
                        label = { Text("Szukaj zamówienia") },
                        placeholder = { Text("ID, adres, telefon, mebel, klient...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (filter.query.isNotBlank()) {
                                IconButton(onClick = { onFilterChange(filter.copy(query = "")) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść wyszukiwanie")
                                }
                            }
                        },
                        singleLine = true,
                        colors = metallicTextFieldColors,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Sortowanie po dacie i cenie
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null,
                                tint = orderStyle.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Sortuj według",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = orderStyle.primary,
                            )
                        }

                        val sortScrollState = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(sortScrollState),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            OrderSortOption.entries.forEach { option ->
                                val isSelected = filter.sortBy == option
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onFilterChange(filter.copy(sortBy = option)) },
                                    label = { Text(option.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isDark) Color(0xFF131F2E) else Color(0xFFEDF3FA),
                                        labelColor = if (isDark) Color(0xFFCBDDF0) else Color(0xFF1C3A5A),
                                        selectedContainerColor = orderStyle.container,
                                        selectedLabelColor = orderStyle.onContainer,
                                        selectedLeadingIconColor = orderStyle.primary,
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = orderStyle.cardBorder.copy(alpha = 0.85f),
                                        selectedBorderColor = orderStyle.primary,
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.5.dp,
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                )
                            }
                        }
                    }

                    // Zakres cenowy
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = orderStyle.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Wartość zamówienia (zł)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = orderStyle.primary,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = filter.minPrice?.let { number(it) }.orEmpty(),
                                onValueChange = { input ->
                                    val parsed = input.toDoubleOrNull()
                                    onFilterChange(filter.copy(minPrice = parsed))
                                },
                                label = { Text("Cena od") },
                                placeholder = { Text("np. 1000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = metallicTextFieldColors,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = filter.maxPrice?.let { number(it) }.orEmpty(),
                                onValueChange = { input ->
                                    val parsed = input.toDoubleOrNull()
                                    onFilterChange(filter.copy(maxPrice = parsed))
                                },
                                label = { Text("Cena do") },
                                placeholder = { Text("np. 10000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = metallicTextFieldColors,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // Zakres terminu dostawy
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = orderStyle.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Termin dostawy",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = orderStyle.primary,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = filter.dateFrom.orEmpty(),
                                onValueChange = { onFilterChange(filter.copy(dateFrom = it.ifBlank { null })) },
                                label = { Text("Od daty") },
                                placeholder = { Text("RRRR-MM-DD") },
                                singleLine = true,
                                colors = metallicTextFieldColors,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = filter.dateTo.orEmpty(),
                                onValueChange = { onFilterChange(filter.copy(dateTo = it.ifBlank { null })) },
                                label = { Text("Do daty") },
                                placeholder = { Text("RRRR-MM-DD") },
                                singleLine = true,
                                colors = metallicTextFieldColors,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if (!filter.isEmpty) {
                        OutlinedButton(
                            onClick = { onFilterChange(OrderFilter()) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = orderStyle.primary,
                            ),
                            border = BorderStroke(1.dp, orderStyle.cardBorder),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Wyczyść wszystkie filtry")
                        }
                    }
                }
            }
        }
    }
}
