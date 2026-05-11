package produkcja.meble.app.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import produkcja.meble.app.DTO.CreateOrderRequest;
import produkcja.meble.app.DTO.OrderItemRequest;
import produkcja.meble.app.Entities.Order;
import produkcja.meble.app.Entities.Product;
import produkcja.meble.app.Mappers.OrderMapper;
import produkcja.meble.app.Repositories.OrderRepository;
import produkcja.meble.app.Repositories.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void saveOrderCreatesOrderItemsFromRequestProducts() {
        Product product = Product.builder()
                .price(BigDecimal.valueOf(2400))
                .model("Kanciate")
                .build();
        product.setId(1L);
        Order order = new Order();
        CreateOrderRequest request = buildCreateOrderRequest(1L);

        when(orderMapper.toEntity(request)).thenReturn(order);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        orderService.saveOrder(request);

        assertEquals(1, order.getItems().size());
        verify(orderRepository).save(order);
    }

    @Test
    void saveOrderThrowsWhenProductDoesNotExist() {
        CreateOrderRequest request = buildCreateOrderRequest(1L);

        when(orderMapper.toEntity(request)).thenReturn(new Order());
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.saveOrder(request)
        );

        assertEquals("Product with id 1 not found", exception.getMessage());
    }

    @Test
    void findAllActiveOrdersReturnsRepositoryOrders() {
        Order order = new Order();

        when(orderRepository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(order));

        List<Order> activeOrders = orderService.findAllActiveOrders();

        assertEquals(1, activeOrders.size());
        assertSame(order, activeOrders.getFirst());
    }

    @Test
    void softDeleteOrderByIdDelegatesToRepository() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));

        orderService.softDeleteOrderById(1L);

        verify(orderRepository).softDeleteById(1L);
    }

    @Test
    void findOrderByIdReturnsOrder() {
        Order order = new Order();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.findOrderById(1L);

        assertSame(order, result);
    }

    @Test
    void findOrderByIdThrowsWhenOrderDoesNotExist() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderService.findOrderById(1L)
        );

        assertEquals("Order with id 1 not found", exception.getMessage());
    }

    private CreateOrderRequest buildCreateOrderRequest(Long productId) {
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        return CreateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusDays(7))
                .phoneNumber("123456789")
                .fulfillmentTime(14)
                .contactRemarks("Call after 16")
                .address("Warszawa, ul. Przykladowa 10")
                .assemblyPrice(200.0)
                .assemblyRemarks("Assembly on site")
                .discount(100.0)
                .discountRemarks("Regular customer discount")
                .transportPrice(150.0)
                .transportRemarks("Evening delivery")
                .remarks("Test order")
                .carryProductsPrice(80.0)
                .carryProductsRemarks("Third floor")
                .deposit(500.0)
                .orderItemRequests(List.of(itemRequest))
                .build();
    }
}
