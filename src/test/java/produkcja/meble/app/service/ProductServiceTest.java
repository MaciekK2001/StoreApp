package produkcja.meble.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import produkcja.meble.app.dto.ProductRequest;
import produkcja.meble.app.dto.ProductResponse;
import produkcja.meble.app.entity.Product;
import produkcja.meble.app.exception.ConflictException;
import produkcja.meble.app.exception.ResourceNotFoundException;
import produkcja.meble.app.mapper.ProductMapper;
import produkcja.meble.app.repository.OrderItemRepository;
import produkcja.meble.app.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Product savedProduct;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        savedProduct = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        savedProduct.setId(1L);
        productRequest = buildTestProductRequest("Test model", BigDecimal.valueOf(1200));
        productResponse = buildTestProductResponse(1L, "Test model", BigDecimal.valueOf(1200));
    }

    @Test
    void saveProductPersistsAndReturnsMappedProduct() {
        when(productRepository.existsByModelIgnoreCase("Test model")).thenReturn(false);
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(productResponse);

        ProductResponse result = productService.saveProduct(productRequest);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test model", result.model());
        verify(productRepository).save(product);
    }

    @Test
    void saveProductThrowsConflictExceptionWhenModelAlreadyExists() {
        when(productRepository.existsByModelIgnoreCase("Test model")).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> productService.saveProduct(productRequest));
        assertTrue(ex.getMessage().contains("już istnieje"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void findProductByIdReturnsProductWhenExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(savedProduct));
        when(productMapper.toResponse(savedProduct)).thenReturn(productResponse);

        ProductResponse result = productService.findProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void findProductByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.findProductById(999L)
        );

        assertEquals("Product with id 999 not found", exception.getMessage());
    }

    @Test
    void findAllProductsDelegatesToRepositoryAndMapper() {
        ProductResponse response = buildTestProductResponse(1L, "Test model", BigDecimal.valueOf(1200));

        when(productRepository.findAll()).thenReturn(List.of(savedProduct));
        when(productMapper.toResponseList(List.of(savedProduct))).thenReturn(List.of(response));

        List<ProductResponse> products = productService.findAllProducts(null);

        assertEquals(1, products.size());
        assertEquals(1L, products.getFirst().id());
    }

    @Test
    void updateProductUpdatesAllEditableFields() {
        Product existingProduct = buildTestProduct("Old model", BigDecimal.valueOf(1200));
        existingProduct.setId(1L);
        ProductRequest request = buildTestProductRequest("Updated model", BigDecimal.valueOf(1500));
        ProductResponse response = buildTestProductResponse(1L, "Updated model", BigDecimal.valueOf(1500));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByModelIgnoreCaseAndIdNot("Updated model", 1L)).thenReturn(false);
        doAnswer(invocation -> {
            existingProduct.setModel("Updated model");
            existingProduct.setPrice(BigDecimal.valueOf(1500));
            return null;
        }).when(productMapper).updateEntityFromRequest(request, existingProduct);
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);
        when(productMapper.toResponse(existingProduct)).thenReturn(response);

        ProductResponse result = productService.updateProduct(1L, request);

        assertEquals("Updated model", result.model());
        assertEquals(0, BigDecimal.valueOf(1500).compareTo(result.price()));
        verify(productRepository).save(existingProduct);
    }

    @Test
    void updateProductThrowsConflictExceptionWhenModelAlreadyExistsForOtherProduct() {
        Product existingProduct = buildTestProduct("Old model", BigDecimal.valueOf(1200));
        existingProduct.setId(1L);
        ProductRequest request = buildTestProductRequest("Updated model", BigDecimal.valueOf(1500));

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.existsByModelIgnoreCaseAndIdNot("Updated model", 1L)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> productService.updateProduct(1L, request));
        assertTrue(ex.getMessage().contains("już istnieje"));
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProductByIdDeletesExistingProduct() {
        Product existingProduct = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        existingProduct.setId(1L);
        existingProduct.setImageUrl("/api/products/images/test.jpg");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(false);

        productService.deleteProductById(1L);

        verify(productRepository).delete(existingProduct);
        verify(fileStorageService).deleteFile("/api/products/images/test.jpg");
    }

    @Test
    void deleteProductByIdThrowsWhenProductIsUsedInOrders() {
        Product existingProduct = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        existingProduct.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> productService.deleteProductById(1L)
        );

        assertEquals("Product with id 1 is used in existing orders", exception.getMessage());
        verify(productRepository, never()).delete(existingProduct);
        verify(fileStorageService, never()).deleteFile(any());
    }

    private Product buildTestProduct(String model, BigDecimal price) {
        return Product.builder()
                .price(price)
                .length(200)
                .height(100)
                .depth(90)
                .colour("Brown")
                .model(model)
                .type("Łóżko")
                .material("Oak")
                .fabric("Leather")
                .remarks("Test product")
                .equipment("Storage")
                .build();
    }

    private ProductRequest buildTestProductRequest(String model, BigDecimal price) {
        return ProductRequest.builder()
                .price(price)
                .length(200)
                .height(100)
                .depth(90)
                .colour("Brown")
                .model(model)
                .type("BED")
                .material("Oak")
                .fabric("Leather")
                .remarks("Test product")
                .equipment("Storage")
                .build();
    }

    private ProductResponse buildTestProductResponse(Long id, String model, BigDecimal price) {
        return ProductResponse.builder()
                .id(id)
                .price(price)
                .length(200)
                .height(100)
                .depth(90)
                .colour("Brown")
                .model(model)
                .type("BED")
                .material("Oak")
                .fabric("Leather")
                .remarks("Test product")
                .equipment("Storage")
                .build();
    }
}
