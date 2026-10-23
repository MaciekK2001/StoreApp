package produkcja.meble.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import produkcja.meble.app.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByProductId(Long productId);
}
