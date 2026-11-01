package produkcja.meble.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import produkcja.meble.app.dto.ProductRequest;
import produkcja.meble.app.dto.ProductResponse;
import produkcja.meble.app.entity.UserAccount;
import produkcja.meble.app.repository.UserAccountRepository;
import produkcja.meble.app.service.JwtService;
import produkcja.meble.app.service.ProductService;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    @Test
    void uploadAndRetrieveImageStoredInDatabase() throws Exception {
        byte[] testImageContent = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-sofa.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                testImageContent
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/products/images")
                        .file(file)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").isString())
                .andReturn();

        String responseJson = uploadResult.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, String> body = objectMapper.readValue(responseJson, Map.class);
        String imageUrl = body.get("imageUrl");
        assertNotNull(imageUrl);

        // GET bez autoryzacji (zdjęcia są publiczne, np. dla Coil w Androidzie)
        MvcResult imageGetResult = mockMvc.perform(get(imageUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("Cache-Control", "public, max-age=86400"))
                .andReturn();

        assertArrayEquals(testImageContent, imageGetResult.getResponse().getContentAsByteArray());
    }

    @Test
    void getImageNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/products/images/nonexistent-image.jpg"))
                .andExpect(status().isNotFound());
    }
}
