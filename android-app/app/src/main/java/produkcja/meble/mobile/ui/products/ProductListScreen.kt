package produkcja.meble.mobile.ui.products

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import produkcja.meble.mobile.data.FurnitureType
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.data.ProductFilter
import produkcja.meble.mobile.ui.common.EmptyState
import produkcja.meble.mobile.ui.common.FullScreenImageDialog
import produkcja.meble.mobile.ui.theme.MebleMobileTheme

private data class FullScreenImageData(
    val imageUrl: String,
    val title: String,
)

@Composable
fun ProductList(
    products: List<Product>,
    onEditProduct: ((Product) -> Unit)? = null,
    onDeleteProduct: ((Product) -> Unit)? = null,
) {
    var activeFilter by remember { mutableStateOf(ProductFilter()) }
    var fullScreenImage by remember { mutableStateOf<FullScreenImageData?>(null) }

    val filteredProducts = remember(products, activeFilter) {
        if (activeFilter.isEmpty) {
            products
        } else {
            products.filter { product ->
                val matchesType = activeFilter.type.isEmpty() || activeFilter.type.any { selectedType ->
                    val productType = FurnitureType.from(product.type)
                    if (productType != null) {
                        productType.displayName.equals(selectedType, ignoreCase = true) ||
                            productType.name.equals(selectedType, ignoreCase = true) ||
                            product.type.contains(selectedType, ignoreCase = true)
                    } else {
                        product.type.contains(selectedType, ignoreCase = true)
                    }
                }
                val matchesMinPrice = activeFilter.minPrice == null || product.price >= activeFilter.minPrice!!
                val matchesMaxPrice = activeFilter.maxPrice == null || product.price <= activeFilter.maxPrice!!

                val matchesMinLength = activeFilter.minLength == null || product.length >= activeFilter.minLength!!
                val matchesMaxLength = activeFilter.maxLength == null || product.length <= activeFilter.maxLength!!

                val matchesMinDepth = activeFilter.minDepth == null || product.depth >= activeFilter.minDepth!!
                val matchesMaxDepth = activeFilter.maxDepth == null || product.depth <= activeFilter.maxDepth!!

                val matchesMinHeight = activeFilter.minHeight == null || product.height >= activeFilter.minHeight!!
                val matchesMaxHeight = activeFilter.maxHeight == null || product.height <= activeFilter.maxHeight!!

                val matchesModel = activeFilter.model.isNullOrBlank() || run {
                    val query = activeFilter.model!!.trim()
                    val displayType = FurnitureType.displayLabel(product.type)
                    product.model.contains(query, ignoreCase = true) ||
                        product.type.contains(query, ignoreCase = true) ||
                        displayType.contains(query, ignoreCase = true) ||
                        product.remarks.contains(query, ignoreCase = true) ||
                        product.equipment.contains(query, ignoreCase = true)
                }
                val matchesColour = activeFilter.colour.isNullOrBlank() || product.colour.contains(activeFilter.colour!!, ignoreCase = true)
                val matchesMaterial = activeFilter.material.isNullOrBlank() || product.material.contains(activeFilter.material!!, ignoreCase = true)
                val matchesFabric = activeFilter.fabric.isNullOrBlank() || product.fabric.contains(activeFilter.fabric!!, ignoreCase = true)

                matchesType && matchesMinPrice && matchesMaxPrice &&
                    matchesMinLength && matchesMaxLength &&
                    matchesMinDepth && matchesMaxDepth &&
                    matchesMinHeight && matchesMaxHeight &&
                    matchesModel && matchesColour && matchesMaterial && matchesFabric
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "product_filter") {
            ProductFilter(
                filter = activeFilter,
                onFilterChange = { activeFilter = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
        }

        if (products.isEmpty()) {
            item(key = "empty_products") {
                EmptyState(
                    title = "Brak produktów",
                    message = "Kliknij przycisk +, aby dodać pierwszy mebel.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                )
            }
        } else if (filteredProducts.isEmpty()) {
            item(key = "empty_filtered") {
                EmptyState(
                    title = "Brak wyników",
                    message = "Żaden produkt nie pasuje do wybranych filtrów.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                )
            }
        } else {
            item(key = "products_count") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (activeFilter.isEmpty) {
                            "Wszystkie produkty (${products.size})"
                        } else {
                            "Wyniki filtrowania: ${filteredProducts.size} z ${products.size}"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(filteredProducts, key = Product::id) { product ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ProductCard(
                        product = product,
                        onImageClick = { imageUrl, title ->
                            fullScreenImage = FullScreenImageData(imageUrl = imageUrl, title = title)
                        },
                        onEditClick = onEditProduct?.let { { it(product) } },
                        onDeleteClick = onDeleteProduct?.let { { it(product) } },
                    )
                }
            }
        }
    }

    fullScreenImage?.let { data ->
        FullScreenImageDialog(
            imageUrl = data.imageUrl,
            title = data.title,
            onDismiss = { fullScreenImage = null },
        )
    }
}

@Preview(name = "Lista produktów - Jasny motyw", showBackground = true)
@Preview(name = "Lista produktów - Ciemny motyw", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ProductListPreview() {
    MebleMobileTheme {
        Surface {
            ProductList(
                products = listOf(
                    Product(
                        id = 1L,
                        price = 2899.0,
                        length = 220.0,
                        depth = 95.0,
                        height = 88.0,
                        colour = "Butelkowa zieleń",
                        model = "Sofa Chesterfield",
                        type = "Sofa 3-osobowa",
                        material = "Drewno bukowe, pianka HR",
                        fabric = "Velvet Riviera 38",
                        remarks = "Nóżki w kolorze złotym",
                        equipment = "2 poduszki dekoracyjne gratis",
                    ),
                    Product(
                        id = 2L,
                        price = 1450.0,
                        length = 90.0,
                        depth = 85.0,
                        height = 105.0,
                        colour = "Musztardowy",
                        model = "Fotel Uszak",
                        type = "Fotel wypoczynkowy",
                        material = "Drewno dębowe, sprężyny faliste",
                        fabric = "Plecionka Sawana",
                        remarks = "Pikowane oparcie",
                        equipment = "Podnóżek w zestawie",
                    ),
                ),
            )
        }
    }
}
