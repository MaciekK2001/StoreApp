package produkcja.meble.app.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class CreateOrderRequest{
    @NotNull
    @FutureOrPresent
    private LocalDateTime deliveryDate;

    @NotBlank
    private String phoneNumber;

    @Positive
    private int fulfillmentTime;

    private String contactRemarks;

    @NotBlank
    private String address;

    @PositiveOrZero
    private double assemblyPrice;

    private String assemblyRemarks;

    @PositiveOrZero
    private double discount;

    private String discountRemarks;

    @PositiveOrZero
    private double transportPrice;

    private String transportRemarks;

    private String remarks;

    @PositiveOrZero
    private double carryProductsPrice;

    private String carryProductsRemarks;

    @PositiveOrZero
    private double deposit;

    @Valid
    @NotEmpty
    private List<OrderItemRequest> orderItemRequests;
}
