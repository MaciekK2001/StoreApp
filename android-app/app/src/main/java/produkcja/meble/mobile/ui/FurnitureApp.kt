package produkcja.meble.mobile.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import produkcja.meble.mobile.data.FurnitureTypeItem
import produkcja.meble.mobile.ui.products.ManageFurnitureTypesDialog
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import produkcja.meble.mobile.FurnitureUiState
import produkcja.meble.mobile.FurnitureViewModel
import produkcja.meble.mobile.MainSection
import produkcja.meble.mobile.data.CreateOrderRequest
import produkcja.meble.mobile.data.CreateProductRequest
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.OrderStatus
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.ui.auth.AuthScreen
import produkcja.meble.mobile.ui.common.ConfirmDeleteDialog
import produkcja.meble.mobile.ui.common.LoadingScreen
import produkcja.meble.mobile.ui.orders.OrderDetails
import produkcja.meble.mobile.ui.orders.OrderFormDialog
import produkcja.meble.mobile.ui.orders.OrderList
import produkcja.meble.mobile.ui.products.ProductFormDialog
import produkcja.meble.mobile.ui.products.ProductList
import produkcja.meble.mobile.ui.theme.*

@Composable
fun FurnitureApp(viewModel: FurnitureViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var previousSession by remember { mutableStateOf(state.session) }
    LaunchedEffect(state.session) {
        if (previousSession == null && state.session != null) {
            snackbar.showSnackbar("Zalogowano pomyślnie")
        }
        previousSession = state.session
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                !state.initialized -> LoadingScreen()
                state.session == null -> AuthScreen(
                    loading = state.loading,
                    snackbar = snackbar,
                    serverUrl = state.serverUrl,
                    onLogin = viewModel::login,
                    onUpdateServerUrl = viewModel::updateServerUrl,
                    onResetServerUrl = viewModel::resetServerUrl,
                )
                else -> Dashboard(
                    state = state,
                    snackbar = snackbar,
                    onSectionSelected = viewModel::selectSection,
                    onOrderSelected = viewModel::selectOrder,
                    onCloseOrder = viewModel::closeOrder,
                    onRefresh = viewModel::refresh,
                    onLogout = viewModel::logout,
                    onCreateProduct = viewModel::createProduct,
                    onCreateProductWithResult = { req, bytes, name, mime, extras, onCreated, onErr ->
                        viewModel.createProduct(
                            request = req,
                            imageBytes = bytes,
                            fileName = name,
                            contentType = mime,
                            additionalImagesBytes = extras,
                            onProductCreated = onCreated,
                            onSuccess = { msg ->
                                coroutineScope.launch { snackbar.showSnackbar(msg) }
                            },
                            onError = onErr,
                        )
                    },
                    onUpdateProduct = viewModel::updateProduct,
                    onDeleteProduct = viewModel::deleteProduct,
                    onCreateOrder = viewModel::createOrder,
                    onUpdateOrder = viewModel::updateOrder,
                    onDeleteOrder = viewModel::deleteOrder,
                    onToggleOrderStatus = viewModel::setOrderStatus,
                    onUpdateOrderStatus = { order, status ->
                        viewModel.setOrderStatus(
                            order = order,
                            status = status,
                            onSuccess = { msg ->
                                coroutineScope.launch { snackbar.showSnackbar(msg) }
                            },
                            onError = { msg ->
                                coroutineScope.launch { snackbar.showSnackbar(msg) }
                            },
                        )
                    },
                    onCreateFurnitureType = viewModel::createFurnitureType,
                    onDeleteFurnitureType = viewModel::deleteFurnitureType,
                    onSeedDefaultFurnitureTypes = viewModel::seedDefaultFurnitureTypes,
                    onSelectOrderStatusFilter = viewModel::setSelectedOrderStatusFilter,
                    onToggleShowActiveOrders = viewModel::setShowOnlyActiveOrders,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dashboard(
    state: FurnitureUiState,
    snackbar: SnackbarHostState,
    onSectionSelected: (MainSection) -> Unit,
    onOrderSelected: (FurnitureOrder) -> Unit,
    onCloseOrder: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onCreateProduct: (CreateProductRequest, ByteArray?, String?, String?, List<ByteArray>, (String) -> Unit, (String) -> Unit) -> Unit,
    onCreateProductWithResult: (CreateProductRequest, ByteArray?, String?, String?, List<ByteArray>, (Product) -> Unit, (String) -> Unit) -> Unit,
    onUpdateProduct: (Long, CreateProductRequest, ByteArray?, String?, String?, List<ByteArray>, (String) -> Unit, (String) -> Unit) -> Unit,
    onDeleteProduct: (Product, (String) -> Unit, (String) -> Unit) -> Unit,
    onCreateOrder: (CreateOrderRequest, (String) -> Unit, (String) -> Unit) -> Unit,
    onUpdateOrder: (Long, CreateOrderRequest, (String) -> Unit, (String) -> Unit) -> Unit,
    onDeleteOrder: (FurnitureOrder, (String) -> Unit, (String) -> Unit) -> Unit,
    onToggleOrderStatus: (FurnitureOrder, Boolean, (String) -> Unit, (String) -> Unit) -> Unit,
    onUpdateOrderStatus: (FurnitureOrder, OrderStatus) -> Unit,
    onCreateFurnitureType: (String, (FurnitureTypeItem) -> Unit, (String) -> Unit) -> Unit,
    onDeleteFurnitureType: (Long, () -> Unit, (String) -> Unit) -> Unit,
    onSeedDefaultFurnitureTypes: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
    onSelectOrderStatusFilter: (OrderStatus?) -> Unit,
    onToggleShowActiveOrders: (Boolean) -> Unit,
) {
    val selectedOrder = state.selectedOrder
    val coroutineScope = rememberCoroutineScope()

    val isDark = isSystemInDarkTheme()
    val productStyle = productSectionStyle(isDark)
    val orderStyle = orderSectionStyle(isDark)
    val activeSectionStyle = when {
        selectedOrder != null -> orderStyle
        state.section == MainSection.ORDERS -> orderStyle
        else -> productStyle
    }

    val topBarContainerColor by animateColorAsState(
        targetValue = activeSectionStyle.topBarContainer,
        label = "TopBarContainerColor",
    )
    val topBarTitleColor by animateColorAsState(
        targetValue = activeSectionStyle.topBarTitle,
        label = "TopBarTitleColor",
    )
    val topBarActionColor by animateColorAsState(
        targetValue = activeSectionStyle.topBarAction,
        label = "TopBarActionColor",
    )
    val fabContainerColor by animateColorAsState(
        targetValue = activeSectionStyle.fabContainer,
        label = "FabContainerColor",
    )
    val fabContentColor by animateColorAsState(
        targetValue = activeSectionStyle.fabContent,
        label = "FabContentColor",
    )

    // Dialog states
    var showManageFurnitureTypesDialog by rememberSaveable { mutableStateOf(false) }
    var showCreateProductDialog by rememberSaveable { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    var showCreateOrderDialog by rememberSaveable { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<FurnitureOrder?>(null) }
    var orderToDelete by remember { mutableStateOf<FurnitureOrder?>(null) }

    // Hierarchical back navigation handling
    BackHandler(enabled = showManageFurnitureTypesDialog) {
        showManageFurnitureTypesDialog = false
    }
    BackHandler(enabled = orderToDelete != null) {
        orderToDelete = null
    }
    BackHandler(enabled = productToDelete != null) {
        productToDelete = null
    }
    BackHandler(enabled = orderToEdit != null) {
        orderToEdit = null
    }
    BackHandler(enabled = showCreateOrderDialog) {
        showCreateOrderDialog = false
    }
    BackHandler(enabled = productToEdit != null) {
        productToEdit = null
    }
    BackHandler(enabled = showCreateProductDialog) {
        showCreateProductDialog = false
    }
    BackHandler(enabled = selectedOrder != null) {
        onCloseOrder()
    }
    BackHandler(enabled = selectedOrder == null && state.section == MainSection.PRODUCTS) {
        onSectionSelected(MainSection.ORDERS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.loading) {
                        Column {
                            Text(
                                text = when {
                                    selectedOrder != null -> "Szczegóły zamówienia #${selectedOrder.id}"
                                    state.section == MainSection.PRODUCTS -> "Katalog mebli"
                                    else -> "Zamówienia"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = topBarTitleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "Synchronizacja danych...",
                                style = MaterialTheme.typography.bodySmall,
                                color = topBarActionColor.copy(alpha = 0.8f),
                                maxLines = 1,
                            )
                        }
                    } else {
                        Text(
                            text = when {
                                selectedOrder != null -> "Szczegóły zamówienia #${selectedOrder.id}"
                                state.section == MainSection.PRODUCTS -> "Katalog mebli"
                                else -> "Zamówienia"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = topBarTitleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                navigationIcon = {
                    if (selectedOrder != null) {
                        IconButton(onClick = onCloseOrder) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Wróć do listy zamówień",
                                tint = topBarActionColor,
                            )
                        }
                    }
                },
                actions = {

                    if (state.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(22.dp)
                                .padding(end = 4.dp),
                            color = topBarActionColor,
                            strokeWidth = 2.dp,
                        )
                    }

                    IconButton(
                        onClick = onRefresh,
                        enabled = !state.loading,
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Odśwież dane",
                            tint = topBarActionColor,
                        )
                    }

                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Wyloguj się",
                            tint = topBarActionColor,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor,
                ),
            )
        },
        bottomBar = {
            if (selectedOrder == null) {
                val navBarContainerColor by animateColorAsState(
                    targetValue = activeSectionStyle.navBarContainer,
                    label = "NavBarContainerColor",
                )
                val navBarIndicatorColor by animateColorAsState(
                    targetValue = activeSectionStyle.navBarIndicator,
                    label = "NavBarIndicatorColor",
                )
                val navBarSelectedItemColor by animateColorAsState(
                    targetValue = activeSectionStyle.navBarSelectedItem,
                    label = "NavBarSelectedItemColor",
                )
                val navBarUnselectedItemColor by animateColorAsState(
                    targetValue = activeSectionStyle.navBarUnselectedItem,
                    label = "NavBarUnselectedItemColor",
                )

                val bottomNavColors = NavigationBarItemDefaults.colors(
                    indicatorColor = navBarIndicatorColor,
                    selectedIconColor = navBarSelectedItemColor,
                    selectedTextColor = navBarSelectedItemColor,
                    unselectedIconColor = navBarUnselectedItemColor,
                    unselectedTextColor = navBarUnselectedItemColor,
                )

                NavigationBar(
                    containerColor = navBarContainerColor,
                ) {
                    NavigationBarItem(
                        selected = state.section == MainSection.PRODUCTS,
                        onClick = { onSectionSelected(MainSection.PRODUCTS) },
                        icon = { Icon(Icons.Default.Chair, contentDescription = null) },
                        label = { Text("Produkty") },
                        colors = bottomNavColors,
                    )
                    NavigationBarItem(
                        selected = state.section == MainSection.ORDERS,
                        onClick = { onSectionSelected(MainSection.ORDERS) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                        label = { Text("Zamówienia") },
                        colors = bottomNavColors,
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedOrder == null) {
                FloatingActionButton(
                    onClick = {
                        if (state.section == MainSection.PRODUCTS) {
                            showCreateProductDialog = true
                        } else {
                            showCreateOrderDialog = true
                        }
                    },
                    containerColor = fabContainerColor,
                    contentColor = fabContentColor,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (state.section == MainSection.PRODUCTS) {
                            "Dodaj nowy mebel"
                        } else {
                            "Utwórz nowe zamówienie"
                        },
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbar) },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AnimatedContent(
                targetState = when {
                    selectedOrder != null -> "order_details"
                    state.section == MainSection.PRODUCTS -> "products"
                    else -> "orders"
                },
                label = "MainContentTransition",
            ) { target ->
                when (target) {
                    "order_details" -> {
                        selectedOrder?.let { order ->
                            OrderDetails(
                                order = order,
                                onEditOrder = { orderToEdit = it },
                                onDeleteOrder = { orderToDelete = it },
                                onUpdateOrderStatus = onUpdateOrderStatus,
                                onToggleStatus = { o, active ->
                                    onToggleOrderStatus(
                                        o,
                                        active,
                                        /* onSuccess = */ { successMessage ->
                                            coroutineScope.launch {
                                                snackbar.showSnackbar(successMessage)
                                            }
                                        },
                                        /* onError = */ { errorMessage ->
                                            coroutineScope.launch {
                                                snackbar.showSnackbar(errorMessage)
                                            }
                                        },
                                    )
                                },
                            )
                        }
                    }

                    "products" -> {
                        ProductList(
                            products = state.products,
                            furnitureTypes = state.furnitureTypes,
                            onEditProduct = { productToEdit = it },
                            onDeleteProduct = { productToDelete = it },
                            onManageFurnitureTypes = { showManageFurnitureTypesDialog = true },
                        )
                    }

                    "orders" -> {
                        OrderList(
                            orders = state.orders,
                            selectedStatus = state.selectedOrderStatusFilter,
                            onSelectStatus = onSelectOrderStatusFilter,
                            showOnlyActive = state.showOnlyActiveOrders,
                            onToggleShowActive = onToggleShowActiveOrders,
                            onOrderSelected = onOrderSelected,
                            onEditOrder = { orderToEdit = it },
                            onDeleteOrder = { orderToDelete = it },
                            onUpdateOrderStatus = onUpdateOrderStatus,
                            onToggleStatus = { order, active ->
                                onToggleOrderStatus(
                                    order,
                                    active,
                                    /* onSuccess = */ { successMessage ->
                                        coroutineScope.launch {
                                            snackbar.showSnackbar(successMessage)
                                        }
                                    },
                                    /* onError = */ { errorMessage ->
                                        coroutineScope.launch {
                                            snackbar.showSnackbar(errorMessage)
                                        }
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    // Create Product Dialog
    if (showCreateProductDialog) {
        ProductFormDialog(
            initialProduct = null,
            existingProducts = state.products,
            furnitureTypes = state.furnitureTypes,
            onCreateType = onCreateFurnitureType,
            onDeleteType = onDeleteFurnitureType,
            onDismiss = { showCreateProductDialog = false },
            onSave = { request, imageBytes, fileName, mimeType, additionalImagesBytes, onDialogSuccess, onDialogError ->
                onCreateProduct(
                    request,
                    imageBytes,
                    fileName,
                    mimeType,
                    additionalImagesBytes,
                    /* onSuccess = */ { successMessage ->
                        onDialogSuccess(successMessage)
                        showCreateProductDialog = false
                        coroutineScope.launch {
                            snackbar.showSnackbar(successMessage)
                        }
                    },
                    /* onError = */ { errorMessage ->
                        onDialogError(errorMessage)
                    },
                )
            },
        )
    }

    // Edit Product Dialog
    productToEdit?.let { product ->
        ProductFormDialog(
            initialProduct = product,
            existingProducts = state.products,
            furnitureTypes = state.furnitureTypes,
            onCreateType = onCreateFurnitureType,
            onDeleteType = onDeleteFurnitureType,
            onSeedDefaults = onSeedDefaultFurnitureTypes,
            onDismiss = { productToEdit = null },
            onSave = { request, imageBytes, fileName, mimeType, additionalImagesBytes, onDialogSuccess, onDialogError ->
                onUpdateProduct(
                    product.id,
                    request,
                    imageBytes,
                    fileName,
                    mimeType,
                    additionalImagesBytes,
                    /* onSuccess = */ { successMessage ->
                        onDialogSuccess(successMessage)
                        productToEdit = null
                        coroutineScope.launch {
                            snackbar.showSnackbar(successMessage)
                        }
                    },
                    /* onError = */ { errorMessage ->
                        onDialogError(errorMessage)
                    },
                )
            },
        )
    }

    // Delete Product Confirmation Dialog
    productToDelete?.let { product ->
        ConfirmDeleteDialog(
            title = "Usuń produkt?",
            message = "Czy na pewno chcesz usunąć produkt „${product.model}”? Tej operacji nie można cofnąć.",
            confirmButtonText = "Usuń",
            onConfirm = {
                onDeleteProduct(
                    product,
                    /* onSuccess = */ { successMessage ->
                        productToDelete = null
                        coroutineScope.launch {
                            snackbar.showSnackbar(successMessage)
                        }
                    },
                    /* onError = */ { errorMessage ->
                        productToDelete = null
                        coroutineScope.launch {
                            snackbar.showSnackbar(errorMessage)
                        }
                    },
                )
            },
            onDismiss = { productToDelete = null },
        )
    }

    // Create Order Dialog
    if (showCreateOrderDialog) {
        OrderFormDialog(
            products = state.products,
            initialOrder = null,
            onDismiss = { showCreateOrderDialog = false },
            onCreateProduct = onCreateProductWithResult,
            onSave = { request, onDialogSuccess, onDialogError ->
                onCreateOrder(
                    request,
                    /* onSuccess = */ { successMessage ->
                        onDialogSuccess(successMessage)
                        showCreateOrderDialog = false
                        coroutineScope.launch {
                            snackbar.showSnackbar(successMessage)
                        }
                    },
                    /* onError = */ { errorMessage ->
                        onDialogError(errorMessage)
                    },
                )
            },
        )
    }

    // Edit Order Dialog
    orderToEdit?.let { order ->
        OrderFormDialog(
            products = state.products,
            initialOrder = order,
            onDismiss = { orderToEdit = null },
            onCreateProduct = onCreateProductWithResult,
            onSave = { request, onDialogSuccess, onDialogError ->
                onUpdateOrder(
                    order.id,
                    request,
                    /* onSuccess = */ { successMessage ->
                        onDialogSuccess(successMessage)
                        orderToEdit = null
                        coroutineScope.launch {
                            snackbar.showSnackbar(successMessage)
                        }
                    },
                    /* onError = */ { errorMessage ->
                        onDialogError(errorMessage)
                    },
                )
            },
        )
    }

    // Delete Order Confirmation Dialog
    orderToDelete?.let { order ->
        ConfirmDeleteDialog(
            title = "Usuń zamówienie?",
            message = "Czy na pewno chcesz usunąć zamówienie #${order.id} dla klienta pod adresem „${order.address}”? Zamówienie zostanie trwale usunięte.",
            confirmButtonText = "Usuń",
            onConfirm = {
                onDeleteOrder(
                    order,
                    /* onSuccess = */ { successMessage ->
                        orderToDelete = null
                        coroutineScope.launch {
                            snackbar.showSnackbar(successMessage)
                        }
                    },
                    /* onError = */ { errorMessage ->
                        orderToDelete = null
                        coroutineScope.launch {
                            snackbar.showSnackbar(errorMessage)
                        }
                    },
                )
            },
            onDismiss = { orderToDelete = null },
        )
    }

    if (showManageFurnitureTypesDialog) {
        ManageFurnitureTypesDialog(
            types = state.furnitureTypes,
            onDismiss = { showManageFurnitureTypesDialog = false },
            onCreateType = onCreateFurnitureType,
            onDeleteType = onDeleteFurnitureType,
            onSeedDefaults = onSeedDefaultFurnitureTypes,
        )
    }
}
