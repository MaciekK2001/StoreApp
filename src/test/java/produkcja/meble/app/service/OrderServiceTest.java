package produkcja.meble.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import produkcja.meble.app.dto.CreateOrderRequest;
import produkcja.meble.app.dto.OrderItemRequest;
import produkcja.meble.app.dto.OrderResponse;
import produkcja.meble.app.dto.UpdateOrderRequest;
import produkcja.meble.app.dto.UserResponse;
import produkcja.meble.app.entity.AccountRole;
import produkcja.meble.app.entity.Order;
import produkcja.meble.app.entity.OrderItem;
import produkcja.meble.app.entity.Product;
import produkcja.meble.app.entity.UserAccount;
import produkcja.meble.app.exception.ResourceNotFoundException;
import produkcja.meble.app.mapper.OrderMapper;
import produkcja.meble.app.repository.OrderRepository;
import produkcja.meble.app.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    private CreateOrderRequest orderRequest;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderRequest = buildTestOrderRequest();
        order = buildTestOrder();
        orderResponse = buildTestOrderResponse();
    }

    @Test
    void saveOrderSuccessfullyCreatesOrder() {
        Product product = Product.builder().id(1L).price(BigDecimal.valueOf(100)).build();
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse createdOrder = orderService.saveOrder(orderRequest);

        assertNotNull(createdOrder);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void saveOrderWithCustomizedOrderItemPreservesCustomAttributes() {
        OrderItemRequest customItem = OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .price(BigDecimal.valueOf(2500))
                .length(210.0)
                .depth(180.0)
                .height(115.0)
                .fabric("Velvet Green")
                .colour("Butelkowa zieleń")
                .material("Drewno dębowe")
                .equipment("Pojemnik na pościel")
                .remarks("Nóżki czarne matowe")
                .build();

        CreateOrderRequest customRequest = CreateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusDays(7))
                .address("Warszawa, Złota 44")
                .phoneNumber("987654321")
                .orderItemRequests(List.of(customItem))
                .build();

        Product catalogProduct = Product.builder()
                .id(1L)
                .price(BigDecimal.valueOf(1900))
                .length(200.0)
                .depth(160.0)
                .height(100.0)
                .fabric("Katalog Standard")
                .colour("Szary")
                .material("Sosna")
                .build();

        Order customOrder = buildTestOrder();
        when(orderMapper.toEntity(customRequest)).thenReturn(customOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(catalogProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        OrderResponse response = orderService.saveOrder(customRequest);

        assertNotNull(response);
        assertEquals(1, customOrder.getItems().size());
        OrderItem savedItem = customOrder.getItems().getFirst();
        assertEquals(BigDecimal.valueOf(2500), savedItem.getPrice());
        assertEquals(210.0, savedItem.getLength());
        assertEquals(180.0, savedItem.getDepth());
        assertEquals(115.0, savedItem.getHeight());
        assertEquals("Velvet Green", savedItem.getFabric());
        assertEquals("Butelkowa zieleń", savedItem.getColour());
        assertEquals("Drewno dębowe", savedItem.getMaterial());
        assertEquals("Pojemnik na pościel", savedItem.getEquipment());
        assertEquals("Nóżki czarne matowe", savedItem.getRemarks());
    }

    @Test
    void saveOrderWithUserAccountAssignsCreator() {
        UserAccount user = UserAccount.builder().id(10L).username("testuser").role(AccountRole.ADMIN).build();
        Product product = Product.builder().id(1L).price(BigDecimal.valueOf(100)).build();
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(
                OrderResponse.builder()
                        .id(1L)
                        .createdBy(UserResponse.from(user))
                        .build()
        );

        OrderResponse createdOrder = orderService.saveOrder(orderRequest, user);

        assertNotNull(createdOrder);
        assertNotNull(createdOrder.createdBy());
        assertEquals("testuser", createdOrder.createdBy().username());
        assertEquals(user, order.getCreatedBy());
    }

    @Test
    void saveOrderThrowsWhenProductMissing() {
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.saveOrder(orderRequest));
    }

    @Test
    void findAllActiveOrdersDelegatesToRepository() {
        OrderResponse expectedResponse = OrderResponse.builder().id(1L).build();
        when(orderRepository.findByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(order));
        when(orderMapper.toResponseList(List.of(order))).thenReturn(List.of(expectedResponse));

        List<OrderResponse> activeOrders = orderService.findAllActiveOrders();

        assertEquals(1, activeOrders.size());
        assertEquals(expectedResponse, activeOrders.getFirst());
    }

    @Test
    void updateOrderSuccessfullyUpdatesOrderAndItems() {
        UpdateOrderRequest updateRequest = UpdateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusDays(10))
                .address("Nowy Adres 5")
                .phoneNumber("555444333")
                .orderItemRequests(List.of(new OrderItemRequest(2L, 3)))
                .build();

        Product newProduct = Product.builder().id(2L).price(BigDecimal.valueOf(300)).build();
        Order existingOrder = buildTestOrder();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        when(orderMapper.toResponse(existingOrder)).thenReturn(orderResponse);

        OrderResponse result = orderService.updateOrder(1L, updateRequest);

        assertNotNull(result);
        verify(orderMapper).updateEntityFromRequest(updateRequest, existingOrder);
        verify(orderRepository).save(existingOrder);
        assertEquals(1, existingOrder.getItems().size());
        assertEquals(newProduct, existingOrder.getItems().getFirst().getProduct());
        assertEquals(3, existingOrder.getItems().getFirst().getQuantity());
    }

    @Test
    void deleteOrderByIdDeletesExistingOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    void setNotActiveDelegatesToRepository() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order()));

        orderService.setNotActive(1L);

        verify(orderRepository).softDeleteById(1L);
    }

    @Test
    void findOrderByIdReturnsOrder() {
        Order order = new Order();
        OrderResponse expectedResponse = OrderResponse.builder().id(1L).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        OrderResponse result = orderService.findOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void findOrderByIdThrowsWhenNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.findOrderById(1L));
    }

    private CreateOrderRequest buildTestOrderRequest() {
        return CreateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusDays(7))
                .address("Testowa 1")
                .phoneNumber("123456789")
                .orderItemRequests(List.of(new OrderItemRequest(1L, 2)))
                .build();
    }

    private Order buildTestOrder() {
        return Order.builder()
                .id(1L)
                .deliveryDate(LocalDateTime.now().plusDays(7))
                .address("Testowa 1")
                .phoneNumber("123456789")
                .active(true)
                .items(new ArrayList<>())
                .build();
    }

    private OrderResponse buildTestOrderResponse() {
        return OrderResponse.builder()
                .id(1L)
                .deliveryDate(LocalDateTime.now().plusDays(7))
                .address("Testowa 1")
                .phoneNumber("123456789")
                .build();
    }
}
