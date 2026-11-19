package produkcja.meble.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import produkcja.meble.app.entity.AppMetadata;

public interface AppMetadataRepository extends JpaRepository<AppMetadata, String> {
}
