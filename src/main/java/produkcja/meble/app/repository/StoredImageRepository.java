package produkcja.meble.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import produkcja.meble.app.entity.StoredImage;

import java.util.Optional;

@Repository
public interface StoredImageRepository extends JpaRepository<StoredImage, Long> {
    Optional<StoredImage> findByFileName(String fileName);
    void deleteByFileName(String fileName);
    boolean existsByFileName(String fileName);
}
