package produkcja.meble.app.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import produkcja.meble.app.Entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
