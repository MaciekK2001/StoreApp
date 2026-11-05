package produkcja.meble.mobile.ui.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import produkcja.meble.mobile.data.FurnitureType
import produkcja.meble.mobile.data.ProductFilter as ProductFilterData
import produkcja.meble.mobile.ui.common.number

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductFilter(
    filter: ProductFilterData,
    onFilterChange: (ProductFilterData) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier.animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Filtry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!filter.isEmpty) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ) {
                            Text(
                                text = "${filter.activeCount}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!filter.isEmpty) {
                        IconButton(
                            onClick = { onFilterChange(ProductFilterData()) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Wyczyść filtry",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp),
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Wyszukiwanie po modelu / tekście
                    OutlinedTextField(
                        value = filter.model.orEmpty(),
                        onValueChange = { onFilterChange(filter.copy(model = it.ifBlank { null })) },
                        label = { Text("Szukaj po nazwie / opisie") },
                        placeholder = { Text("np. Sofa, Dąb, Riviera...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (!filter.model.isNullOrBlank()) {
                                IconButton(onClick = { onFilterChange(filter.copy(model = null)) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Wyczyść")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Typ mebla (chipy wielokrotnego wyboru)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Kategoria mebla",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            FurnitureType.entries.forEach { type ->
                                val isSelected = filter.type.contains(type.displayName)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val updatedTypes = if (isSelected) {
                                            filter.type - type.displayName
                                        } else {
                                            filter.type + type.displayName
                                        }
                                        onFilterChange(filter.copy(type = updatedTypes))
                                    },
                                    label = { Text(type.displayName) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    ),
                                )
                            }
                        }
                    }

                    // Cena (min - max)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Cena (zł)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            NumberFilterField(
                                value = filter.minPrice,
                                onValueChange = { onFilterChange(filter.copy(minPrice = it)) },
                                label = "Cena od",
                                modifier = Modifier.weight(1f),
                            )
                            NumberFilterField(
                                value = filter.maxPrice,
                                onValueChange = { onFilterChange(filter.copy(maxPrice = it)) },
                                label = "Cena do",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    // Wymiary (Długość, Głębokość / Szerokość, Wysokość)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Wymiary (cm)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        DimensionFilterRow(
                            label = "Długość",
                            minValue = filter.minLength,
                            onMinChange = { onFilterChange(filter.copy(minLength = it)) },
                            maxValue = filter.maxLength,
                            onMaxChange = { onFilterChange(filter.copy(maxLength = it)) },
                        )
                        DimensionFilterRow(
                            label = "Głęb. / Szer.",
                            minValue = filter.minDepth,
                            onMinChange = { onFilterChange(filter.copy(minDepth = it)) },
                            maxValue = filter.maxDepth,
                            onMaxChange = { onFilterChange(filter.copy(maxDepth = it)) },
                        )
                        DimensionFilterRow(
                            label = "Wysokość",
                            minValue = filter.minHeight,
                            onMinChange = { onFilterChange(filter.copy(minHeight = it)) },
                            maxValue = filter.maxHeight,
                            onMaxChange = { onFilterChange(filter.copy(maxHeight = it)) },
                        )
                    }

                    // Kolor, Tkanina, Materiał
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Materiały i kolory",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = filter.colour.orEmpty(),
                                onValueChange = { onFilterChange(filter.copy(colour = it.ifBlank { null })) },
                                label = { Text("Kolor") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = filter.fabric.orEmpty(),
                                onValueChange = { onFilterChange(filter.copy(fabric = it.ifBlank { null })) },
                                label = { Text("Tkanina") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.weight(1f),
                            )
                        }
                        OutlinedTextField(
                            value = filter.material.orEmpty(),
                            onValueChange = { onFilterChange(filter.copy(material = it.ifBlank { null })) },
                            label = { Text("Materiał") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (!filter.isEmpty) {
                        OutlinedButton(
                            onClick = { onFilterChange(ProductFilterData()) },
                            modifier = Modifier.fillMaxWidth(),
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

@Composable
fun NumberFilterField(
    value: Double?,
    onValueChange: (Double?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    var text by remember(value) { mutableStateOf(value?.let(::number).orEmpty()) }

    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            val parsed = input.trim().replace(',', '.').toDoubleOrNull()
            if (input.isBlank()) {
                onValueChange(null)
            } else if (parsed != null) {
                onValueChange(parsed)
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
        modifier = modifier,
    )
}

@Composable
fun DimensionFilterRow(
    label: String,
    minValue: Double?,
    onMinChange: (Double?) -> Unit,
    maxValue: Double?,
    onMaxChange: (Double?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(96.dp),
        )
        NumberFilterField(
            value = minValue,
            onValueChange = onMinChange,
            label = "Od",
            modifier = Modifier.weight(1f),
        )
        NumberFilterField(
            value = maxValue,
            onValueChange = onMaxChange,
            label = "Do",
            modifier = Modifier.weight(1f),
        )
    }
}
