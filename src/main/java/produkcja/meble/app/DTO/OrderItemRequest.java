package produkcja.meble.app.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@AllArgsConstructor
@Getter
@Builder
public class OrderItemRequest {
    @NotNull
    private Long productId;

    @Positive
    private int quantity;
}


