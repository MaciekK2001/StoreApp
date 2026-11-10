package produkcja.meble.app.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import produkcja.meble.app.dto.CreateOrderRequest;
import produkcja.meble.app.dto.OrderItemRequest;
import produkcja.meble.app.dto.ProductRequest;
import produkcja.meble.app.dto.ProductResponse;
import produkcja.meble.app.entity.AccountRole;
import produkcja.meble.app.entity.UserAccount;
import produkcja.meble.app.repository.OrderRepository;
import produkcja.meble.app.repository.ProductRepository;
import produkcja.meble.app.entity.FurnitureType;
import produkcja.meble.app.entity.Product;
import produkcja.meble.app.repository.FurnitureTypeRepository;
import produkcja.meble.app.repository.UserAccountRepository;
import produkcja.meble.app.service.OrderService;
import produkcja.meble.app.service.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class SampleDataLoader implements CommandLineRunner {

    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserAccountRepository userAccountRepository;
    private final FurnitureTypeRepository furnitureTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
        UserAccount admin = loadUsers();



        if (productRepository.count() > 0 || orderRepository.count() > 0) {
            return;
        }

        ProductResponse bed = productService.saveProduct(ProductRequest.builder()
                .price(BigDecimal.valueOf(2400))
                .length(200)
                .height(100)
                .depth(90)
                .colour("Dark brown")
                .model("Kanciate")
                .type("Łóżko")
                .material("Oak")
                .fabric("Leather")
                .remarks("Sample bed")
                .equipment("Storage box")
                .leadTimeWeeks(3)
                .build());

        ProductResponse sofa = productService.saveProduct(ProductRequest.builder()
                .price(BigDecimal.valueOf(3200))
                .length(260)
                .height(95)
                .depth(110)
                .colour("Grey")
                .model("Moderno")
                .type("Sofa")
                .material("Beech")
                .fabric("Velour")
                .remarks("Sample sofa")
                .equipment("Sleeping function")
                .leadTimeWeeks(4)
                .build());

        ProductResponse wardrobe = productService.saveProduct(ProductRequest.builder()
                .price(BigDecimal.valueOf(4100))
                .length(240)
                .height(220)
                .depth(65)
                .colour("White")
                .model("Praktik")
                .type("Szafa")
                .material("Laminated board")
                .fabric(null)
                .remarks("Sample wardrobe")
                .equipment("Mirror")
                .leadTimeWeeks(2)
                .build());

        orderService.saveOrder(CreateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusWeeks(4))
                .phoneNumber("123456789")
                .fulfillmentTime(4)
                .contactRemarks("Call after 16")
                .address("Warszawa, ul. Przykladowa 10")
                .assemblyPrice(BigDecimal.valueOf(200))
                .assemblyRemarks("Assembly on site")
                .discount(BigDecimal.valueOf(100))
                .discountRemarks("Regular customer discount")
                .transportPrice(BigDecimal.valueOf(150))
                .transportRemarks("Evening delivery")
                .remarks("Sample order with bed and sofa")
                .carryProductsPrice(BigDecimal.valueOf(80))
                .carryProductsRemarks("Third floor")
                .deposit(BigDecimal.valueOf(500))
                .orderItemRequests(List.of(
                        OrderItemRequest.builder()
                                .productId(bed.id())
                                .quantity(1)
                                .build(),
                        OrderItemRequest.builder()
                                .productId(sofa.id())
                                .quantity(1)
                                .build()
                ))
                .build(), admin);

        orderService.saveOrder(CreateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusWeeks(2))
                .phoneNumber("987654321")
                .fulfillmentTime(2)
                .contactRemarks("Email preferred")
                .address("Krakow, ul. Testowa 5")
                .assemblyPrice(BigDecimal.valueOf(300))
                .assemblyRemarks("Wall mounting required")
                .discount(BigDecimal.ZERO)
                .discountRemarks("")
                .transportPrice(BigDecimal.valueOf(180))
                .transportRemarks("Morning delivery")
                .remarks("Sample wardrobe order")
                .carryProductsPrice(BigDecimal.valueOf(120))
                .carryProductsRemarks("Elevator available")
                .deposit(BigDecimal.valueOf(700))
                .orderItemRequests(List.of(
                        OrderItemRequest.builder()
                                .productId(wardrobe.id())
                                .quantity(2)
                                .build()
                ))
                .build(), admin);
    }

    private UserAccount loadUsers() {
        userAccountRepository.findByUsername("user")
                .orElseGet(() -> userAccountRepository.save(UserAccount.builder()
                        .username("user")
                        .email("user@firma.pl")
                        .password(passwordEncoder.encode("user12345"))
                        .role(AccountRole.ADMIN)
                        .build()));

        return userAccountRepository.findByUsername("admin")
                .orElseGet(() -> userAccountRepository.save(UserAccount.builder()
                        .username("admin")
                        .email("admin@firma.pl")
                        .password(passwordEncoder.encode("admin1234"))
                        .role(AccountRole.ADMIN)
                        .build()));
    }
}
