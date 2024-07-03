package com.jvnyor.unzip.service;

import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class ZipServiceGzipImpl implements ZipService {

    @Override
    public void unzipFile(String sourceGzipFilePath, String outputFilePath) throws IOException {
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(sourceGzipFilePath));
             FileOutputStream out = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
    }

    @Override
    public void zipFile(String sourceFilePath, String outputGzipFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFilePath);
             FileOutputStream fos = new FileOutputStream(outputGzipFilePath);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }
        }
    }
}
