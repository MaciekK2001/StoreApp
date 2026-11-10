package produkcja.meble.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import produkcja.meble.app.dto.CreateFurnitureTypeRequest;
import produkcja.meble.app.dto.FurnitureTypeResponse;
import produkcja.meble.app.service.FurnitureTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/furniture-types")
@RequiredArgsConstructor
public class FurnitureTypeController {

    private final FurnitureTypeService furnitureTypeService;

    @GetMapping
    public ResponseEntity<List<FurnitureTypeResponse>> getAllTypes() {
        return ResponseEntity.ok(furnitureTypeService.findAll());
    }

    @PostMapping
    public ResponseEntity<FurnitureTypeResponse> createType(@Valid @RequestBody CreateFurnitureTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(furnitureTypeService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable Long id) {
        furnitureTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
