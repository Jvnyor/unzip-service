package com.jvnyor.unzip.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class ZipServiceZipEntryImpl implements ZipService {

    @Override
    public void unzipFile(String sourceZipFilePath, String outputDirectoryPath) throws IOException {
        // First, validate the zip file to ensure it contains no directories.
        validateZipFile(sourceZipFilePath);

        // If validation passes, proceed to unzip.
        File destDir = new File(outputDirectoryPath);
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceZipFilePath))) {
            unzip(zis, destDir, buffer);
        }
    }

    @Override
    public void zipFile(String sourceFilePath, String outputZipFilePath) throws IOException {

    }

    private void validateZipFile(String sourceZipFilePath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceZipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                if (zipEntry.isDirectory()) {
                    throw new IOException("Zip file contains directories, which are not allowed.");
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }

    private void unzip(ZipInputStream zis, File destDir, byte[] buffer) throws IOException {
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (!zipEntry.isDirectory()) {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            zipEntry = zis.getNextEntry();
        }
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    public void zipDirectory(File directoryToZip, File zippedFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zippedFile))) {
            Path sourcePath = directoryToZip.toPath();
            Files.walk(sourcePath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                try {
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    System.err.println(e);
                }
            });
        }
    }
}