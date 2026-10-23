package produkcja.meble.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import produkcja.meble.app.dto.ProductRequest;
import produkcja.meble.app.dto.ProductResponse;
import produkcja.meble.app.entity.Product;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequest request);

    ProductResponse toResponse(Product product);

    List<ProductResponse> toResponseList(List<Product> products);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}