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

    public enum ImageType {
        LISTING_IMAGE("listing-images", "image-for-listing_", 1920, 1920, 0.8),
        NEWS_IMAGE("news-images", "image-for-news_", 1920, 1920, 0.8),
        AVATAR("avatars", "avatar_", 300, 300, 0.8);

        private final String directory;
        private final String prefix;
        private final int width;
        private final int height;
        private final double quality;

        // Конструктор и геттеры
        ImageType(String directory, String prefix, int width, int height, double quality) {
            this.directory = directory;
            this.prefix = prefix;
            this.width = width;
            this.height = height;
            this.quality = quality;
        }

        public String getDirectory() { return directory; }
        public String getPrefix() { return prefix; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public double getQuality() { return quality; }
    }

    public String storeImage(MultipartFile file, ImageType imageType, Long entityId) throws IOException {
        // Базовые проверки
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file");
        }
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        // Проверка расширения файла
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (!allowedExtensions.contains(fileExtension)) {
            throw new RuntimeException("Only JPG, JPEG, PNG, GIF images are allowed");
        }

        // Создаем подпапку, если ее нет
        Path targetPath = this.rootLocation.resolve(imageType.getDirectory());
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
        }

        // Формируем имя файла
        String filename = imageType.getPrefix() + 
                        (entityId != null ? entityId + "_" : "") + 
                        UUID.randomUUID().toString() + fileExtension;
        
        Path destinationFile = targetPath.resolve(filename).normalize();

        if (!destinationFile.getParent().equals(targetPath)) {
            throw new RuntimeException("Cannot store file outside the target directory");
        }

        // Обработка изображения
        try (var inputStream = file.getInputStream()) {
            Thumbnails.of(inputStream)
                    .size(imageType.getWidth(), imageType.getHeight())
                    .outputQuality(imageType.getQuality())
                    .toFile(destinationFile.toFile());
        }

        return imageType.getDirectory() + "/" + filename;
    }

    public String storeListingImage(MultipartFile file, Long listingId) throws IOException {
        return storeImage(file, ImageType.LISTING_IMAGE, listingId);
    }

    public String storeAvatar(MultipartFile file, Long userId) throws IOException {
        return storeImage(file, ImageType.AVATAR, userId);
    }

    public String storeNewsImage(MultipartFile file, Long newsId) throws IOException {
        return storeImage(file, ImageType.NEWS_IMAGE, newsId);
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