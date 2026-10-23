package produkcja.meble.app.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(
        Long id,
        UserResponse createdBy,
        LocalDateTime createdAt,
        LocalDateTime deliveryDate,
        String phoneNumber,
        int fulfillmentTime,
        String contactRemarks,
        String address,
        BigDecimal assemblyPrice,
        String assemblyRemarks,
        BigDecimal discount,
        String discountRemarks,
        BigDecimal transportPrice,
        String transportRemarks,
        String remarks,
        BigDecimal carryProductsPrice,
        String carryProductsRemarks,
        boolean active,
        BigDecimal deposit,
        List<OrderItemResponse> items
) {
}
