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

    @PostMapping("/unzip-and-download")
    public ResponseEntity<?> unzipAndDownloadFile(@RequestParam("file") MultipartFile file) throws Exception {
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
        byte[] bytes = Files.readAllBytes(unzippedFile.toPath());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + unzippedFileName + "\"")
                .body(bytes);
    }

    @PostMapping("/zip-and-download")
    public ResponseEntity<?> zipAndDownloadFile(@RequestParam("file") MultipartFile file) throws Exception {
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
}