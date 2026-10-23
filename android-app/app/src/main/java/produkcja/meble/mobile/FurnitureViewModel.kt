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
import produkcja.meble.mobile.data.LoginRequest
import produkcja.meble.mobile.data.Product
import produkcja.meble.mobile.data.SessionStore
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
    val orders: List<FurnitureOrder> = emptyList(),
    val selectedOrder: FurnitureOrder? = null,
    val error: String? = null,
    val showOnlyActiveOrders: Boolean = true,
)

class FurnitureViewModel(application: Application) : AndroidViewModel(application) {
    private val api = ApiClient(BuildConfig.API_BASE_URL)
    private val sessionStore = SessionStore(application)
    private val _uiState = MutableStateFlow(FurnitureUiState(session = sessionStore.load()))

    val uiState: StateFlow<FurnitureUiState> = _uiState.asStateFlow()

    init {
        if (_uiState.value.session == null) {
            _uiState.update { it.copy(initialized = true) }
        } else {
            refresh()
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Uzupełnij nazwę użytkownika i hasło.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                val session = withContext(Dispatchers.IO) {
                    api.login(LoginRequest(username.trim(), password))
                }
                sessionStore.save(session)
                _uiState.update { it.copy(session = session) }
                loadDashboard(session)
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd logowania", exception)
                _uiState.update {
                    it.copy(
                        initialized = true,
                        loading = false,
                        error = exception.toUserMessage(),
                    )
                }
            }
        }
    }

    fun refresh() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                loadDashboard(session)
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd odświeżania", exception)
                handleAuthorizedError(exception)
            }
        }
    }

    fun setShowOnlyActiveOrders(activeOnly: Boolean) {
        if (_uiState.value.showOnlyActiveOrders == activeOnly) return
        val session = _uiState.value.session ?: return
        _uiState.update { it.copy(showOnlyActiveOrders = activeOnly, loading = true) }
        viewModelScope.launch {
            try {
                val orders = withContext(Dispatchers.IO) {
                    fetchOrders(session, activeOnly)
                }
                _uiState.update { it.copy(orders = orders, loading = false) }
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd przełączania widoku zamówień", exception)
                handleAuthorizedError(exception)
            }
        }
    }

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
        mimeType: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val (createdProduct, updatedProducts) = withContext(Dispatchers.IO) {
                    val created = api.createProduct(request, imageBytes, fileName, mimeType, session)
                    val products = api.getProducts(session)
                    Pair(created, products)
                }
                _uiState.update { it.copy(products = updatedProducts) }
                onSuccess("Mebel „${createdProduct.model}” został pomyślnie dodany.")
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

    fun updateProduct(
        id: Long,
        request: CreateProductRequest,
        imageBytes: ByteArray?,
        fileName: String?,
        mimeType: String?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val (updatedProduct, updatedProducts) = withContext(Dispatchers.IO) {
                    val updated = api.updateProduct(id, request, imageBytes, fileName, mimeType, session)
                    val products = api.getProducts(session)
                    Pair(updated, products)
                }
                _uiState.update { it.copy(products = updatedProducts) }
                onSuccess("Mebel „${updatedProduct.model}” został pomyślnie zaktualizowany.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd aktualizacji produktu", exception)
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
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val updatedProducts = withContext(Dispatchers.IO) {
                    api.deleteProduct(product.id, session)
                    api.getProducts(session)
                }
                _uiState.update { it.copy(products = updatedProducts) }
                onSuccess("Mebel „${product.model}” został pomyślnie usunięty.")
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
                val (createdOrder, updatedOrders) = withContext(Dispatchers.IO) {
                    val created = api.createOrder(request, session)
                    val orders = fetchOrders(session)
                    Pair(created, orders)
                }
                _uiState.update { it.copy(orders = updatedOrders) }
                onSuccess("Zamówienie #${createdOrder.id} zostało pomyślnie utworzone.")
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
                val (updatedOrder, updatedOrders) = withContext(Dispatchers.IO) {
                    val updated = api.updateOrder(id, request, session)
                    val orders = fetchOrders(session)
                    Pair(updated, orders)
                }
                _uiState.update {
                    it.copy(
                        orders = updatedOrders,
                        selectedOrder = if (it.selectedOrder?.id == id) updatedOrder else it.selectedOrder,
                    )
                }
                onSuccess("Zamówienie #${updatedOrder.id} zostało pomyślnie zaktualizowane.")
            } catch (exception: Exception) {
                Log.e("FurnitureViewModel", "Błąd aktualizacji zamówienia", exception)
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
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = _uiState.value.session ?: run {
            onError("Brak aktywnej sesji użytkownika. Zaloguj się ponownie.")
            return
        }
        viewModelScope.launch {
            try {
                val (updatedOrder, updatedOrders) = withContext(Dispatchers.IO) {
                    val updated = api.setOrderStatus(order.id, active, session)
                    val orders = fetchOrders(session)
                    Pair(updated, orders)
                }
                _uiState.update {
                    it.copy(
                        orders = updatedOrders,
                        selectedOrder = if (it.selectedOrder?.id == order.id) updatedOrder else it.selectedOrder,
                    )
                }
                val msg = if (active) {
                    "Zamówienie #${order.id} oznaczono jako aktywne."
                } else {
                    "Zamówienie #${order.id} oznaczono jako nieaktywne."
                }
                onSuccess(msg)
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
        _uiState.value = FurnitureUiState(initialized = true)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun fetchOrders(
        session: AuthSession,
        activeOnly: Boolean = _uiState.value.showOnlyActiveOrders,
    ): List<FurnitureOrder> {
        return if (activeOnly) {
            api.getActiveOrders(session)
        } else {
            api.getOrders(session)
        }
    }

    private suspend fun loadDashboard(session: AuthSession) {
        val (products, orders) = withContext(Dispatchers.IO) {
            val productsDeferred = async { api.getProducts(session) }
            val ordersDeferred = async { fetchOrders(session) }
            Pair(productsDeferred.await(), ordersDeferred.await())
        }
        _uiState.update {
            it.copy(
                initialized = true,
                loading = false,
                products = products,
                orders = orders,
                error = null,
            )
        }
    }

    private fun handleAuthorizedError(exception: Exception) {
        if (exception is ApiException && exception.statusCode == 401) {
            sessionStore.clear()
            _uiState.value = FurnitureUiState(
                initialized = true,
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
