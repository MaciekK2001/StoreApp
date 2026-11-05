package produkcja.meble.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductRequest(
        @NotNull(message = "Product price is required")
        @PositiveOrZero(message = "Product price must be greater than or equal to 0")
        BigDecimal price,

        @PositiveOrZero(message = "Length must be greater than or equal to 0")
        double length,

        @PositiveOrZero(message = "Depth must be greater than or equal to 0")
        double depth,

        @PositiveOrZero(message = "Height must be greater than or equal to 0")
        double height,

        String colour,

        @NotBlank(message = "Product model is required")
        String model,

        String type,

        String material,

        String fabric,

        String remarks,

        String equipment,

        String imageUrl,

        List<String> additionalImages,

        @PositiveOrZero(message = "Lead time weeks must be greater than or equal to 0")
        Integer leadTimeWeeks
) {
}
