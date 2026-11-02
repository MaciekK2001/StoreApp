package produkcja.meble.mobile.ui

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import produkcja.meble.mobile.FurnitureUiState
import produkcja.meble.mobile.FurnitureViewModel
import produkcja.meble.mobile.MainSection
import produkcja.meble.mobile.data.CreateOrderRequest
import produkcja.meble.mobile.data.CreateProductRequest
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.ui.auth.AuthScreen
import produkcja.meble.mobile.ui.common.ConfirmDeleteDialog
import produkcja.meble.mobile.ui.common.LoadingScreen
import produkcja.meble.mobile.ui.orders.OrderDetails
import produkcja.meble.mobile.ui.orders.OrderFormDialog
import produkcja.meble.mobile.ui.orders.OrderList
import produkcja.meble.mobile.ui.products.ProductFormDialog
import produkcja.meble.mobile.ui.products.ProductList
import produkcja.meble.mobile.ui.theme.orderSectionStyle
import produkcja.meble.mobile.ui.theme.productSectionStyle

@Composable
fun FurnitureApp(viewModel: FurnitureViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

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
                    onUpdateProduct = viewModel::updateProduct,
                    onDeleteProduct = viewModel::deleteProduct,
                    onCreateOrder = viewModel::createOrder,
                    onUpdateOrder = viewModel::updateOrder,
                    onDeleteOrder = viewModel::deleteOrder,
                    onToggleOrderStatus = viewModel::setOrderStatus,
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
    onCreateProduct: (CreateProductRequest, ByteArray?, String?, String?, (String) -> Unit, (String) -> Unit) -> Unit,
    onUpdateProduct: (Long, CreateProductRequest, ByteArray?, String?, String?, (String) -> Unit, (String) -> Unit) -> Unit,
    onDeleteProduct: (Product, (String) -> Unit, (String) -> Unit) -> Unit,
    onCreateOrder: (CreateOrderRequest, (String) -> Unit, (String) -> Unit) -> Unit,
    onUpdateOrder: (Long, CreateOrderRequest, (String) -> Unit, (String) -> Unit) -> Unit,
    onDeleteOrder: (FurnitureOrder, (String) -> Unit, (String) -> Unit) -> Unit,
    onToggleOrderStatus: (FurnitureOrder, Boolean, (String) -> Unit, (String) -> Unit) -> Unit,
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
        label = "top-bar-container",
    )
    val topBarContentColor by animateColorAsState(
        targetValue = activeSectionStyle.topBarContent,
        label = "top-bar-content",
    )
    val navBarContainerColor by animateColorAsState(
        targetValue = activeSectionStyle.topBarContainer,
        label = "nav-bar-container",
    )

    var showAddProductDialog by rememberSaveable { mutableStateOf(false) }
    var showCreateOrderDialog by rememberSaveable { mutableStateOf(false) }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    var orderToEdit by remember { mutableStateOf<FurnitureOrder?>(null) }
    var orderToDelete by remember { mutableStateOf<FurnitureOrder?>(null) }

    val handleToggleStatus: (FurnitureOrder, Boolean) -> Unit = { order, active ->
        onToggleOrderStatus(
            order,
            active,
            /* onSuccess = */ { msg ->
                coroutineScope.launch {
                    snackbar.showSnackbar(msg)
                }
            },
            /* onError = */ { err ->
                coroutineScope.launch {
                    snackbar.showSnackbar(err)
                }
            },
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = when {
                                selectedOrder != null -> Icons.AutoMirrored.Filled.ReceiptLong
                                state.section == MainSection.PRODUCTS -> Icons.Default.Chair
                                else -> Icons.AutoMirrored.Filled.ReceiptLong
                            },
                            contentDescription = null,
                            tint = activeSectionStyle.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Column {
                            Text(
                                text = when {
                                    selectedOrder != null -> "Zamówienie #${selectedOrder.id}"
                                    state.section == MainSection.PRODUCTS -> "Katalog mebli"
                                    else -> "Zamówienia klientów"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = topBarContentColor,
                            )
                            if (selectedOrder == null) {
                                Text(
                                    text = state.session?.user?.username?.let { "Użytkownik: $it" }.orEmpty(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = topBarContentColor.copy(alpha = 0.75f),
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (selectedOrder != null) {
                        IconButton(onClick = onCloseOrder) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Wróć",
                                tint = activeSectionStyle.primary,
                            )
                        }
                    }
                },
                actions = {
                    if (selectedOrder == null) {
                        IconButton(onClick = onRefresh, enabled = !state.loading) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Odśwież",
                                tint = activeSectionStyle.primary,
                            )
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Wyloguj",
                                tint = activeSectionStyle.primary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarContainerColor,
                    titleContentColor = topBarContentColor,
                    actionIconContentColor = activeSectionStyle.primary,
                    navigationIconContentColor = activeSectionStyle.primary,
                ),
            )
        },
        floatingActionButton = {
            if (selectedOrder == null) {
                when (state.section) {
                    MainSection.PRODUCTS -> {
                        FloatingActionButton(
                            onClick = { showAddProductDialog = true },
                            containerColor = productStyle.fabColor,
                            contentColor = productStyle.fabContentColor,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj mebel")
                        }
                    }
                    MainSection.ORDERS -> {
                        FloatingActionButton(
                            onClick = { showCreateOrderDialog = true },
                            containerColor = orderStyle.fabColor,
                            contentColor = orderStyle.fabContentColor,
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nowe zamówienie")
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (selectedOrder == null) {
                NavigationBar(
                    containerColor = navBarContainerColor,
                    tonalElevation = 6.dp,
                ) {
                    NavigationBarItem(
                        selected = state.section == MainSection.PRODUCTS,
                        onClick = { onSectionSelected(MainSection.PRODUCTS) },
                        icon = { Icon(Icons.Default.Chair, null) },
                        label = {
                            Text(
                                "Produkty",
                                fontWeight = if (state.section == MainSection.PRODUCTS) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = productStyle.primary,
                            selectedTextColor = productStyle.primary,
                            indicatorColor = productStyle.navIndicatorColor,
                            unselectedIconColor = activeSectionStyle.primary.copy(alpha = 0.5f),
                            unselectedTextColor = activeSectionStyle.primary.copy(alpha = 0.7f),
                        ),
                    )
                    NavigationBarItem(
                        selected = state.section == MainSection.ORDERS,
                        onClick = { onSectionSelected(MainSection.ORDERS) },
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) },
                        label = {
                            Text(
                                "Zamówienia",
                                fontWeight = if (state.section == MainSection.ORDERS) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = orderStyle.primary,
                            selectedTextColor = orderStyle.primary,
                            indicatorColor = orderStyle.navIndicatorColor,
                            unselectedIconColor = activeSectionStyle.primary.copy(alpha = 0.5f),
                            unselectedTextColor = activeSectionStyle.primary.copy(alpha = 0.7f),
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AnimatedContent(
                targetState = selectedOrder,
                label = "order-details",
            ) { order ->
                if (order != null) {
                    OrderDetails(
                        order = order,
                        onEditOrder = { orderToEdit = it },
                        onDeleteOrder = { orderToDelete = it },
                        onToggleStatus = handleToggleStatus,
                    )
                } else {
                    when (state.section) {
                        MainSection.PRODUCTS -> ProductList(
                            products = state.products,
                            onEditProduct = { productToEdit = it },
                            onDeleteProduct = { productToDelete = it },
                        )
                        MainSection.ORDERS -> OrderList(
                            orders = state.orders,
                            showOnlyActive = state.showOnlyActiveOrders,
                            onToggleShowActive = onToggleShowActiveOrders,
                            onOrderSelected = onOrderSelected,
                            onEditOrder = { orderToEdit = it },
                            onDeleteOrder = { orderToDelete = it },
                            onToggleStatus = handleToggleStatus,
                        )
                    }
                }
            }
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .size(28.dp),
                )
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        ProductFormDialog(
            initialProduct = null,
            onDismiss = { showAddProductDialog = false },
            onSave = { request, imageBytes, fileName, mimeType, onDialogSuccess, onDialogError ->
                onCreateProduct(
                    request,
                    imageBytes,
                    fileName,
                    mimeType,
                    /* onSuccess = */ { successMessage ->
                        onDialogSuccess(successMessage)
                        showAddProductDialog = false
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
            onDismiss = { productToEdit = null },
            onSave = { request, imageBytes, fileName, mimeType, onDialogSuccess, onDialogError ->
                onUpdateProduct(
                    product.id,
                    request,
                    imageBytes,
                    fileName,
                    mimeType,
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
            title = "Usunąć mebel?",
            message = "Czy na pewno chcesz usunąć „${product.model}”? Tej operacji nie można cofnąć.",
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
            title = "Usunąć zamówienie?",
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
}
