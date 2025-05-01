package org.kirya343.main.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final long maxFileSize = 5 * 1024 * 1024; // 5MB


    public StorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String storeImage(MultipartFile file) throws IOException {
        // Проверка пустого файла
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        // Проверка размера файла
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds 5MB limit");
        }

        // Проверка расширения файла
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (!allowedExtensions.contains(fileExtension)) {
            throw new RuntimeException("Only JPG, JPEG, PNG, GIF images are allowed");
        }

        // Генерация уникального имени файла + кодировка
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Путь до конечной папки (можно использовать rootLocation, если путь настраивается)
        Path destinationFile = this.rootLocation.resolve(filename).normalize();

        // Проверка безопасности пути
        if (!destinationFile.getParent().equals(this.rootLocation)) {
            throw new RuntimeException("Cannot store file outside the target directory");
        }

        // Сохранение файла
        Files.copy(file.getInputStream(), destinationFile);

        // Возвращаем путь, который можно вставить в <img src="/uploads/filename">
        return filename;
    }

    public String storeFile(MultipartFile file) throws IOException {
        // Проверка пустого файла
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }

        // Проверка размера файла (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 5MB limit");
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