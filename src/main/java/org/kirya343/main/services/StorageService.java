package org.kirya343.main.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {
    private final Path rootLocation = Paths.get("uploads").toAbsolutePath().normalize();
    private final Set<String> allowedExtensions = Set.of(".jpg", ".jpeg", ".png", ".gif");
    private final long maxFileSize = 10 * 1024 * 1024; // 5MB


    public StorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String storeImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (!allowedExtensions.contains(fileExtension)) {
            throw new RuntimeException("Only JPG, JPEG, PNG, GIF images are allowed");
        }

        String filename = UUID.randomUUID().toString() + fileExtension;
        Path destinationFile = this.rootLocation.resolve(filename).normalize();

        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new RuntimeException("Cannot store file outside the target directory");
        }

        // Автосжатие с качеством 80%
        try (var inputStream = file.getInputStream()) {
            Thumbnails.of(inputStream)
                    .size(1920, 1920)               // Максимальный размер (если нужно уменьшить)
                    .outputQuality(0.8)             // Сжатие JPEG (0.0 - 1.0)
                    .toFile(destinationFile.toFile());
        }

        return filename;
    }

    public String storeFile(MultipartFile file) throws IOException {
        // Проверка пустого файла
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        // Проверка размера файла (5MB)
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        // Проверка расширения (только PDF)
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (!".pdf".equals(fileExtension)) {
            throw new RuntimeException("Only PDF files are allowed");
        }

        // Создаем директорию для резюме, если не существует
        Path resumeDir = this.rootLocation.resolve("resumes");
        Files.createDirectories(resumeDir);

        // Генерация уникального имени файла
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Сохранение файла
        Path destinationFile = resumeDir.resolve(filename).normalize();
        Files.copy(file.getInputStream(), destinationFile);

        // Возвращаем относительный путь (resumes/filename.pdf)
        return "resumes/" + filename;
    }

    public void deleteImage(String filename) throws IOException {
        Path filePath = rootLocation.resolve(filename).normalize();
        Files.deleteIfExists(filePath);
    }
}