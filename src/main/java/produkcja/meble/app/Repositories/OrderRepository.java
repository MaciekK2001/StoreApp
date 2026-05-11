package produkcja.meble.app.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import produkcja.meble.app.Entities.Order;

import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByIsActiveTrueOrderByCreatedAtDesc();

    void deleteById(Long id);

    @Modifying
    @Query("update Order o set o.isActive = false where o.id = :id")
    void softDeleteById(@Param("id") Long id);

}
