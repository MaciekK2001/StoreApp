package produkcja.meble.app.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Long id,
        BigDecimal price,
        double length,
        double depth,
        double height,
        String colour,
        String model,
        String type,
        String material,
        String fabric,
        String remarks,
        String equipment,
        String imageUrl
) {
}
