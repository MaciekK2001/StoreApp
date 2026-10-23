package produkcja.meble.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CreateOrderRequest(
        @NotNull(message = "Delivery date is required")
        @FutureOrPresent(message = "Delivery date must be today or in the future")
        LocalDateTime deliveryDate,

        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @PositiveOrZero(message = "Fulfillment time must be greater than or equal to 0")
        int fulfillmentTime,

        String contactRemarks,

        @NotBlank(message = "Address is required")
        String address,

        @PositiveOrZero(message = "Assembly price must be greater than or equal to 0")
        BigDecimal assemblyPrice,

        String assemblyRemarks,

        @PositiveOrZero(message = "Discount must be greater than or equal to 0")
        BigDecimal discount,

        String discountRemarks,

        @PositiveOrZero(message = "Transport price must be greater than or equal to 0")
        BigDecimal transportPrice,

        String transportRemarks,

        String remarks,

        @PositiveOrZero(message = "Carry products price must be greater than or equal to 0")
        BigDecimal carryProductsPrice,

        String carryProductsRemarks,

        @PositiveOrZero(message = "Deposit must be greater than or equal to 0")
        BigDecimal deposit,

        @Valid
        @NotEmpty(message = "Order must contain at least one item")
        List<OrderItemRequest> orderItemRequests
) {
    public List<OrderItemRequest> getOrderItemRequests() {
        return orderItemRequests;
    }
}
