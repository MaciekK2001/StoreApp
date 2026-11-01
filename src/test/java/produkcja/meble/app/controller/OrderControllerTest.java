package produkcja.meble.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import produkcja.meble.app.entity.UserAccount;
import produkcja.meble.app.repository.UserAccountRepository;
import produkcja.meble.app.service.JwtService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private String token;

    @BeforeEach
    void setUp() {
        UserAccount admin = userAccountRepository.findByUsername("admin").orElseThrow();
        token = "Bearer " + jwtService.generateToken(admin);
    }

    @Test
    void getActiveOrdersReturnsList() throws Exception {
        mockMvc.perform(get("/api/orders/active")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllOrdersReturnsList() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void softDeleteOrderReturnsNoContent() throws Exception {
        mockMvc.perform(patch("/api/orders/1/soft-not-active")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateOrderReturnsOk() throws Exception {
        String json = """
                {
                    "deliveryDate": "2030-10-25T14:30:00",
                    "phoneNumber": "555444333",
                    "fulfillmentTime": 10,
                    "address": "Zaktualizowana 20",
                    "assemblyPrice": 100,
                    "discount": 0,
                    "transportPrice": 50,
                    "carryProductsPrice": 0,
                    "deposit": 200,
                    "orderItemRequests": [
                        {
                            "productId": 1,
                            "quantity": 1
                        }
                    ]
                }
                """;

        mockMvc.perform(put("/api/orders/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Zaktualizowana 20"))
                .andExpect(jsonPath("$.phoneNumber").value("555444333"));
    }

    @Test
    void deleteOrderReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/2")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }
}
