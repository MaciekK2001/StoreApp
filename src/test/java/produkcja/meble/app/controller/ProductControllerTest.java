package produkcja.meble.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import produkcja.meble.app.dto.ProductRequest;
import produkcja.meble.app.dto.ProductResponse;
import produkcja.meble.app.entity.UserAccount;
import produkcja.meble.app.repository.UserAccountRepository;
import produkcja.meble.app.service.JwtService;
import produkcja.meble.app.service.ProductService;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @BeforeEach
    void setUp() {
        UserAccount admin = userAccountRepository.findByUsername("admin").orElseThrow();
        token = "Bearer " + jwtService.generateToken(admin);
    }

    @Test
    void getProductsReturnsOkAndList() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createProductReturnsCreatedWithLocation() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .price(BigDecimal.valueOf(1500))
                .model("Fotel Relax")
                .length(90)
                .height(100)
                .depth(85)
                .build();

        mockMvc.perform(post("/api/products")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.model").value("Fotel Relax"));
    }

    @Test
    void updateProductReturnsOk() throws Exception {
        ProductResponse created = productService.saveProduct(ProductRequest.builder()
                .price(BigDecimal.valueOf(800))
                .model("Półka ścienna")
                .length(100)
                .height(30)
                .depth(20)
                .build());

        ProductRequest updateRequest = ProductRequest.builder()
                .price(BigDecimal.valueOf(950))
                .model("Półka ścienna Premium")
                .length(110)
                .height(35)
                .depth(25)
                .build();

        mockMvc.perform(put("/api/products/" + created.id())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Półka ścienna Premium"))
                .andExpect(jsonPath("$.price").value(950));
    }

    @Test
    void deleteProductReturnsNoContent() throws Exception {
        ProductResponse created = productService.saveProduct(ProductRequest.builder()
                .price(BigDecimal.valueOf(500))
                .model("Taboret do usunięcia")
                .length(40)
                .height(45)
                .depth(40)
                .build());

        mockMvc.perform(delete("/api/products/" + created.id())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void unauthenticatedRequestReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }
}
