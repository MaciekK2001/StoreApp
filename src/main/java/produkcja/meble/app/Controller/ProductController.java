package produkcja.meble.app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import produkcja.meble.app.dto.ProductRequest;
import produkcja.meble.app.dto.ProductResponse;
import produkcja.meble.app.entity.FurnitureType;
import produkcja.meble.app.exception.BadRequestException;
import produkcja.meble.app.service.FileStorageService;
import produkcja.meble.app.service.ProductService;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(@RequestParam(required = false) List<FurnitureType> types) {
        return ResponseEntity.ok(productService.findAllProducts(types));
    }

    @GetMapping("/types")
    public ResponseEntity<FurnitureType[]> getFurnitureTypes() {
        return ResponseEntity.ok(FurnitureType.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.saveProduct(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        MultipartFile targetFile = file != null ? file : image;
        if (targetFile == null || targetFile.isEmpty()) {
            throw new BadRequestException("Wymagany jest plik ze zdjęciem (parametr 'file' lub 'image').");
        }
        String imageUrl = fileStorageService.storeFile(targetFile);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ignored) {
        }
        if (contentType == null) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".png")) {
                contentType = "image/png";
            } else if (lower.endsWith(".webp")) {
                contentType = "image/webp";
            } else {
                contentType = "image/jpeg";
            }
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(resource);
    }
}
