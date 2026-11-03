package produkcja.meble.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import produkcja.meble.app.entity.Order;
import produkcja.meble.app.entity.OrderStatus;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByActiveTrueOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findAllByOrderByCreatedAtDesc();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.active = false, o.status = produkcja.meble.app.entity.OrderStatus.CANCELLED WHERE o.id = :id")
    void softDeleteById(@Param("id") Long id);
}
