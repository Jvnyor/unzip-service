package com.jvnyor.unzip.service;

import java.io.IOException;

public interface ZipService {
    void unzipFile(String sourceZipFilePath, String outputDirectoryPath) throws IOException;

    void zipFile(String sourceFilePath, String outputZipFilePath) throws IOException;
}
