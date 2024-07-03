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
    void testUnzipFileSuccess(@TempDir Path tempDir) throws IOException {
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
    void testUnzipFileWithDirectoryException(@TempDir Path tempDir) {
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
}