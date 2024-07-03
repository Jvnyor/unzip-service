package com.jvnyor.unzip.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipServiceGzipImplTest {

    private ZipServiceGzipImpl zipService;

    @BeforeEach
    void setUp() {
        zipService = new ZipServiceGzipImpl();
    }

    @Test
    void givenGzipFile_whenUnzipping_thenOriginalContentIsPreserved(@TempDir Path tempDir) throws Exception {
        // Given: A temporary GZIP file with known content
        File gzipFile = tempDir.resolve("test.gz").toFile();
        try (FileOutputStream fos = new FileOutputStream(gzipFile);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            gzos.write("Hello, World!".getBytes());
        }

        // Output file path for the unzipped content
        File outputFile = tempDir.resolve("output.txt").toFile();

        // When: Unzipping the GZIP file
        zipService.unzipFile(gzipFile.getAbsolutePath(), outputFile.getAbsolutePath());

        // Then: The output file exists and contains the expected content
        assertTrue(outputFile.exists());
        assertEquals("Hello, World!", Files.readString(outputFile.toPath()));
    }

    @Test
    void givenFileWithKnownContent_whenZipping_thenGzipFileContainsSameContent(@TempDir Path tempDir) throws Exception {
        // Given: A temporary file with known content
        File sourceFile = tempDir.resolve("source.txt").toFile();
        Files.writeString(sourceFile.toPath(), "Hello, World!");

        // Output GZIP file path
        File gzipFile = tempDir.resolve("output.gz").toFile();

        // When: Zipping the file
        zipService.zipFile(sourceFile.getAbsolutePath(), gzipFile.getAbsolutePath());

        // Then: Verify the GZIP file by unzipping and checking content
        File unzippedFile = tempDir.resolve("unzipped.txt").toFile();
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(gzipFile));
             FileOutputStream out = new FileOutputStream(unzippedFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }

        // Verify the content is as expected
        assertTrue(unzippedFile.exists());
        assertEquals("Hello, World!", Files.readString(unzippedFile.toPath()));
    }
}