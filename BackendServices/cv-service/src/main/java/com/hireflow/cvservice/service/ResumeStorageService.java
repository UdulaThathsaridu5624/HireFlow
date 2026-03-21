package com.hireflow.cvservice.service;

import com.hireflow.cvservice.dto.ResumeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
public class ResumeStorageService {

    private final Path storagePath;

    public ResumeStorageService(@Value("${app.resume.storage-path:uploads/resumes}") String storagePath) {
        this.storagePath = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storagePath);
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize resume storage", e);
        }
    }

    public ResumeResponse uploadPdf(MultipartFile file, Boolean isDefault) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        if (!isPdf(file.getContentType(), originalName)) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        String storedFileName = UUID.randomUUID() + "-" + originalName;
        Path destination = storagePath.resolve(storedFileName).normalize();

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store resume file", e);
        }

        return new ResumeResponse(
                "/api/cv/resumes/files/" + storedFileName,
                originalName,
                Boolean.TRUE.equals(isDefault),
                LocalDateTime.now()
        );
    }

    public Resource loadAsResource(String storedFileName) {
        String sanitized = sanitizeFileName(storedFileName);
        Path filePath = storagePath.resolve(sanitized).normalize();

        if (!filePath.startsWith(storagePath)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Resume file not found");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid file path", e);
        }
    }

    private boolean isPdf(String contentType, String filename) {
        boolean contentTypeOk = contentType != null && contentType.toLowerCase(Locale.ROOT).contains("pdf");
        boolean extensionOk = filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
        return contentTypeOk || extensionOk;
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) {
            return "resume.pdf";
        }
        return Paths.get(name).getFileName().toString().replace("..", "");
    }
}
