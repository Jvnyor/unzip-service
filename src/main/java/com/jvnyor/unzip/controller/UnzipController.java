package com.jvnyor.unzip.controller;

import com.jvnyor.unzip.service.UnzipService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/unzip")
public class UnzipController {

    private final UnzipService unzipService;

    public UnzipController(UnzipService unzipService) {
        this.unzipService = unzipService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndDownloadUnzippedFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File must not be empty");
        }

        try {
            File tempDir = Files.createTempDirectory("upload").toFile();
            File uploadedFile = new File(tempDir, Objects.requireNonNull(file.getOriginalFilename()));
            file.transferTo(uploadedFile);

            File unzippedDir = Files.createTempDirectory("unzipped").toFile();
            unzipService.unzipFile(uploadedFile.getAbsolutePath(), unzippedDir.getAbsolutePath());

            File[] unzippedFiles = unzippedDir.listFiles();
            if (unzippedFiles == null || unzippedFiles.length == 0) {
                throw new FileNotFoundException("No files found in the uploaded zip");
            }
            File firstUnzippedFile = unzippedFiles[0];

            Path path = Paths.get(firstUnzippedFile.getAbsolutePath());
            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // Default MIME type if detection fails
            }

            Resource resource = new UrlResource(path.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + firstUnzippedFile.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
        }
    }
}