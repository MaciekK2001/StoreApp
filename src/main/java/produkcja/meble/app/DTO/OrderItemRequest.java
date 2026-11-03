package produkcja.meble.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        BigDecimal price,
        Double length,
        Double depth,
        Double height,
        String colour,
        String material,
        String fabric,
        String equipment,
        String remarks
) {
    public OrderItemRequest(Long productId, int quantity) {
        this(productId, quantity, null, null, null, null, null, null, null, null, null);
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
