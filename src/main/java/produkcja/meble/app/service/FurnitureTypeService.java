package produkcja.meble.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import produkcja.meble.app.dto.CreateFurnitureTypeRequest;
import produkcja.meble.app.dto.FurnitureTypeResponse;
import produkcja.meble.app.entity.FurnitureType;
import produkcja.meble.app.exception.ConflictException;
import produkcja.meble.app.exception.ResourceNotFoundException;
import produkcja.meble.app.repository.FurnitureTypeRepository;
import produkcja.meble.app.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FurnitureTypeService {

    private final FurnitureTypeRepository furnitureTypeRepository;
    private final ProductRepository productRepository;

    public List<FurnitureTypeResponse> findAll() {
        return furnitureTypeRepository.findAll().stream()
                .map(t -> new FurnitureTypeResponse(t.getId(), t.getName()))
                .toList();
    }

    @Transactional
    public FurnitureTypeResponse create(CreateFurnitureTypeRequest request) {
        String trimmed = request.name().trim();
        if (furnitureTypeRepository.existsByNameIgnoreCase(trimmed)) {
            throw new ConflictException("Typ mebla o nazwie '" + trimmed + "' już istnieje.");
        }
        FurnitureType entity = FurnitureType.builder()
                .name(trimmed)
                .build();
        FurnitureType saved = furnitureTypeRepository.save(entity);
        return new FurnitureTypeResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public void delete(Long id) {
        FurnitureType type = furnitureTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FurnitureType", id));

        if (productRepository.existsByTypeIgnoreCase(type.getName())) {
            throw new ConflictException("Nie można usunąć typu '" + type.getName() + "', ponieważ istnieją produkty przypisane do tej kategorii.");
        }

        furnitureTypeRepository.delete(type);
    }
}
