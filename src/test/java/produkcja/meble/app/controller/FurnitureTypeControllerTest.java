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
import produkcja.meble.app.dto.CreateFurnitureTypeRequest;
import produkcja.meble.app.dto.FurnitureTypeResponse;
import produkcja.meble.app.dto.ProductRequest;
import produkcja.meble.app.entity.UserAccount;
import produkcja.meble.app.repository.UserAccountRepository;
import produkcja.meble.app.service.FurnitureTypeService;
import produkcja.meble.app.service.JwtService;
import produkcja.meble.app.service.ProductService;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FurnitureTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private FurnitureTypeService furnitureTypeService;

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
    void shouldGetAllTypes() throws Exception {
        mockMvc.perform(get("/api/furniture-types")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldCreateAndDeleteFurnitureType() throws Exception {
        String typeName = "NiestandardowyTyp_" + System.currentTimeMillis();
        CreateFurnitureTypeRequest request = new CreateFurnitureTypeRequest(typeName);

        String response = mockMvc.perform(post("/api/furniture-types")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(typeName))
                .andReturn().getResponse().getContentAsString();

        FurnitureTypeResponse created = objectMapper.readValue(response, FurnitureTypeResponse.class);

        // Conflict on duplicate
        mockMvc.perform(post("/api/furniture-types")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        // Delete
        mockMvc.perform(delete("/api/furniture-types/" + created.id())
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldPreventDeletingUsedType() throws Exception {
        String typeName = "TypPrzypisany_" + System.currentTimeMillis();
        FurnitureTypeResponse created = furnitureTypeService.create(new CreateFurnitureTypeRequest(typeName));

        productService.saveProduct(ProductRequest.builder()
                .model("Model_Z_Typem_" + System.currentTimeMillis())
                .type(typeName)
                .price(BigDecimal.valueOf(100))
                .length(100)
                .depth(50)
                .height(80)
                .build());

        mockMvc.perform(delete("/api/furniture-types/" + created.id())
                        .header("Authorization", token))
                .andExpect(status().isConflict());
    }
}
