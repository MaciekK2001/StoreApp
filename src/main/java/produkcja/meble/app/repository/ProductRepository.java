package produkcja.meble.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import produkcja.meble.app.entity.Product;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTypeIn(Collection<String> types);
    boolean existsByModelIgnoreCase(String model);
    boolean existsByModelIgnoreCaseAndIdNot(String model, Long id);
    boolean existsByTypeIgnoreCase(String type);
}
