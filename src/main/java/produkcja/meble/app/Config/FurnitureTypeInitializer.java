package produkcja.meble.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import produkcja.meble.app.entity.FurnitureType;
import produkcja.meble.app.entity.Product;
import produkcja.meble.app.repository.FurnitureTypeRepository;
import produkcja.meble.app.repository.ProductRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FurnitureTypeInitializer implements ApplicationRunner {

    private final FurnitureTypeRepository furnitureTypeRepository;
    private final ProductRepository productRepository;

    private static final List<String> DEFAULT_TYPES = List.of(
            "Krzesło", "Fotel", "Sofa", "Narożnik", "Stół", "Stolik kawowy",
            "Biurko", "Łóżko", "Szafa", "Regał", "Komoda", "Szafka nocna", "Półka"
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 1. Zapewnij domyślne typy w bazie danych dla każdego środowiska (w tym Render / Prod)
        for (String typeName : DEFAULT_TYPES) {
            if (!furnitureTypeRepository.existsByNameIgnoreCase(typeName)) {
                furnitureTypeRepository.save(FurnitureType.builder().name(typeName).build());
                log.info("Zainicjalizowano domyślny typ mebla: {}", typeName);
            }
        }

        // 2. Migracja starych angielskich enumów w istniejących produktach (np. BED -> Łóżko, SOFA -> Sofa)
        for (Product p : productRepository.findAll()) {
            if (p.getType() != null && !p.getType().isBlank()) {
                String currentType = p.getType().trim();
                String normalizedType = switch (currentType.toUpperCase()) {
                    case "BED" -> "Łóżko";
                    case "SOFA" -> "Sofa";
                    case "WARDROBE" -> "Szafa";
                    case "CHAIR" -> "Krzesło";
                    case "ARMCHAIR" -> "Fotel";
                    case "CORNER_SOFA" -> "Narożnik";
                    case "TABLE" -> "Stół";
                    case "COFFEE_TABLE" -> "Stolik kawowy";
                    case "DESK" -> "Biurko";
                    case "BOOKCASE" -> "Regał";
                    case "COMMODE" -> "Komoda";
                    case "NIGHTSTAND" -> "Szafka nocna";
                    case "SHELF" -> "Półka";
                    default -> currentType;
                };

                if (!normalizedType.equals(currentType)) {
                    p.setType(normalizedType);
                    productRepository.save(p);
                    log.info("Zmigrowano typ produktu #{} z '{}' na '{}'", p.getId(), currentType, normalizedType);
                }

                // 3. Upewnij się, że każdy typ aktualnie używany przez istniejące produkty istnieje w tabeli typów
                if (!furnitureTypeRepository.existsByNameIgnoreCase(p.getType())) {
                    furnitureTypeRepository.save(FurnitureType.builder().name(p.getType()).build());
                    log.info("Dodano brakujący typ mebla z istniejącego produktu: {}", p.getType());
                }
            }
        }
    }
}
