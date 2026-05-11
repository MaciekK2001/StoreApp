package produkcja.meble.app.Config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import produkcja.meble.app.DTO.CreateOrderRequest;
import produkcja.meble.app.DTO.OrderItemRequest;
import produkcja.meble.app.Entities.AccountRole;
import produkcja.meble.app.Entities.Product;
import produkcja.meble.app.Entities.UserAccount;
import produkcja.meble.app.Repositories.OrderRepository;
import produkcja.meble.app.Repositories.ProductRepository;
import produkcja.meble.app.Repositories.UserAccountRepository;
import produkcja.meble.app.Services.OrderService;
import produkcja.meble.app.Services.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
public class SampleDataLoader implements CommandLineRunner {

    private final ProductService productService;
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public SampleDataLoader(
            ProductService productService,
            OrderService orderService,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.productService = productService;
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String @NonNull ... args) {
        loadUsers();

        if (productRepository.count() > 0 || orderRepository.count() > 0) {
            return;
        }

        Product bed = productService.saveProduct(Product.builder()
                .price(BigDecimal.valueOf(2400))
                .length(200)
                .height(100)
                .depth(90)
                .colour("Dark brown")
                .model("Kanciate")
                .type("Bed")
                .material("Oak")
                .fabric("Leather")
                .remarks("Sample bed")
                .equipment("Storage box")
                .build());

        Product sofa = productService.saveProduct(Product.builder()
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
                .build());

        Product wardrobe = productService.saveProduct(Product.builder()
                .price(BigDecimal.valueOf(4100))
                .length(240)
                .height(220)
                .depth(65)
                .colour("White")
                .model("Praktik")
                .type("Wardrobe")
                .material("Laminated board")
                .fabric("None")
                .remarks("Sample wardrobe")
                .equipment("Mirror")
                .build());

        orderService.saveOrder(CreateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusDays(14))
                .phoneNumber("123456789")
                .fulfillmentTime(14)
                .contactRemarks("Call after 16")
                .address("Warszawa, ul. Przykladowa 10")
                .assemblyPrice(200)
                .assemblyRemarks("Assembly on site")
                .discount(100)
                .discountRemarks("Regular customer discount")
                .transportPrice(150)
                .transportRemarks("Evening delivery")
                .remarks("Sample order with bed and sofa")
                .carryProductsPrice(80)
                .carryProductsRemarks("Third floor")
                .deposit(500)
                .orderItemRequests(List.of(
                        OrderItemRequest.builder()
                                .productId(bed.getId())
                                .quantity(1)
                                .build(),
                        OrderItemRequest.builder()
                                .productId(sofa.getId())
                                .quantity(1)
                                .build()
                ))
                .build());

        orderService.saveOrder(CreateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusDays(21))
                .phoneNumber("987654321")
                .fulfillmentTime(21)
                .contactRemarks("Email preferred")
                .address("Krakow, ul. Testowa 5")
                .assemblyPrice(300)
                .assemblyRemarks("Wall mounting required")
                .discount(0)
                .discountRemarks("")
                .transportPrice(180)
                .transportRemarks("Morning delivery")
                .remarks("Sample wardrobe order")
                .carryProductsPrice(120)
                .carryProductsRemarks("Elevator available")
                .deposit(700)
                .orderItemRequests(List.of(
                        OrderItemRequest.builder()
                                .productId(wardrobe.getId())
                                .quantity(2)
                                .build()
                ))
                .build());
    }

    private void loadUsers() {
        if (!userAccountRepository.existsByUsername("admin")) {
            userAccountRepository.save(UserAccount.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin1234"))
                    .role(AccountRole.ADMIN)
                    .build());
        }

        if (!userAccountRepository.existsByUsername("user")) {
            userAccountRepository.save(UserAccount.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user12345"))
                    .role(AccountRole.USER)
                    .build());
        }
    }
}
