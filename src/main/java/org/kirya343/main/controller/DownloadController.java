package org.kirya343.main.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.core.io.Resource;
import java.nio.file.Path;

@Controller
public class DownloadController {
    @GetMapping("/resumes/download/resumes/{filename:.+}")
    public ResponseEntity<Resource> downloadResume(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get("uploads/resumes/").resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new FileNotFoundException("Файл не найден: " + filename);
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }
}
