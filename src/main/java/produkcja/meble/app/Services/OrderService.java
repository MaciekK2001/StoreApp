package produkcja.meble.app.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import produkcja.meble.app.DTO.CreateOrderRequest;
import produkcja.meble.app.DTO.OrderItemRequest;
import produkcja.meble.app.Entities.Order;
import produkcja.meble.app.Entities.OrderItem;
import produkcja.meble.app.Entities.Product;
import produkcja.meble.app.Exceptions.ResourceNotFoundException;
import produkcja.meble.app.Mappers.OrderMapper;
import produkcja.meble.app.Repositories.OrderRepository;
import produkcja.meble.app.Repositories.ProductRepository;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final OrderMapper orderMapper;

    OrderService(OrderRepository orderRepository, ProductRepository productRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public void saveOrder(CreateOrderRequest createOrderRequest) {
        Order order = orderMapper.toEntity(createOrderRequest);

        for (OrderItemRequest itemRequest : createOrderRequest.getOrderItemRequests()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.getProductId()));

            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addItem(item);
        }

        orderRepository.save(order);
    }

    public List<Order> findAllActiveOrders() {
        return orderRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public void deleteOrderById(Long id) {
        Order order = findOrderById(id);
        orderRepository.delete(order);
    }

    @Transactional
    public void softDeleteOrderById(Long id) {
        findOrderById(id);
        orderRepository.softDeleteById(id);
    }

    public Order findOrderById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

}
