package produkcja.meble.app.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import produkcja.meble.app.Entities.Product;
import produkcja.meble.app.Exceptions.BadRequestException;
import produkcja.meble.app.Exceptions.ConflictException;
import produkcja.meble.app.Repositories.OrderItemRepository;
import produkcja.meble.app.Repositories.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void saveProductSavesInRepository() {
        Product product = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        Product savedProduct = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        savedProduct.setId(1L);

        when(productRepository.save(product)).thenReturn(savedProduct);

        Product result = productService.saveProduct(product);

        assertSame(savedProduct, result);
        verify(productRepository).save(product);
    }

    @Test
    void saveProductThrowsWhenProductIsInvalid() {
        Product product = buildTestProduct("", BigDecimal.valueOf(1200));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productService.saveProduct(product)
        );

        assertEquals("Product model is required", exception.getMessage());
        verify(productRepository, never()).save(product);
    }

    @Test
    void findProductByIdReturnsProduct() {
        Product savedProduct = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        savedProduct.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(savedProduct));

        Product result = productService.findProductById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Test model", result.getModel());
        assertEquals(0, BigDecimal.valueOf(1200).compareTo(result.getPrice()));
    }

    @Test
    void findProductByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productService.findProductById(1L)
        );

        assertEquals("Product with id 1 not found", exception.getMessage());
    }

    @Test
    void findAllProductsReturnsRepositoryProducts() {
        Product savedProduct = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        savedProduct.setId(1L);

        when(productRepository.findAll()).thenReturn(List.of(savedProduct));

        List<Product> products = productService.findAllProducts();

        assertEquals(1, products.size());
        assertEquals(1L, products.getFirst().getId());
    }

    @Test
    void updateProductUpdatesAllEditableFields() {
        Product existingProduct = buildTestProduct("Old model", BigDecimal.valueOf(1200));
        existingProduct.setId(1L);
        Product productDetails = buildTestProduct("Updated model", BigDecimal.valueOf(1500));
        productDetails.setLength(220);
        productDetails.setHeight(90);
        productDetails.setDepth(80);
        productDetails.setColour("Black");
        productDetails.setRemarks("Updated remarks");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        Product updatedProduct = productService.updateProduct(1L, productDetails);

        assertEquals("Updated model", updatedProduct.getModel());
        assertEquals(0, BigDecimal.valueOf(1500).compareTo(updatedProduct.getPrice()));
        assertEquals(220, updatedProduct.getLength());
        assertEquals(90, updatedProduct.getHeight());
        assertEquals(80, updatedProduct.getDepth());
        assertEquals("Black", updatedProduct.getColour());
        assertEquals("Updated remarks", updatedProduct.getRemarks());
        verify(productRepository).save(existingProduct);
    }

    @Test
    void deleteProductByIdDeletesExistingProduct() {
        Product existingProduct = buildTestProduct("Test model", BigDecimal.valueOf(1200));
        existingProduct.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(orderItemRepository.existsByProductId(1L)).thenReturn(false);

        productService.deleteProductById(1L);

        verify(productRepository).delete(existingProduct);
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
    }

    private Product buildTestProduct(String model, BigDecimal price) {
        return Product.builder()
                .price(price)
                .length(200)
                .height(100)
                .depth(90)
                .colour("Brown")
                .model(model)
                .type("Bed")
                .material("Oak")
                .fabric("Leather")
                .remarks("Test product")
                .equipment("Storage")
                .build();
    }
}
