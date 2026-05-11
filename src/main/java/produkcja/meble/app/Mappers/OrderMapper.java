package produkcja.meble.app.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import produkcja.meble.app.DTO.CreateOrderRequest;
import produkcja.meble.app.Entities.Order;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    Order toEntity(CreateOrderRequest createOrderRequest);
}
