package produkcja.meble.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import produkcja.meble.app.dto.CreateOrderRequest;
import produkcja.meble.app.dto.OrderItemRequest;
import produkcja.meble.app.dto.OrderResponse;
import produkcja.meble.app.dto.UpdateOrderRequest;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse saveOrder(CreateOrderRequest createOrderRequest) {
        return saveOrder(createOrderRequest, null);
    }

    @Transactional
    public OrderResponse saveOrder(CreateOrderRequest createOrderRequest, UserAccount userAccount) {
        Order order = orderMapper.toEntity(createOrderRequest);
        order.setCreatedBy(userAccount);

        if (order.getAssemblyPrice() == null) order.setAssemblyPrice(BigDecimal.ZERO);
        if (order.getDiscount() == null) order.setDiscount(BigDecimal.ZERO);
        if (order.getTransportPrice() == null) order.setTransportPrice(BigDecimal.ZERO);
        if (order.getCarryProductsPrice() == null) order.setCarryProductsPrice(BigDecimal.ZERO);
        if (order.getDeposit() == null) order.setDeposit(BigDecimal.ZERO);

        for (OrderItemRequest itemRequest : createOrderRequest.orderItemRequests()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.productId()));

            OrderItem item = buildOrderItem(itemRequest, product);
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    public List<OrderResponse> findAllActiveOrders() {
        List<Order> activeOrders = orderRepository.findByActiveTrueOrderByCreatedAtDesc();
        return orderMapper.toResponseList(activeOrders);
    }

    public List<OrderResponse> findOrdersByStatus(OrderStatus status) {
        if (status == null) {
            return findAllOrders();
        }
        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orderMapper.toResponseList(orders);
    }

    public List<OrderResponse> findAllOrders() {
        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc();
        return orderMapper.toResponseList(allOrders);
    }

    public OrderResponse findOrderById(Long id) {
        Order order = findEntityById(id);
        return orderMapper.toResponse(order);
    }

    public Order findEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Transactional
    public OrderResponse updateOrder(Long id, UpdateOrderRequest request) {
        Order order = findEntityById(id);
        orderMapper.updateEntityFromRequest(request, order);

        if (request.status() != null) {
            order.setStatus(request.status());
        } else if (request.active() != null) {
            order.setActive(request.active());
        }

        if (order.getAssemblyPrice() == null) order.setAssemblyPrice(BigDecimal.ZERO);
        if (order.getDiscount() == null) order.setDiscount(BigDecimal.ZERO);
        if (order.getTransportPrice() == null) order.setTransportPrice(BigDecimal.ZERO);
        if (order.getCarryProductsPrice() == null) order.setCarryProductsPrice(BigDecimal.ZERO);
        if (order.getDeposit() == null) order.setDeposit(BigDecimal.ZERO);

        if (request.orderItemRequests() != null) {
            order.getItems().clear();
            for (OrderItemRequest itemRequest : request.orderItemRequests()) {
                Product product = productRepository.findById(itemRequest.productId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.productId()));

                OrderItem item = buildOrderItem(itemRequest, product);
                order.addItem(item);
            }
        }

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }

    private OrderItem buildOrderItem(OrderItemRequest itemRequest, Product product) {
        BigDecimal price = (itemRequest.price() != null && itemRequest.price().compareTo(BigDecimal.ZERO) >= 0)
                ? itemRequest.price()
                : (product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);

        Double length = itemRequest.length() != null ? itemRequest.length() : product.getLength();
        Double depth = itemRequest.depth() != null ? itemRequest.depth() : product.getDepth();
        Double height = itemRequest.height() != null ? itemRequest.height() : product.getHeight();
        String colour = (itemRequest.colour() != null && !itemRequest.colour().isBlank()) ? itemRequest.colour().trim() : product.getColour();
        String material = (itemRequest.material() != null && !itemRequest.material().isBlank()) ? itemRequest.material().trim() : product.getMaterial();
        String fabric = (itemRequest.fabric() != null && !itemRequest.fabric().isBlank()) ? itemRequest.fabric().trim() : product.getFabric();
        String equipment = (itemRequest.equipment() != null && !itemRequest.equipment().isBlank()) ? itemRequest.equipment().trim() : product.getEquipment();
        String remarks = (itemRequest.remarks() != null && !itemRequest.remarks().isBlank()) ? itemRequest.remarks().trim() : product.getRemarks();

        return OrderItem.builder()
                .product(product)
                .quantity(itemRequest.quantity())
                .price(price)
                .length(length)
                .depth(depth)
                .height(height)
                .colour(colour)
                .material(material)
                .fabric(fabric)
                .equipment(equipment)
                .remarks(remarks)
                .build();
    }

    @Transactional
    public void deleteOrderById(Long id) {
        Order order = findEntityById(id);
        orderRepository.delete(order);
    }

    @Transactional
    public void setNotActive(Long id) {
        findEntityById(id);
        orderRepository.softDeleteById(id);
    }

    @Transactional
    public OrderResponse setOrderActiveStatus(Long id, boolean active) {
        Order order = findEntityById(id);
        order.setActive(active);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse setOrderStatus(Long id, OrderStatus status) {
        Order order = findEntityById(id);
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }
}
