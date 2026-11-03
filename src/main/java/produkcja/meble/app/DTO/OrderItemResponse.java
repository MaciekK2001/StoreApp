package produkcja.meble.app.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Long id,
        ProductResponse product,
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
}
