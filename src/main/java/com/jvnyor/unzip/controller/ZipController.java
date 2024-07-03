package com.jvnyor.unzip.controller;

import com.jvnyor.unzip.service.ZipServiceGzipImpl;
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

@RestController
@RequestMapping("/api/v1/zip")
public class ZipController {
    // for tests, in the future will be dynamically injected
    private final ZipServiceGzipImpl zipService;

    public ZipController(ZipServiceGzipImpl zipService) {
        this.zipService = zipService;
    }

//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadAndDownloadUnzippedFile(@RequestParam("file") MultipartFile file) {
//        if (file.isEmpty()) {
//            return ResponseEntity.badRequest().body("File must not be empty");
//        }
//
//        try {
//            File tempDir = Files.createTempDirectory("upload").toFile();
//            File uploadedFile = new File(tempDir, Objects.requireNonNull(file.getOriginalFilename()));
//            file.transferTo(uploadedFile);
//
//            File unzippedDir = Files.createTempDirectory("unzipped").toFile();
//            zipService.unzipFile(uploadedFile.getAbsolutePath(), unzippedDir.getAbsolutePath());
//
//            File[] unzippedFiles = unzippedDir.listFiles();
//            if (unzippedFiles == null || unzippedFiles.length == 0) {
//                throw new FileNotFoundException("No files found in the uploaded zip");
//            }
//            File firstUnzippedFile = unzippedFiles[0];
//
//            Path path = Paths.get(firstUnzippedFile.getAbsolutePath());
//            String mimeType = Files.probeContentType(path);
//            if (mimeType == null) {
//                mimeType = "application/octet-stream"; // Default MIME type if detection fails
//            }
//
//            Resource resource = new UrlResource(path.toUri());
//            return ResponseEntity.ok()
//                    .contentType(MediaType.parseMediaType(mimeType))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + firstUnzippedFile.getName() + "\"")
//                    .body(resource);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("An error occurred: " + e.getMessage());
//        }
//    }

    @PostMapping("/zip-and-download")
    public ResponseEntity<?> zipAndDownloadFile(@RequestParam("file") MultipartFile file) throws Exception {
        File tempFile = Files.createTempFile(null, null).toFile();
        file.transferTo(tempFile);

        File zippedFile = Files.createTempFile(null, ".gz").toFile();
        zipService.zipFile(tempFile.getAbsolutePath(), zippedFile.getAbsolutePath());

        byte[] bytes = Files.readAllBytes(zippedFile.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/gzip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zippedFile.getName() + "\"")
                .body(bytes);
    }

    @PostMapping("/unzip-and-download")
    public ResponseEntity<?> unzipAndDownloadFile(@RequestParam("file") MultipartFile file) throws Exception {
        File tempFile = Files.createTempFile(null, ".gz").toFile();
        file.transferTo(tempFile);

        File unzippedFile = Files.createTempFile(null, null).toFile();
        zipService.unzipFile(tempFile.getAbsolutePath(), unzippedFile.getAbsolutePath());

        byte[] bytes = Files.readAllBytes(unzippedFile.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + unzippedFile.getName() + "\"")
                .body(bytes);
    }

    @PostMapping("/zip-and-download-2")
    public ResponseEntity<?> zipAndDownloadFile2(@RequestParam("file") MultipartFile file) throws Exception {
        // Extract original file name and extension
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isEmpty()) {
            return ResponseEntity.badRequest().body("File name must not be empty");
        }

        // Create a temporary file with the original file name
        File tempFile = File.createTempFile("temp", originalFileName);
        file.transferTo(tempFile);

        // Create a zipped file with the original file name but with .gz extension
        File zippedFile = new File(tempFile.getParent(), originalFileName + ".gz");
        zipService.zipFile(tempFile.getAbsolutePath(), zippedFile.getAbsolutePath());

        // Serve the zipped file for download
        byte[] bytes = Files.readAllBytes(zippedFile.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/gzip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zippedFile.getName() + "\"")
                .body(bytes);
    }

    @PostMapping("/unzip-read-and-download")
    public ResponseEntity<?> unzipReadAndDownloadFile(@RequestParam("file") MultipartFile file) throws Exception {
        // Unzip the file
        File tempFile = Files.createTempFile(null, ".gz").toFile();
        file.transferTo(tempFile);
        File unzippedFile = Files.createTempFile(null, null).toFile();
        zipService.unzipFile(tempFile.getAbsolutePath(), unzippedFile.getAbsolutePath());

        // Read the unzipped file
        String content = new String(Files.readAllBytes(unzippedFile.toPath()));

        // Return the content of the unzipped file
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + unzippedFile.getName() + "\"")
                .body(content);
    }
}