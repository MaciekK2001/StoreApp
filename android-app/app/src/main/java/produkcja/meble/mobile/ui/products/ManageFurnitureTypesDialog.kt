package produkcja.meble.mobile.ui.products

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import produkcja.meble.mobile.data.FurnitureTypeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFurnitureTypesDialog(
    types: List<FurnitureTypeItem>,
    onDismiss: () -> Unit,
    onCreateType: (name: String, onSuccess: (FurnitureTypeItem) -> Unit, onError: (String) -> Unit) -> Unit,
    onDeleteType: (id: Long, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onTypeSelected: ((FurnitureTypeItem) -> Unit)? = null,
    onSeedDefaults: ((onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit)? = null,
) {
    BackHandler { onDismiss() }

    var newTypeName by rememberSaveable { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Typy mebli",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zamknij")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Zarządzaj dostępnymi typami (kategoriami) mebli w katalogu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Error / Success banners
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = successMessage != null) {
                    successMessage?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                // Add Type Form
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = "Dodaj nowy typ mebla",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = newTypeName,
                                onValueChange = {
                                    newTypeName = it
                                    errorMessage = null
                                    successMessage = null
                                },
                                label = { Text("Nazwa typu") },
                                placeholder = { Text("np. Puf, Toaletka") },
                                singleLine = true,
                                enabled = !isProcessing,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (newTypeName.isNotBlank() && !isProcessing) {
                                            isProcessing = true
                                            errorMessage = null
                                            successMessage = null
                                            onCreateType(
                                                newTypeName.trim(),
                                                { created ->
                                                    isProcessing = false
                                                    newTypeName = ""
                                                    successMessage = "Dodano typ \"${created.name}\""
                                                    onTypeSelected?.invoke(created)
                                                },
                                                { err ->
                                                    isProcessing = false
                                                    errorMessage = err
                                                },
                                            )
                                        }
                                    },
                                ),
                                modifier = Modifier.weight(1f),
                            )

                            Button(
                                onClick = {
                                    if (newTypeName.isNotBlank() && !isProcessing) {
                                        isProcessing = true
                                        errorMessage = null
                                        successMessage = null
                                        onCreateType(
                                            newTypeName.trim(),
                                            { created ->
                                                isProcessing = false
                                                newTypeName = ""
                                                successMessage = "Dodano typ \"${created.name}\""
                                                onTypeSelected?.invoke(created)
                                            },
                                            { err ->
                                                isProcessing = false
                                                errorMessage = err
                                            },
                                        )
                                    }
                                },
                                enabled = newTypeName.isNotBlank() && !isProcessing,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                } else {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dodaj")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Dostępne typy (${types.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // List of types
                if (types.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Brak zdefiniowanych typów w bazie danych.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (onSeedDefaults != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    isProcessing = true
                                    errorMessage = null
                                    successMessage = null
                                    onSeedDefaults(
                                        {
                                            isProcessing = false
                                            successMessage = "Załadowano standardowe kategorie mebli."
                                        },
                                        { err ->
                                            isProcessing = false
                                            errorMessage = err
                                        },
                                    )
                                },
                                enabled = !isProcessing,
                            ) {
                                Text("Dodaj standardowe kategorie")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        items(types, key = { it.id }) { item ->
                            var isDeletingThis by remember { mutableStateOf(false) }

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f),
                                    )

                                    if (isDeletingThis) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    } else {
                                        IconButton(
                                            onClick = {
                                                isDeletingThis = true
                                                errorMessage = null
                                                successMessage = null
                                                onDeleteType(
                                                    item.id,
                                                    {
                                                        isDeletingThis = false
                                                        successMessage = "Usunięto typ \"${item.name}\""
                                                    },
                                                    { err ->
                                                        isDeletingThis = false
                                                        errorMessage = err
                                                    },
                                                )
                                            },
                                            enabled = !isProcessing,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Usuń ${item.name}",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Gotowe")
                }
            }
        }
    }
}
