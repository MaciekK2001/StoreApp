package produkcja.meble.app.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Long id,
        ProductResponse product,
        int quantity,
        BigDecimal price
) {
}