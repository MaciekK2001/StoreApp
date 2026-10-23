package produkcja.meble.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import produkcja.meble.app.exception.BadRequestException;
import produkcja.meble.app.exception.ResourceNotFoundException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.upload.dir:uploads/products}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Nie można utworzyć katalogu do przechowywania plików: " + uploadDir, ex);
        }
    }

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
            fileExtension = originalFileName.substring(dotIndex);
        } else if (contentType.contains("png")) {
            fileExtension = ".png";
        } else {
            fileExtension = ".jpg";
        }

        String newFileName = UUID.randomUUID() + fileExtension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/api/products/images/" + newFileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Nie udało się zapisać pliku: " + newFileName, ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new BadRequestException("Próba nieautoryzowanego dostępu do ścieżki.");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Zdjęcie nie zostało znalezione: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Nieprawidłowy adres pliku: " + fileName);
        }
    }

    public void deleteFile(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return;
        }
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.fileStorageLocation)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ignored) {
        }
    }
}
