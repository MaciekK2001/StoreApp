package produkcja.meble.app.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import produkcja.meble.app.Entities.Product;
import produkcja.meble.app.Exceptions.BadRequestException;
import produkcja.meble.app.Exceptions.ConflictException;
import produkcja.meble.app.Exceptions.ResourceNotFoundException;
import produkcja.meble.app.Repositories.OrderItemRepository;
import produkcja.meble.app.Repositories.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Product saveProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        validateProduct(productDetails);
        Product product = findProductById(id);

        product.setPrice(productDetails.getPrice());
        product.setLength(productDetails.getLength());
        product.setDepth(productDetails.getDepth());
        product.setHeight(productDetails.getHeight());
        product.setColour(productDetails.getColour());
        product.setModel(productDetails.getModel());
        product.setType(productDetails.getType());
        product.setMaterial(productDetails.getMaterial());
        product.setFabric(productDetails.getFabric());
        product.setRemarks(productDetails.getRemarks());
        product.setEquipment(productDetails.getEquipment());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProductById(Long id) {
        Product product = findProductById(id);

        if (orderItemRepository.existsByProductId(id)) {
            throw new ConflictException("Product with id " + id + " is used in existing orders");
        }

        productRepository.delete(product);
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new BadRequestException("Product request body is required");
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Product price must be greater than or equal to 0");
        }

        if (product.getModel() == null || product.getModel().isBlank()) {
            throw new BadRequestException("Product model is required");
        }

        if (product.getLength() < 0 || product.getDepth() < 0 || product.getHeight() < 0) {
            throw new BadRequestException("Product dimensions must be greater than or equal to 0");
        }
    }
}
