package produkcja.meble.mobile.data

class ApiException(val statusCode: Int, message: String) : Exception(message)

enum class OrderStatus(val displayName: String) {
    ACTIVE("Aktywne"),
    COMPLETED("Zrealizowane"),
    CANCELLED("Anulowane");

    companion object {
        fun from(value: String?): OrderStatus {
            if (value.isNullOrBlank()) return ACTIVE
            val trimmed = value.trim().uppercase()
            for (status in entries) {
                if (status.name.equals(trimmed, ignoreCase = true)) {
                    return status
                }
            }
            if (trimmed.startsWith("ZREALIZOWAN")) return COMPLETED
            if (trimmed.startsWith("ANULOWAN")) return CANCELLED
            if (trimmed.startsWith("AKTYWN")) return ACTIVE
            return ACTIVE
        }
    }
}

data class LoginRequest(
    val username: String,
    val password: String,
)

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
)

data class AuthSession(
    val tokenType: String,
    val token: String,
    val user: User,
) {
    val authorizationHeader: String
        get() = "$tokenType $token"
}

data class Product(
    val id: Long,
    val price: Double,
    val length: Double,
    val depth: Double,
    val height: Double,
    val colour: String,
    val model: String,
    val type: String,
    val material: String,
    val fabric: String,
    val remarks: String,
    val equipment: String,
    val imageUrl: String? = null,
    val additionalImages: List<String> = emptyList(),
) {
    val allImages: List<String>
        get() = listOfNotNull(imageUrl.takeIf { !it.isNullOrBlank() }) + additionalImages.filter { it.isNotBlank() }
}

data class CreateProductRequest(
    val price: Double,
    val length: Double,
    val depth: Double,
    val height: Double,
    val colour: String,
    val model: String,
    val type: String,
    val material: String,
    val fabric: String,
    val remarks: String,
    val equipment: String,
    val imageUrl: String? = null,
    val additionalImages: List<String> = emptyList(),
)

data class OrderItem(
    val id: Long,
    val product: Product?,
    val quantity: Int,
    val price: Double,
    val length: Double? = null,
    val depth: Double? = null,
    val height: Double? = null,
    val colour: String = "",
    val material: String = "",
    val fabric: String = "",
    val equipment: String = "",
    val remarks: String = "",
) {
    val effectiveLength: Double get() = length ?: product?.length ?: 0.0
    val effectiveDepth: Double get() = depth ?: product?.depth ?: 0.0
    val effectiveHeight: Double get() = height ?: product?.height ?: 0.0
    val effectiveColour: String get() = colour.ifBlank { product?.colour.orEmpty() }
    val effectiveMaterial: String get() = material.ifBlank { product?.material.orEmpty() }
    val effectiveFabric: String get() = fabric.ifBlank { product?.fabric.orEmpty() }
    val effectiveEquipment: String get() = equipment.ifBlank { product?.equipment.orEmpty() }
    val effectiveRemarks: String get() = remarks.ifBlank { product?.remarks.orEmpty() }
    val effectivePrice: Double get() = if (price > 0.0) price else (product?.price ?: 0.0)

    val isCustomized: Boolean
        get() = product != null && (
            (length != null && length != product.length) ||
            (depth != null && depth != product.depth) ||
            (height != null && height != product.height) ||
            (colour.isNotBlank() && colour != product.colour) ||
            (fabric.isNotBlank() && fabric != product.fabric) ||
            (material.isNotBlank() && material != product.material) ||
            (equipment.isNotBlank() && equipment != product.equipment) ||
            (remarks.isNotBlank() && remarks != product.remarks) ||
            (price > 0.0 && price != product.price)
        )
}

data class FurnitureOrder(
    val id: Long,
    val createdBy: User? = null,
    val createdAt: String,
    val deliveryDate: String,
    val phoneNumber: String,
    val fulfillmentTime: Int,
    val contactRemarks: String,
    val address: String,
    val assemblyPrice: Double,
    val assemblyRemarks: String,
    val discount: Double,
    val discountRemarks: String,
    val transportPrice: Double,
    val transportRemarks: String,
    val remarks: String,
    val carryProductsPrice: Double,
    val carryProductsRemarks: String,
    val active: Boolean,
    val status: String = "ACTIVE",
    val deposit: Double,
    val items: List<OrderItem>,
) {
    val orderStatus: OrderStatus
        get() = if (!active && status == "ACTIVE") OrderStatus.COMPLETED else OrderStatus.from(status)
}

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int,
    val price: Double? = null,
    val length: Double? = null,
    val depth: Double? = null,
    val height: Double? = null,
    val colour: String? = null,
    val material: String? = null,
    val fabric: String? = null,
    val equipment: String? = null,
    val remarks: String? = null,
)

data class CreateOrderRequest(
    val deliveryDate: String,
    val phoneNumber: String,
    val fulfillmentTime: Int,
    val contactRemarks: String? = null,
    val address: String,
    val assemblyPrice: Double = 0.0,
    val assemblyRemarks: String? = null,
    val discount: Double = 0.0,
    val discountRemarks: String? = null,
    val transportPrice: Double = 0.0,
    val transportRemarks: String? = null,
    val remarks: String? = null,
    val carryProductsPrice: Double = 0.0,
    val carryProductsRemarks: String? = null,
    val deposit: Double = 0.0,
    val active: Boolean? = true,
    val status: String? = "ACTIVE",
    val orderItemRequests: List<OrderItemRequest>,
)

data class ProductFilter(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minLength: Double? = null,
    val maxLength: Double? = null,
    val minDepth: Double? = null,
    val maxDepth: Double? = null,
    val minHeight: Double? = null,
    val maxHeight: Double? = null,
    val colour: String? = null,
    val model: String? = null,
    val type: Set<String> = emptySet(),
    val material: String? = null,
    val fabric: String? = null,
) {
    val activeCount: Int
        get() {
            var count = 0
            if (minPrice != null) count++
            if (maxPrice != null) count++
            if (minLength != null) count++
            if (maxLength != null) count++
            if (minDepth != null) count++
            if (maxDepth != null) count++
            if (minHeight != null) count++
            if (maxHeight != null) count++
            if (!colour.isNullOrBlank()) count++
            if (!model.isNullOrBlank()) count++
            if (type.isNotEmpty()) count += type.size
            if (!material.isNullOrBlank()) count++
            if (!fabric.isNullOrBlank()) count++
            return count
        }

    val isEmpty: Boolean
        get() = activeCount == 0
}
