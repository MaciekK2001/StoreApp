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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        String trimmedModel = request.model() != null ? request.model().trim() : "";
        if (StringUtils.hasText(trimmedModel) && productRepository.existsByModelIgnoreCase(trimmedModel)) {
            throw new ConflictException("Produkt o modelu '" + trimmedModel + "' już istnieje. Nazwa modelu musi być unikalna.");
        }

        Product product = productMapper.toEntity(request);
        product.setModel(trimmedModel);
        if (request.additionalImages() != null) {
            product.setAdditionalImages(new ArrayList<>(request.additionalImages()));
        } else if (product.getAdditionalImages() == null) {
            product.setAdditionalImages(new ArrayList<>());
        }
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
        String trimmedModel = request.model() != null ? request.model().trim() : "";
        if (StringUtils.hasText(trimmedModel) && productRepository.existsByModelIgnoreCaseAndIdNot(trimmedModel, id)) {
            throw new ConflictException("Produkt o modelu '" + trimmedModel + "' już istnieje. Nazwa modelu musi być unikalna.");
        }

        String oldImageUrl = product.getImageUrl();
        List<String> oldAdditionalImages = new ArrayList<>(
                product.getAdditionalImages() != null ? product.getAdditionalImages() : List.of()
        );

        productMapper.updateEntityFromRequest(request, product);
        product.setModel(trimmedModel);
        if (request.additionalImages() != null) {
            product.setAdditionalImages(new ArrayList<>(request.additionalImages()));
        } else if (product.getAdditionalImages() == null) {
            product.setAdditionalImages(new ArrayList<>());
        }

        Product updatedProduct = productRepository.save(product);

        Set<String> retainedImages = new HashSet<>();
        if (StringUtils.hasText(product.getImageUrl())) {
            retainedImages.add(product.getImageUrl());
        }
        if (product.getAdditionalImages() != null) {
            retainedImages.addAll(product.getAdditionalImages());
        }

        if (StringUtils.hasText(oldImageUrl) && !retainedImages.contains(oldImageUrl)) {
            fileStorageService.deleteFile(oldImageUrl);
        }
        for (String oldImg : oldAdditionalImages) {
            if (StringUtils.hasText(oldImg) && !retainedImages.contains(oldImg)) {
                fileStorageService.deleteFile(oldImg);
            }
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
        List<String> additionalImages = new ArrayList<>(
                product.getAdditionalImages() != null ? product.getAdditionalImages() : List.of()
        );

        productRepository.delete(product);

        if (StringUtils.hasText(imageUrl)) {
            fileStorageService.deleteFile(imageUrl);
        }
        for (String additionalImage : additionalImages) {
            if (StringUtils.hasText(additionalImage)) {
                fileStorageService.deleteFile(additionalImage);
            }
        }
    }
}
