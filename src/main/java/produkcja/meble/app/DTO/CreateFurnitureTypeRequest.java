package produkcja.meble.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFurnitureTypeRequest(
        @NotBlank(message = "Nazwa typu mebla jest wymagana")
        @Size(max = 50, message = "Nazwa typu mebla nie może przekraczać 50 znaków")
        String name
) {
}
