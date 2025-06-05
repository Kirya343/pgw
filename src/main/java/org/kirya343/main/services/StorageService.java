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
    private final long maxFileSize = 10 * 1024 * 1024; // 5MB

    public StorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public enum FileType {
        LISTING_IMAGE("listing-images", "image-for-listing_", 1920, 1920, 0.8, Set.of(".jpg", ".jpeg", ".png", ".gif")),
        NEWS_IMAGE("news-images", "image-for-news_", 1920, 1920, 0.8, Set.of(".jpg", ".jpeg", ".png", ".gif")),
        AVATAR("avatars", "avatar_", 300, 300, 0.8, Set.of(".jpg", ".jpeg", ".png", ".gif")),
        RESUME("resumes", "resume_", null, null, null, Set.of(".pdf"));

        private final String directory;
        private final String prefix;
        private final Integer width;  // null для не-изображений
        private final Integer height; // null для не-изображений
        private final Double quality; // null для не-изображений
        private final Set<String> allowedExtensions;

        // Конструктор и геттеры
        FileType(String directory, String prefix, Integer width, Integer height, Double quality, Set<String> allowedExtensions) {
            this.directory = directory;
            this.prefix = prefix;
            this.width = width;
            this.height = height;
            this.quality = quality;
            this.allowedExtensions = allowedExtensions;
        }

        public String getDirectory() { return directory; }
        public String getPrefix() { return prefix; }
        public Integer getWidth() { return width; }
        public Integer getHeight() { return height; }
        public double getQuality() { return quality; }
        public Set<String> getAllowedExtensions() { return allowedExtensions; }
    }

    public String storeFile(MultipartFile file, FileType fileType, Long entityId) throws IOException {
        // Базовые проверки
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds size limit");
        }

        // Проверка расширения файла
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (!fileType.getAllowedExtensions().contains(fileExtension)) {
            throw new RuntimeException("Only " + fileType.getAllowedExtensions() + " files are allowed");
        }

        // Создаем подпапку
        Path targetPath = this.rootLocation.resolve(fileType.getDirectory());
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        // Формируем имя файла
        String filename = fileType.getPrefix() + 
                        (entityId != null ? entityId + "_" : "") + 
                        UUID.randomUUID().toString() + fileExtension;
        
        Path destinationFile = targetPath.resolve(filename).normalize();

        if (!destinationFile.getParent().equals(targetPath)) {
            throw new RuntimeException("Cannot store file outside the target directory");
        }

        // Обработка файла
        if (fileType.getWidth() != null) { // Это изображение
            try (var inputStream = file.getInputStream()) {
                Thumbnails.of(inputStream)
                        .size(fileType.getWidth(), fileType.getHeight())
                        .outputQuality(fileType.getQuality())
                        .toFile(destinationFile.toFile());
            }
        } else { // Не изображение (например, PDF)
            Files.copy(file.getInputStream(), destinationFile);
        }

        return "/" + fileType.getDirectory() + "/" + filename;
    }

    public String storeListingImage(MultipartFile file, Long listingId) throws IOException {
        return storeFile(file, FileType.LISTING_IMAGE, listingId);
    }

    public String storeAvatar(MultipartFile file, Long userId) throws IOException {
        return storeFile(file, FileType.AVATAR, userId);
    }

    public String storeNewsImage(MultipartFile file, Long newsId) throws IOException {
        return storeFile(file, FileType.NEWS_IMAGE, newsId);
    }

    public String storeResume(MultipartFile file, Long userId) throws IOException {
        return storeFile(file, FileType.RESUME, userId);
    }

    public void deleteFile(String filePath) throws IOException {
        Path fileToDelete = rootLocation.resolve(filePath).normalize();
        Files.deleteIfExists(fileToDelete);
    }

    public void deleteImage(String filename) throws IOException {
        Path filePath = rootLocation.resolve(filename).normalize();
        Files.deleteIfExists(filePath);
    }
}