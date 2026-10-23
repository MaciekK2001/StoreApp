package produkcja.meble.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import produkcja.meble.app.dto.ProductRequest;
import produkcja.meble.app.dto.ProductResponse;
import produkcja.meble.app.entity.FurnitureType;
import produkcja.meble.app.entity.Product;
import produkcja.meble.app.exception.ConflictException;
import produkcja.meble.app.exception.ResourceNotFoundException;
import produkcja.meble.app.mapper.ProductMapper;
import produkcja.meble.app.repository.OrderItemRepository;
import produkcja.meble.app.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductMapper productMapper;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProductResponse saveProduct(ProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    public List<ProductResponse> findAllProducts(List<FurnitureType> types) {
        List<Product> products;
        if(types == null) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findByTypeIn(types);
        }
        return productMapper.toResponseList(products);
    }

    public ProductResponse findProductById(Long id) {
        Product product = findEntityById(id);
        return productMapper.toResponse(product);
    }

    public Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findEntityById(id);
        String oldImageUrl = product.getImageUrl();

        productMapper.updateEntityFromRequest(request, product);
        Product updatedProduct = productRepository.save(product);

        if (StringUtils.hasText(oldImageUrl) && !oldImageUrl.equals(request.imageUrl())) {
            fileStorageService.deleteFile(oldImageUrl);
        }

        return productMapper.toResponse(updatedProduct);
    }

    @Transactional
    public void deleteProductById(Long id) {
        Product product = findEntityById(id);

        if (orderItemRepository.existsByProductId(id)) {
            throw new ConflictException("Product with id " + id + " is used in existing orders");
        }

        String imageUrl = product.getImageUrl();
        productRepository.delete(product);

        if (StringUtils.hasText(imageUrl)) {
            fileStorageService.deleteFile(imageUrl);
        }
    }
}
