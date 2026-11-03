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
import produkcja.meble.app.entity.OrderStatus;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void saveOrderCreatesOrderWithUser() {
        UserAccount user = UserAccount.builder()
                .id(10L)
                .username("testuser")
                .role(AccountRole.ADMIN)
                .build();
        Product product = new Product();
        product.setPrice(BigDecimal.valueOf(100));

        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.saveOrder(orderRequest, user);

        assertNotNull(result);
        assertEquals(user, order.getCreatedBy());
        verify(orderRepository).save(order);
    }

    @Test
    void saveOrderThrowsWhenProductNotFound() {
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.saveOrder(orderRequest));
    }

    @Test
    void saveOrderWithCustomizedOrderItemPreservesCustomAttributes() {
        Product baseProduct = Product.builder()
                .id(1L)
                .price(BigDecimal.valueOf(1000))
                .length(200.0)
                .depth(90.0)
                .height(80.0)
                .colour("Czarny")
                .material("Drewno")
                .fabric("Welur")
                .equipment("Szuflady")
                .remarks("Standard")
                .build();

        OrderItemRequest customItemRequest = OrderItemRequest.builder()
                .productId(1L)
                .quantity(2)
                .price(BigDecimal.valueOf(1250.50))
                .length(220.0)
                .depth(95.0)
                .height(85.0)
                .colour("Biały mat")
                .material("MDF lakierowany")
                .fabric("Skóra naturalna")
                .equipment("Cichy domyk, oświetlenie LED")
                .remarks("Niestandardowe wymiary na zamówienie klienta")
                .build();

        CreateOrderRequest requestWithCustomItem = CreateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusDays(10))
                .address("Warszawska 5")
                .phoneNumber("987654321")
                .orderItemRequests(List.of(customItemRequest))
                .build();

        Order blankOrder = new Order();
        when(orderMapper.toEntity(requestWithCustomItem)).thenReturn(blankOrder);
        when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

        orderService.saveOrder(requestWithCustomItem);

        assertEquals(1, blankOrder.getItems().size());
        OrderItem savedItem = blankOrder.getItems().getFirst();
        assertEquals(baseProduct, savedItem.getProduct());
        assertEquals(2, savedItem.getQuantity());
        assertEquals(BigDecimal.valueOf(1250.50), savedItem.getPrice());
        assertEquals(220.0, savedItem.getLength());
        assertEquals(95.0, savedItem.getDepth());
        assertEquals(85.0, savedItem.getHeight());
        assertEquals("Biały mat", savedItem.getColour());
        assertEquals("MDF lakierowany", savedItem.getMaterial());
        assertEquals("Skóra naturalna", savedItem.getFabric());
        assertEquals("Cichy domyk, oświetlenie LED", savedItem.getEquipment());
        assertEquals("Niestandardowe wymiary na zamówienie klienta", savedItem.getRemarks());
    }

    @Test
    void findAllActiveOrdersReturnsList() {
        when(orderRepository.findByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(order));
        when(orderMapper.toResponseList(List.of(order))).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderService.findAllActiveOrders();

        assertEquals(1, result.size());
        verify(orderRepository).findByActiveTrueOrderByCreatedAtDesc();
    }

    @Test
    void findOrdersByStatusCallsRepository() {
        when(orderRepository.findByStatusOrderByCreatedAtDesc(OrderStatus.COMPLETED)).thenReturn(List.of(order));
        when(orderMapper.toResponseList(List.of(order))).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderService.findOrdersByStatus(OrderStatus.COMPLETED);

        assertEquals(1, result.size());
        verify(orderRepository).findByStatusOrderByCreatedAtDesc(OrderStatus.COMPLETED);
    }

    @Test
    void setOrderStatusUpdatesStatusAndActiveStatus() {
        Order existingOrder = new Order();
        existingOrder.setStatus(OrderStatus.ACTIVE);
        existingOrder.setActive(true);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);
        when(orderMapper.toResponse(existingOrder)).thenReturn(orderResponse);

        OrderResponse res = orderService.setOrderStatus(1L, OrderStatus.COMPLETED);

        assertNotNull(res);
        assertEquals(OrderStatus.COMPLETED, existingOrder.getStatus());
        assertFalse(existingOrder.isActive());

        orderService.setOrderStatus(1L, OrderStatus.ACTIVE);
        assertEquals(OrderStatus.ACTIVE, existingOrder.getStatus());
        assertTrue(existingOrder.isActive());

        orderService.setOrderStatus(1L, OrderStatus.CANCELLED);
        assertEquals(OrderStatus.CANCELLED, existingOrder.getStatus());
        assertFalse(existingOrder.isActive());
    }

    @Test
    void findAllOrdersReturnsList() {
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(order));
        when(orderMapper.toResponseList(List.of(order))).thenReturn(List.of(orderResponse));

        List<OrderResponse> result = orderService.findAllOrders();

        assertEquals(1, result.size());
        verify(orderRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void updateOrderUpdatesFieldsAndItems() {
        Order existingOrder = new Order();
        existingOrder.setId(1L);
        existingOrder.setItems(new ArrayList<>());
        OrderItem oldItem = new OrderItem();
        existingOrder.addItem(oldItem);

        Product newProduct = new Product();
        newProduct.setId(2L);
        newProduct.setPrice(BigDecimal.valueOf(50));

        UpdateOrderRequest updateRequest = UpdateOrderRequest.builder()
                .deliveryDate(LocalDateTime.now().plusDays(5))
                .address("Nowa 2")
                .phoneNumber("987654321")
                .active(false)
                .orderItemRequests(List.of(new OrderItemRequest(2L, 3)))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existingOrder));
        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));
        when(orderRepository.save(existingOrder)).thenReturn(existingOrder);
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
                .status(OrderStatus.ACTIVE)
                .items(new ArrayList<>())
                .build();
    }

    private OrderResponse buildTestOrderResponse() {
        return OrderResponse.builder()
                .id(1L)
                .deliveryDate(LocalDateTime.now().plusDays(7))
                .address("Testowa 1")
                .phoneNumber("123456789")
                .status(OrderStatus.ACTIVE)
                .build();
    }
}
