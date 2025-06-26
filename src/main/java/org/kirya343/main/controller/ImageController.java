/* package org.kirya343.main.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/uploads")
public class ImageController {

    private static final String IMAGE_DIR = "src/main/resources/static/images/";

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName,
                                           @RequestHeader(value = "Accept", defaultValue = "") String acceptHeader) throws IOException {
        String originalPath = IMAGE_DIR + fileName;
        String webpPath = originalPath.replaceAll("\\.(jpg|jpeg|png)$", ".webp");

        File imageFile;

        // Если браузер поддерживает WebP и такой файл существует — отдаём его
        if (acceptHeader.contains("image/webp") && new File(webpPath).exists()) {
            imageFile = new File(webpPath);
        } else {
            imageFile = new File(originalPath);
        }

        if (!imageFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        byte[] bytes = Files.readAllBytes(imageFile.toPath());
        String contentType = Files.probeContentType(imageFile.toPath());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .body(bytes);
    }
}
 */