package produkcja.meble.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import produkcja.meble.app.dto.CreateOrderRequest;
import produkcja.meble.app.dto.OrderItemResponse;
import produkcja.meble.app.dto.OrderResponse;
import produkcja.meble.app.dto.ProductResponse;
import produkcja.meble.app.dto.UpdateOrderRequest;
import produkcja.meble.app.dto.UserResponse;
import produkcja.meble.app.entity.Order;
import produkcja.meble.app.entity.OrderItem;
import produkcja.meble.app.entity.OrderStatus;
import produkcja.meble.app.entity.Product;
import produkcja.meble.app.entity.UserAccount;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "status", expression = "java(createOrderRequest.status() != null ? createOrderRequest.status() : produkcja.meble.app.entity.OrderStatus.ACTIVE)")
    @Mapping(target = "items", ignore = true)
    Order toEntity(CreateOrderRequest createOrderRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    void updateEntityFromRequest(UpdateOrderRequest request, @MappingTarget Order order);

    OrderResponse toResponse(Order order);

    List<OrderResponse> toResponseList(List<Order> orders);

    OrderItemResponse toItemResponse(OrderItem orderItem);

    ProductResponse toProductResponse(Product product);

    default UserResponse toUserResponse(UserAccount userAccount) {
        if (userAccount == null) {
            return null;
        }
        return UserResponse.from(userAccount);
    }
}
