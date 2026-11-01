package produkcja.meble.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import produkcja.meble.app.entity.StoredImage;
import produkcja.meble.app.exception.BadRequestException;
import produkcja.meble.app.exception.ResourceNotFoundException;
import produkcja.meble.app.repository.StoredImageRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    public record StoredImageResource(Resource resource, String contentType, String fileName) {}

    private final StoredImageRepository storedImageRepository;
    private final Path fileStorageLocation;

    public FileStorageService(
            StoredImageRepository storedImageRepository,
            @Value("${app.upload.dir:uploads/products}") String uploadDir
    ) {
        this.storedImageRepository = storedImageRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            log.warn("Nie można utworzyć lokalnego katalogu na pliki (używamy bazy danych jako głównego magazynu): {}", uploadDir);
        }
    }

    @Transactional
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Przesłany plik jest pusty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Dozwolone są wyłącznie pliki graficzne (JPEG, PNG, WEBP).");
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "photo.jpg"));
        if (originalFileName.contains("..")) {
            throw new BadRequestException("Nieprawidłowa nazwa pliku: " + originalFileName);
        }

        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex >= 0) {
            fileExtension = originalFileName.substring(dotIndex).toLowerCase();
        } else if (contentType.contains("png")) {
            fileExtension = ".png";
        } else if (contentType.contains("webp")) {
            fileExtension = ".webp";
        } else {
            fileExtension = ".jpg";
        }

        String newFileName = UUID.randomUUID() + fileExtension;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Nie udało się odczytać przesłanego pliku: " + originalFileName, ex);
        }

        StoredImage storedImage = StoredImage.builder()
                .fileName(newFileName)
                .contentType(contentType)
                .fileSize(file.getSize())
                .data(bytes)
                .createdAt(Instant.now())
                .build();

        storedImageRepository.save(storedImage);
        log.info("Zapisano zdjęcie w bazie danych: {} (rozmiar: {} bajtów, typ: {})", newFileName, bytes.length, contentType);

        // Opcjonalna kopia lokalna na dysku
        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.write(targetLocation, bytes);
        } catch (Exception ex) {
            log.debug("Nie zapisano kopii lokalnej na dysku: {}", ex.getMessage());
        }

        return "/api/products/images/" + newFileName;
    }

    @Transactional(readOnly = true)
    public StoredImageResource loadStoredImage(String fileName) {
        // 1. Sprawdzamy bazę danych (główny, trwały magazyn)
        Optional<StoredImage> storedImage = storedImageRepository.findByFileName(fileName);
        if (storedImage.isPresent()) {
            StoredImage img = storedImage.get();
            Resource resource = new ByteArrayResource(img.getData(), img.getFileName()) {
                @Override
                public String getFilename() {
                    return img.getFileName();
                }
            };
            return new StoredImageResource(resource, img.getContentType(), img.getFileName());
        }

        // 2. Fallback: sprawdzamy lokalny system plików (np. dla starszych plików)
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.fileStorageLocation) && Files.exists(filePath) && Files.isReadable(filePath)) {
                Resource resource = new UrlResource(filePath.toUri());
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = resolveContentTypeFromFileName(fileName);
                }
                return new StoredImageResource(resource, contentType, fileName);
            }
        } catch (MalformedURLException ignored) {
            throw new ResourceNotFoundException("Nieprawidłowy adres pliku: " + fileName);
        } catch (IOException ignored) {
        }

        throw new ResourceNotFoundException("Zdjęcie nie zostało znalezione: " + fileName);
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(String fileName) {
        return loadStoredImage(fileName).resource();
    }

    @Transactional
    public void deleteFile(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return;
        }
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            storedImageRepository.deleteByFileName(fileName);
            log.info("Usunięto zdjęcie z bazy danych: {}", fileName);

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.fileStorageLocation)) {
                Files.deleteIfExists(filePath);
            }
        } catch (Exception ex) {
            log.warn("Błąd podczas usuwania zdjęcia {}: {}", imageUrl, ex.getMessage());
        }
    }

    private String resolveContentTypeFromFileName(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        } else {
            return "image/jpeg";
        }
    }
}
