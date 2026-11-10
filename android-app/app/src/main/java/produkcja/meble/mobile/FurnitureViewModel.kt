package produkcja.meble.mobile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import produkcja.meble.mobile.data.ApiClient
import produkcja.meble.mobile.data.ApiException
import produkcja.meble.mobile.data.AuthSession
import produkcja.meble.mobile.data.CreateOrderRequest
import produkcja.meble.mobile.data.CreateProductRequest
import produkcja.meble.mobile.data.FurnitureOrder
import produkcja.meble.mobile.data.FurnitureTypeItem
import produkcja.meble.mobile.data.LoginRequest
import produkcja.meble.mobile.data.OrderStatus
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.data.SessionStore
import produkcja.meble.mobile.ui.common.ServerConfig
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

enum class MainSection {
    PRODUCTS,
    ORDERS,
}

data class FurnitureUiState(
    val initialized: Boolean = false,
    val loading: Boolean = false,
    val session: AuthSession? = null,
    val section: MainSection = MainSection.PRODUCTS,
    val products: List<Product> = emptyList(),
    val furnitureTypes: List<FurnitureTypeItem> = emptyList(),
    val orders: List<FurnitureOrder> = emptyList(),
    val selectedOrder: FurnitureOrder? = null,
    val error: String? = null,
    val showOnlyActiveOrders: Boolean = true,
    val selectedOrderStatusFilter: OrderStatus? = OrderStatus.ACTIVE,
    val serverUrl: String = BuildConfig.API_BASE_URL,
)

class FurnitureViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionStore = SessionStore(application)
    private var currentServerUrl = sessionStore.getServerUrl()
    private var api = ApiClient(currentServerUrl)
    private val _uiState = MutableStateFlow(
        FurnitureUiState(
            session = sessionStore.load(),
            serverUrl = currentServerUrl,
        )
    )

    val uiState: StateFlow<FurnitureUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val session = _uiState.value.session
            if (session != null) {
                try {
                    _uiState.update { it.copy(loading = true, error = null) }
                    loadDashboard(session)
                } catch (exception: Exception) {
                    Log.e("FurnitureViewModel", "Błąd ładowania pulpitu", exception)
                    handleAuthorizedError(exception)
                }
            } else {
                _uiState.update { it.copy(initialized = true) }
            }
        }
    }

    fun updateServerUrl(rawUrl: String) {
        val trimmed = rawUrl.trim()
        val sanitized = if (trimmed.endsWith("/")) trimmed else "$trimmed/"
        sessionStore.setServerUrl(sanitized)
        ServerConfig.baseUrl = sanitized
        currentServerUrl = sanitized
        api = ApiClient(sanitized)
        _uiState.update { it.copy(serverUrl = sanitized) }
        refreshData()
    }

    fun resetServerUrl() {
        sessionStore.resetServerUrl()
        val defaultUrl = sessionStore.getServerUrl()
        ServerConfig.baseUrl = defaultUrl
        currentServerUrl = defaultUrl
        api = ApiClient(defaultUrl)
        _uiState.update { it.copy(serverUrl = defaultUrl) }
        refreshData()
    }

    fun toggleShowOnlyActiveOrders(activeOnly: Boolean) {
        val session = _uiState.value.session ?: return
        _uiState.update { it.copy(showOnlyActiveOrders = activeOnly) }
        viewModelScope.launch {
            try {
                val orders = withContext(Dispatchers.IO) {
                    fetchOrders(session, activeOnly)
                }
                _uiState.update { it.copy(orders = orders) }
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd przełączania widoku zamówień", exception)
                handleAuthorizedError(exception)
            }
        }
    }

    fun setShowOnlyActiveOrders(activeOnly: Boolean) = toggleShowOnlyActiveOrders(activeOnly)

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, error = null) }
                val session = withContext(Dispatchers.IO) {
                    api.login(loginRequest)
                }
                sessionStore.save(session)
                _uiState.update { it.copy(session = session) }
                loadDashboard(session)
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd logowania", exception)
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = exception.toUserMessage(),
                        initialized = true,
                    )
                }
            }
        }
    }

    fun login(username: String, password: String) = login(LoginRequest(username, password))

    fun refreshData() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, error = null) }
                loadDashboard(session)
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd odświeżania", exception)
                handleAuthorizedError(exception)
            }
        }
    }

    fun refresh() = refreshData()

    fun refreshOrders() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            try {
                val orders = withContext(Dispatchers.IO) {
                    fetchOrders(session)
                }
                _uiState.update { it.copy(orders = orders) }
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd odświeżania zamówień", exception)
                handleAuthorizedError(exception)
            }
        }
    }

    fun createProduct(
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        contentType: String?,
        additionalImagesBytes: List<ByteArray> = emptyList(),
        onProductCreated: ((Product) -> Unit)? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val (created, updatedProducts) = withContext(Dispatchers.IO) {
                    val product = api.createProduct(
                        productRequest = request,
                        imageBytes = imageBytes,
                        imageFileName = fileName,
                        imageMimeType = contentType,
                        additionalImagesBytes = additionalImagesBytes,
                        session = session,
                    )
                    val products = api.getProducts(session)
                    Pair(product, products)
                }
                _uiState.update { it.copy(products = updatedProducts) }
                onProductCreated?.invoke(created)
                onSuccess("Produkt \"${created.model}\" został pomyślnie dodany do katalogu.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd dodawania produktu", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun createProduct(
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        contentType: String?,
        additionalImagesBytes: List<ByteArray> = emptyList(),
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        createProduct(
            request = request,
            imageBytes = imageBytes,
            fileName = fileName,
            contentType = contentType,
            additionalImagesBytes = additionalImagesBytes,
            onProductCreated = null,
            onSuccess = onSuccess,
            onError = onError,
        )
    }

    suspend fun createProductDirect(
        request: CreateProductRequest,
        imageBytes: ByteArray? = null,
        fileName: String? = null,
        contentType: String? = null,
        additionalImagesBytes: List<ByteArray> = emptyList(),
    ): Product {
        val session = _uiState.value.session ?: throw ApiException(401, "Brak aktywnej sesji")
        val (created, updatedProducts) = withContext(Dispatchers.IO) {
            val product = api.createProduct(
                productRequest = request,
                imageBytes = imageBytes,
                imageFileName = fileName,
                imageMimeType = contentType,
                additionalImagesBytes = additionalImagesBytes,
                session = session,
            )
            val products = api.getProducts(session)
            Pair(product, products)
        }
        _uiState.update { it.copy(products = updatedProducts) }
        return created
    }

    fun updateProduct(
        id: Long,
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        contentType: String?,
        additionalImagesBytes: List<ByteArray> = emptyList(),
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val (updated, updatedProducts) = withContext(Dispatchers.IO) {
                    val product = api.updateProduct(
                        id = id,
                        productRequest = request,
                        imageBytes = imageBytes,
                        imageFileName = fileName,
                        imageMimeType = contentType,
                        additionalImagesBytes = additionalImagesBytes,
                        session = session,
                    )
                    val products = api.getProducts(session)
                    Pair(product, products)
                }
                _uiState.update { it.copy(products = updatedProducts) }
                onSuccess("Produkt \"${updated.model}\" został zaktualizowany.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd edycji produktu", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun createFurnitureType(
        name: String,
        onSuccess: (FurnitureTypeItem) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val created = withContext(Dispatchers.IO) {
                    api.createFurnitureType(name.trim(), session)
                }
                _uiState.update { current ->
                    current.copy(furnitureTypes = (current.furnitureTypes + created).sortedBy { it.name })
                }
                onSuccess(created)
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd dodawania typu mebla", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun deleteFurnitureType(
        id: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    api.deleteFurnitureType(id, session)
                }
                _uiState.update { current ->
                    current.copy(furnitureTypes = current.furnitureTypes.filter { it.id != id })
                }
                onSuccess()
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd usuwania typu mebla", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun deleteProduct(
        id: Long,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val updatedProducts = withContext(Dispatchers.IO) {
                    api.deleteProduct(id, session)
                    api.getProducts(session)
                }
                _uiState.update { it.copy(products = updatedProducts) }
                onSuccess("Produkt został pomyślnie usunięty.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd usuwania produktu", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun deleteProduct(
        product: Product,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = deleteProduct(product.id, onSuccess, onError)

    fun createOrder(
        request: CreateOrderRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val (created, updatedOrders) = withContext(Dispatchers.IO) {
                    val order = api.createOrder(request, session)
                    val orders = fetchOrders(session)
                    Pair(order, orders)
                }
                _uiState.update { it.copy(orders = updatedOrders) }
                onSuccess("Zamówienie #${created.id} zostało utworzone.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd tworzenia zamówienia", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun updateOrder(
        id: Long,
        request: CreateOrderRequest,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val (updated, updatedOrders) = withContext(Dispatchers.IO) {
                    val order = api.updateOrder(id, request, session)
                    val orders = fetchOrders(session)
                    Pair(order, orders)
                }
                _uiState.update {
                    it.copy(
                        orders = updatedOrders,
                        selectedOrder = if (it.selectedOrder?.id == id) updated else it.selectedOrder,
                    )
                }
                onSuccess("Zamówienie #$id zostało zaktualizowane.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd edycji zamówienia", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun setOrderStatus(
        order: FurnitureOrder,
        status: OrderStatus,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val (updatedOrder, updatedOrders) = withContext(Dispatchers.IO) {
                    val updated = api.setOrderStatus(order.id, status, session)
                    val orders = fetchOrders(session)
                    Pair(updated, orders)
                }
                _uiState.update {
                    it.copy(
                        orders = updatedOrders,
                        selectedOrder = if (it.selectedOrder?.id == order.id) updatedOrder else it.selectedOrder,
                    )
                }
                onSuccess("Status zamówienia #${order.id} zmieniono na: ${status.displayName}.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd zmiany statusu zamówienia", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun setOrderStatus(
        order: FurnitureOrder,
        active: Boolean,
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {},
    ) {
        setOrderStatus(order, if (active) OrderStatus.ACTIVE else OrderStatus.COMPLETED, onSuccess, onError)
    }

    fun setSelectedOrderStatusFilter(status: OrderStatus?) {
        _uiState.update {
            it.copy(
                selectedOrderStatusFilter = status,
                showOnlyActiveOrders = (status == OrderStatus.ACTIVE),
            )
        }
    }

    fun deleteOrder(
        order: FurnitureOrder,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val updatedOrders = withContext(Dispatchers.IO) {
                    api.deleteOrder(order.id, session)
                    fetchOrders(session)
                }
                _uiState.update {
                    it.copy(
                        orders = updatedOrders,
                        selectedOrder = if (it.selectedOrder?.id == order.id) null else it.selectedOrder,
                    )
                }
                onSuccess("Zamówienie #${order.id} zostało pomyślnie usunięte.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd usuwania zamówienia", exception)
                val userMsg = exception.toUserMessage()
                if (exception is ApiException && exception.statusCode == 401) {
                    handleAuthorizedError(exception)
                }
                onError(userMsg)
            }
        }
    }

    fun selectSection(section: MainSection) {
        _uiState.update { it.copy(section = section, selectedOrder = null) }
    }

    fun selectOrder(order: FurnitureOrder) {
        val session = _uiState.value.session ?: return
        _uiState.update { it.copy(selectedOrder = order, loading = true, error = null) }
        viewModelScope.launch {
            try {
                val details = withContext(Dispatchers.IO) {
                    api.getOrder(order.id, session)
                }
                _uiState.update { it.copy(selectedOrder = details, loading = false) }
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd pobierania zamówienia", exception)
                handleAuthorizedError(exception)
            }
        }
    }

    fun closeOrder() {
        _uiState.update { it.copy(selectedOrder = null) }
    }

    fun logout() {
        sessionStore.clear()
        _uiState.value = FurnitureUiState(initialized = true, serverUrl = currentServerUrl)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun fetchOrders(
        session: AuthSession,
        activeOnly: Boolean = false,
    ): List<FurnitureOrder> {
        return api.getOrders(session)
    }

    private suspend fun loadDashboard(session: AuthSession) {
        val (products, orders, types) = withContext(Dispatchers.IO) {
            val productsDeferred = async { api.getProducts(session) }
            val ordersDeferred = async { fetchOrders(session) }
            val typesDeferred = async {
                try {
                    api.getFurnitureTypes(session)
                } catch (e: Exception) {
                    emptyList()
                }
            }
            Triple(productsDeferred.await(), ordersDeferred.await(), typesDeferred.await())
        }
        _uiState.update {
            it.copy(
                initialized = true,
                loading = false,
                products = products,
                orders = orders,
                furnitureTypes = types,
                error = null,
            )
        }
    }

    private fun handleAuthorizedError(exception: Exception) {
        if (exception is ApiException && exception.statusCode == 401) {
            sessionStore.clear()
            _uiState.value = FurnitureUiState(
                initialized = true,
                serverUrl = currentServerUrl,
                error = "Sesja wygasła. Zaloguj się ponownie.",
            )
        } else {
            _uiState.update {
                it.copy(initialized = true, loading = false, error = exception.toUserMessage())
            }
        }
    }

    private fun Throwable.toUserMessage(): String {
        return when (this) {
            is ApiException -> message ?: "Błąd serwera ($statusCode)"
            is ConnectException, is UnknownHostException -> "Nie można połączyć się z serwerem. Sprawdź połączenie z siecią i adres API."
            is SocketTimeoutException -> "Przekroczono limit czasu połączenia."
            else -> localizedMessage ?: "Wystąpił nieoczekiwany błąd."
        }
    }
}
