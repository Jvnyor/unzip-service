package com.jvnyor.unzip.controller;

import com.jvnyor.unzip.service.ZipServiceGzipImpl;
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
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/zip")
public class ZipController {

    private final ZipServiceGzipImpl zipService;

    public ZipController(ZipServiceGzipImpl zipService) {
        this.zipService = zipService;
    }

    @PostMapping("/unzip-and-download")
    public ResponseEntity<Resource> unzipAndDownloadFile(@RequestParam("file") MultipartFile file) throws Exception {
        // Step 1 & 2: Transfer the uploaded file to a temporary file
        File tempFile = Files.createTempFile(null, ".gz").toFile();
        file.transferTo(tempFile);

        // Step 3 & 4: Unzip the file to another temporary file
        File unzippedFile = Files.createTempFile(null, null).toFile();
        zipService.unzipFile(tempFile.getAbsolutePath(), unzippedFile.getAbsolutePath());

        // Step 5: Determine a meaningful filename for the unzipped content
        String originalFileName = file.getOriginalFilename();
        String unzippedFileName = originalFileName != null ? originalFileName.replaceAll("\\.gz$", "") : "unzippedFile";

        // Step 6: Serve the unzipped file for download
        Path path = unzippedFile.toPath();
        Resource resource = new UrlResource(path.toUri());

        tempFile.delete();
        unzippedFile.deleteOnExit();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + unzippedFileName + "\"")
                .body(resource);
    }

    @PostMapping("/zip-and-download")
    public ResponseEntity<Resource> zipAndDownloadFile(@RequestParam("file") MultipartFile file) throws Exception {
        // Extract original file name and extension
        String originalFileName = file.getOriginalFilename();

        // Create a temporary file with the original file name
        File tempFile = File.createTempFile("temp", originalFileName);
        file.transferTo(tempFile);

        // Create a zipped file with the original file name but with .gz extension
        File zippedFile = new File(tempFile.getParent(), originalFileName + ".gz");
        zipService.zipFile(tempFile.getAbsolutePath(), zippedFile.getAbsolutePath());

        // Serve the zipped file for download
        Path path = zippedFile.toPath();
        Resource resource = new UrlResource(path.toUri());

        tempFile.delete();
        zippedFile.deleteOnExit();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/gzip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zippedFile.getName() + "\"")
                .body(resource);
    }
}