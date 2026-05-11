package produkcja.meble.app.Controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import produkcja.meble.app.DTO.CreateOrderRequest;
import produkcja.meble.app.Entities.Order;
import produkcja.meble.app.Entities.Product;
import produkcja.meble.app.Services.OrderService;
import produkcja.meble.app.Services.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AppController {

    private final ProductService productService;
    private final OrderService orderService;

    public AppController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/products")
    public List<Product> getProducts() {
        return productService.findAllProducts();
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findProductById(id);
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/orders/active")
    public List<Order> getActiveOrders() {
        return orderService.findAllActiveOrders();
    }

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.findOrderById(id);
    }

    @PostMapping("/orders")
    public ResponseEntity<Void> createOrder(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        orderService.saveOrder(createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/orders/{id}/soft-delete")
    public ResponseEntity<Void> softDeleteOrder(@PathVariable Long id) {
        orderService.softDeleteOrderById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.noContent().build();
    }
}
