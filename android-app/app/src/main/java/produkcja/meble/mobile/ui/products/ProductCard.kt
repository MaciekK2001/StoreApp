package produkcja.meble.mobile.ui.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import produkcja.meble.mobile.data.FurnitureType
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.ui.common.DetailRow
import produkcja.meble.mobile.ui.common.isMeaningful
import produkcja.meble.mobile.ui.common.money
import produkcja.meble.mobile.ui.common.number
import produkcja.meble.mobile.ui.common.resolveImageUrl

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onImageClick: ((url: String, title: String) -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    val displayModel = product.model.ifBlank { "Mebel #${product.id}" }
    val hasImage = product.imageUrl.isMeaningful()
    var isActionsExpanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        if (hasImage) {
            val context = LocalContext.current
            val resolvedUrl = remember(product.imageUrl) {
                resolveImageUrl(product.imageUrl) ?: product.imageUrl!!
            }
            val imageRequest = remember(resolvedUrl) {
                ImageRequest.Builder(context)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .build()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(enabled = onImageClick != null) {
                        onImageClick?.invoke(resolvedUrl, displayModel)
                    },
                contentAlignment = Alignment.Center,
            ) {
                SubcomposeAsyncImage(
                    model = imageRequest,
                    contentDescription = "Zdjęcie mebla: $displayModel",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    },
                    error = {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ImageNotSupported,
                                contentDescription = "Nie udało się załadować zdjęcia",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(40.dp),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Brak podglądu",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            )
                        }
                    },
                )

                Surface(
                    shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Text(
                        text = "Kliknij, aby powiększyć",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayModel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = money(product.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (onEditClick != null || onDeleteClick != null) {
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

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text(
                    text = FurnitureType.from(product.type)?.displayName ?: product.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }

            Spacer(Modifier.height(10.dp))
            DetailRow("Wymiary", "${number(product.length)} × ${number(product.depth)} × ${number(product.height)} cm")
            DetailRow("Kolor", product.colour)
            DetailRow("Materiał", product.material)
            if (product.fabric.isMeaningful()) {
                DetailRow("Tkanina", product.fabric)
            }
            if (product.equipment.isMeaningful()) {
                DetailRow("Wyposażenie", product.equipment)
            }
            if (product.remarks.isMeaningful()) {
                DetailRow("Uwagi", product.remarks)
            }

            // Rozsuwany panel akcji zarządzania produktem
            if (onEditClick != null || onDeleteClick != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                )

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
                        if (onEditClick != null) {
                            OutlinedButton(
                                onClick = onEditClick,
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Edytuj")
                            }
                        }
                        if (onDeleteClick != null) {
                            OutlinedButton(
                                onClick = onDeleteClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Usuń")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProductCardPreview() {
    ProductCard(
        product = Product(
            id = 1,
            price = 1499.0,
            length = 200.0,
            depth = 90.0,
            height = 85.0,
            colour = "Szary",
            model = "Sofa Oslo",
            type = "SOFA",
            material = "Drewno sosnowe, pianka HR",
            fabric = "Monolith 84",
            remarks = "Z funkcją spania",
            equipment = "Pojemnik na pościel, 2 poduszki",
            imageUrl = null,
        ),
    )
}
