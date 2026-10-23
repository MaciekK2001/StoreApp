package produkcja.meble.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {
    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
