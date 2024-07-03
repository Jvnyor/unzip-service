package com.jvnyor.unzip.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class UnzipServiceTest {

    private UnzipService unzipService;

    @BeforeEach
    void setUp() {
        unzipService = new UnzipService();
    }

    @Test
    void givenZipFile_whenUnzipped_thenFileExtractedSuccessfully(@TempDir Path tempDir) throws IOException {
        // Create a temporary zip file
        Path zipPath = tempDir.resolve("test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry zipEntry = new ZipEntry("testFile.txt");
            zos.putNextEntry(zipEntry);
            zos.write("Hello, World!".getBytes());
            zos.closeEntry();
        }

        // Output directory
        File outputDir = tempDir.resolve("output").toFile();

        // Test unzipFile
        unzipService.unzipFile(zipPath.toString(), outputDir.getAbsolutePath());

        // Verify the file was extracted
        File extractedFile = new File(outputDir, "testFile.txt");
        assertTrue(extractedFile.exists());
        assertEquals("Hello, World!", new String(Files.readAllBytes(extractedFile.toPath())));
    }

    @Test
    void givenZipFileWithDirectory_whenUnzipped_thenIOExceptionThrown(@TempDir Path tempDir) {
        // Assuming the implementation is updated to throw an exception when directories are encountered
        // Create a temporary zip file with a directory
        Path zipPath = tempDir.resolve("testWithDir.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry zipEntry = new ZipEntry("dir/");
            zos.putNextEntry(zipEntry);
            zos.closeEntry();
        } catch (IOException e) {
            fail("Failed to create test zip file with directory", e);
        }

        // Output directory
        File outputDir = tempDir.resolve("outputDir").toFile();

        // Test unzipFile
        IOException exception = assertThrows(IOException.class, () ->
                unzipService.unzipFile(zipPath.toString(), outputDir.getAbsolutePath())
        );
        assertEquals("Zip file contains directories, which are not allowed.", exception.getMessage());
    }

    @Test
    void givenZipEntryOutsideTargetDir_whenUnzipped_thenSecurityExceptionThrown(@TempDir Path tempDir) throws IOException {
        // Setup: Create a zip file with an entry that has a path traversal attempt
        Path zipPath = tempDir.resolve("malicious.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry zipEntry = new ZipEntry("../maliciousFile.txt");
            zos.putNextEntry(zipEntry);
            zos.write("Malicious content".getBytes());
            zos.closeEntry();
        }

        File outputDir = tempDir.resolve("output").toFile();

        // Test: Attempting to unzip should throw an IOException due to security check in newFile method
        IOException exception = assertThrows(IOException.class, () ->
                unzipService.unzipFile(zipPath.toString(), outputDir.getAbsolutePath())
        );

        assertTrue(exception.getMessage().contains("Entry is outside of the target dir"));
    }

    @Test
    void givenDirectoryCannotBeCreated_whenUnzipped_thenIOExceptionThrown(@TempDir Path tempDir) throws IOException {
        // Setup: Create a zip file with one file entry simulating a nested directory structure
        Path zipPath = tempDir.resolve("test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            ZipEntry zipEntry = new ZipEntry("nestedDir/testFile.txt");
            zos.putNextEntry(zipEntry);
            zos.write("Content".getBytes());
            zos.closeEntry();
        }

        // Instead of using a non-existent directory, use a file to ensure mkdirs() fails
        File outputDir = tempDir.resolve("output").toFile();
        assertTrue(outputDir.createNewFile()); // Ensure this is a file, not a directory

        // Test: Attempting to unzip should throw an IOException because it cannot create the nested directory
        IOException exception = assertThrows(IOException.class, () ->
                unzipService.unzipFile(zipPath.toString(), outputDir.getAbsolutePath())
        );

        assertTrue(exception.getMessage().contains("Failed to create directory"));
    }
}