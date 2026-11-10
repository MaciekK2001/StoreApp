package produkcja.meble.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import produkcja.meble.app.entity.FurnitureType;

import java.util.Optional;

public interface FurnitureTypeRepository extends JpaRepository<FurnitureType, Long> {
    Optional<FurnitureType> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
