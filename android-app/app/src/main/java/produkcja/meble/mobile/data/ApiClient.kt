package produkcja.meble.mobile.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.UUID

class ApiClient(private val baseUrl: String) {

    fun login(loginRequest: LoginRequest): AuthSession {
        val payload = JSONObject().apply {
            put("username", loginRequest.username)
            put("password", loginRequest.password)
        }
        val response = sendJson("POST", "/api/auth/login", payload.toString(), null)
        return parseAuthSession(JSONObject(response))
    }

    fun getProducts(session: AuthSession): List<Product> {
        val response = sendJson("GET", "/api/products", null, session.authorizationHeader)
        return parseArray(response, ::parseProduct)
    }

    fun getOrders(session: AuthSession): List<FurnitureOrder> {
        val response = sendJson("GET", "/api/orders", null, session.authorizationHeader)
        return parseArray(response, ::parseOrder)
    }

    fun getActiveOrders(session: AuthSession): List<FurnitureOrder> {
        val response = sendJson("GET", "/api/orders/active", null, session.authorizationHeader)
        return parseArray(response, ::parseOrder)
    }

    fun getOrder(id: Long, session: AuthSession): FurnitureOrder {
        val response = sendJson("GET", "/api/orders/$id", null, session.authorizationHeader)
        return parseOrder(JSONObject(response))
    }

    fun createOrder(orderRequest: CreateOrderRequest, session: AuthSession): FurnitureOrder {
        val payload = serializeOrderRequest(orderRequest)
        val response = sendJson("POST", "/api/orders", payload.toString(), session.authorizationHeader)
        return parseOrder(JSONObject(response))
    }

    fun updateOrder(id: Long, orderRequest: CreateOrderRequest, session: AuthSession): FurnitureOrder {
        val payload = serializeOrderRequest(orderRequest)
        val response = sendJson("PUT", "/api/orders/$id", payload.toString(), session.authorizationHeader)
        return parseOrder(JSONObject(response))
    }

    fun setOrderStatus(id: Long, active: Boolean, session: AuthSession): FurnitureOrder {
        val response = sendJson("PATCH", "/api/orders/$id/status?active=$active", null, session.authorizationHeader)
        return parseOrder(JSONObject(response))
    }

    fun deleteOrder(id: Long, session: AuthSession) {
        sendJson("DELETE", "/api/orders/$id", null, session.authorizationHeader)
    }

    fun createProduct(
        productRequest: CreateProductRequest,
        imageBytes: ByteArray? = null,
        imageFileName: String? = null,
        imageMimeType: String? = null,
        additionalImagesBytes: List<ByteArray> = emptyList(),
        session: AuthSession,
    ): Product {
        var finalImageUrl = productRequest.imageUrl
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            val fileName = imageFileName ?: "product_main_${System.currentTimeMillis()}.jpg"
            val mimeType = imageMimeType ?: "image/jpeg"
            finalImageUrl = uploadImage(imageBytes, fileName, mimeType, session)
        }

        val uploadedAdditionalUrls = additionalImagesBytes.mapIndexed { index, bytes ->
            val fileName = "product_extra_${System.currentTimeMillis()}_$index.jpg"
            uploadImage(bytes, fileName, "image/jpeg", session)
        }
        val finalAdditionalImages = productRequest.additionalImages + uploadedAdditionalUrls

        val enrichedRequest = productRequest.copy(
            imageUrl = finalImageUrl,
            additionalImages = finalAdditionalImages,
        )
        val payload = serializeProductRequest(enrichedRequest)
        val response = sendJson("POST", "/api/products", payload.toString(), session.authorizationHeader)
        return parseProduct(JSONObject(response))
    }

    fun updateProduct(
        id: Long,
        productRequest: CreateProductRequest,
        imageBytes: ByteArray? = null,
        imageFileName: String? = null,
        imageMimeType: String? = null,
        additionalImagesBytes: List<ByteArray> = emptyList(),
        session: AuthSession,
    ): Product {
        var finalImageUrl = productRequest.imageUrl
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            val fileName = imageFileName ?: "product_main_${System.currentTimeMillis()}.jpg"
            val mimeType = imageMimeType ?: "image/jpeg"
            finalImageUrl = uploadImage(imageBytes, fileName, mimeType, session)
        }

        val uploadedAdditionalUrls = additionalImagesBytes.mapIndexed { index, bytes ->
            val fileName = "product_extra_${System.currentTimeMillis()}_$index.jpg"
            uploadImage(bytes, fileName, "image/jpeg", session)
        }
        val finalAdditionalImages = productRequest.additionalImages + uploadedAdditionalUrls

        val enrichedRequest = productRequest.copy(
            imageUrl = finalImageUrl,
            additionalImages = finalAdditionalImages,
        )
        val payload = serializeProductRequest(enrichedRequest)
        val response = sendJson("PUT", "/api/products/$id", payload.toString(), session.authorizationHeader)
        return parseProduct(JSONObject(response))
    }

    fun deleteProduct(id: Long, session: AuthSession) {
        sendJson("DELETE", "/api/products/$id", null, session.authorizationHeader)
    }

    private fun serializeProductRequest(request: CreateProductRequest): JSONObject {
        return JSONObject().apply {
            put("price", request.price)
            put("length", request.length)
            put("depth", request.depth)
            put("height", request.height)
            put("colour", request.colour)
            put("model", request.model)
            put("type", request.type)
            put("material", request.material)
            put("fabric", request.fabric)
            put("remarks", request.remarks)
            put("equipment", request.equipment)
            if (request.imageUrl != null) {
                put("imageUrl", request.imageUrl)
            } else {
                put("imageUrl", JSONObject.NULL)
            }
            val additionalArray = JSONArray()
            request.additionalImages.forEach { additionalArray.put(it) }
            put("additionalImages", additionalArray)
        }
    }

    private fun serializeOrderRequest(request: CreateOrderRequest): JSONObject {
        return JSONObject().apply {
            put("deliveryDate", request.deliveryDate)
            put("phoneNumber", request.phoneNumber)
            put("fulfillmentTime", request.fulfillmentTime)
            request.contactRemarks?.let { put("contactRemarks", it) }
            put("address", request.address)
            put("assemblyPrice", request.assemblyPrice)
            request.assemblyRemarks?.let { put("assemblyRemarks", it) }
            put("discount", request.discount)
            request.discountRemarks?.let { put("discountRemarks", it) }
            put("transportPrice", request.transportPrice)
            request.transportRemarks?.let { put("transportRemarks", it) }
            request.remarks?.let { put("remarks", it) }
            put("carryProductsPrice", request.carryProductsPrice)
            request.carryProductsRemarks?.let { put("carryProductsRemarks", it) }
            put("deposit", request.deposit)
            request.active?.let { put("active", it) }

            val itemsArray = JSONArray()
            request.orderItemRequests.forEach { item ->
                if (item.productId > 0 && item.quantity > 0) {
                    itemsArray.put(
                        JSONObject().apply {
                            put("productId", item.productId)
                            put("quantity", item.quantity)
                        }
                    )
                }
            }
            put("orderItemRequests", itemsArray)
        }
    }

    private fun uploadImage(
        imageBytes: ByteArray,
        fileName: String,
        mimeType: String,
        session: AuthSession,
    ): String {
        val boundary = "Boundary" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "")
        val lineEnd = "\r\n"
        val twoHyphens = "--"

        val url = URL(buildUrl("/api/products/images"))
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            doInput = true
            useCaches = false
            setRequestProperty("Connection", "Keep-Alive")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            setRequestProperty("Authorization", session.authorizationHeader)
        }

        connection.outputStream.use { output ->
            output.write(("$twoHyphens$boundary$lineEnd").toByteArray(StandardCharsets.UTF_8))
            output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$lineEnd").toByteArray(StandardCharsets.UTF_8))
            output.write(("Content-Type: $mimeType$lineEnd$lineEnd").toByteArray(StandardCharsets.UTF_8))
            output.write(imageBytes)
            output.write(lineEnd.toByteArray(StandardCharsets.UTF_8))
            output.write(("$twoHyphens$boundary$twoHyphens$lineEnd").toByteArray(StandardCharsets.UTF_8))
            output.flush()
        }

        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            val errorResponse = readStream(connection.errorStream)
            throw ApiException(statusCode, "Błąd wysyłania zdjęcia ($statusCode): $errorResponse")
        }

        val responseBody = readStream(connection.inputStream)
        return try {
            val json = JSONObject(responseBody)
            json.optString("imageUrl", json.optString("url", responseBody.trim()))
        } catch (_: Exception) {
            responseBody.trim()
        }
    }

    private fun sendJson(
        method: String,
        endpoint: String,
        body: String?,
        authHeader: String?,
    ): String {
        val url = URL(buildUrl(endpoint))
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Accept", "application/json")
            if (authHeader != null) {
                setRequestProperty("Authorization", authHeader)
            }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
        }

        if (body != null) {
            connection.outputStream.use { output ->
                output.write(body.toByteArray(StandardCharsets.UTF_8))
                output.flush()
            }
        }

        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            val errorResponse = readStream(connection.errorStream)
            throw ApiException(statusCode, "Żądanie nie powiodło się ($statusCode): $errorResponse")
        }

        if (statusCode == 204) {
            return ""
        }

        return readStream(connection.inputStream)
    }

    private fun readStream(stream: InputStream?): String {
        if (stream == null) return ""
        val buffer = ByteArray(1024)
        val result = ByteArrayOutputStream()
        var length: Int
        while (stream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }
        return result.toString(StandardCharsets.UTF_8.name())
    }

    private fun buildUrl(endpoint: String): String {
        val cleanBase = baseUrl.trimEnd('/')
        val cleanEndpoint = if (endpoint.startsWith("/")) endpoint else "/$endpoint"
        return "$cleanBase$cleanEndpoint"
    }

    private fun <T> parseArray(rawJson: String, transform: (JSONObject) -> T): List<T> {
        if (rawJson.isBlank()) return emptyList()
        val array = JSONArray(rawJson)
        return (0 until array.length()).map { index ->
            transform(array.getJSONObject(index))
        }
    }

    private fun parseAuthSession(json: JSONObject): AuthSession {
        val token = json.getString("token")
        val tokenType = json.optString("type", "Bearer")
        val userJson = json.getJSONObject("user")
        val user = User(
            id = userJson.getLong("id"),
            username = userJson.getString("username"),
            email = userJson.optString("email", ""),
            role = userJson.optString("role", "ROLE_USER"),
        )
        return AuthSession(tokenType = tokenType, token = token, user = user)
    }

    private fun parseProduct(json: JSONObject): Product {
        val additionalImagesArray = json.optJSONArray("additionalImages")
        val additionalImages = if (additionalImagesArray != null) {
            (0 until additionalImagesArray.length()).mapNotNull { idx ->
                additionalImagesArray.optString(idx).takeIf { it.isNotBlank() }
            }
        } else {
            emptyList()
        }

        return Product(
            id = json.getLong("id"),
            price = json.optDouble("price", 0.0),
            length = json.optDouble("length", 0.0),
            depth = json.optDouble("depth", 0.0),
            height = json.optDouble("height", 0.0),
            colour = json.optString("colour", ""),
            model = json.optString("model", ""),
            type = json.optString("type", ""),
            material = json.optString("material", ""),
            fabric = json.optString("fabric", ""),
            remarks = json.optString("remarks", ""),
            equipment = json.optString("equipment", ""),
            imageUrl = json.optString("imageUrl", "").takeIf { it.isNotBlank() },
            additionalImages = additionalImages,
        )
    }

    private fun parseOrder(json: JSONObject): FurnitureOrder {
        val user = json.optJSONObject("createdBy")?.let {
            User(
                id = it.optLong("id", 0),
                username = it.optString("username", ""),
                email = it.optString("email", ""),
                role = it.optString("role", "ROLE_USER"),
            )
        }

        val itemsArray = json.optJSONArray("items") ?: json.optJSONArray("orderItems") ?: JSONArray()
        val items = (0 until itemsArray.length()).map { index ->
            val itemJson = itemsArray.getJSONObject(index)
            val productJson = itemJson.optJSONObject("product")
            val product = productJson?.let { parseProduct(it) }
            OrderItem(
                id = itemJson.optLong("id", 0),
                product = product,
                quantity = itemJson.optInt("quantity", 1),
                price = itemJson.optDouble("price", 0.0),
            )
        }

        return FurnitureOrder(
            id = json.getLong("id"),
            createdBy = user,
            createdAt = json.optString("createdAt", ""),
            deliveryDate = json.optString("deliveryDate", ""),
            phoneNumber = json.optString("phoneNumber", ""),
            fulfillmentTime = json.optInt("fulfillmentTime", 0),
            contactRemarks = json.optString("contactRemarks", ""),
            address = json.optString("address", ""),
            assemblyPrice = json.optDouble("assemblyPrice", 0.0),
            assemblyRemarks = json.optString("assemblyRemarks", ""),
            discount = json.optDouble("discount", 0.0),
            discountRemarks = json.optString("discountRemarks", ""),
            transportPrice = json.optDouble("transportPrice", 0.0),
            transportRemarks = json.optString("transportRemarks", ""),
            remarks = json.optString("remarks", ""),
            carryProductsPrice = json.optDouble("carryProductsPrice", 0.0),
            carryProductsRemarks = json.optString("carryProductsRemarks", ""),
            deposit = json.optDouble("deposit", 0.0),
            active = json.optBoolean("active", true),
            items = items,
        )
    }
}
