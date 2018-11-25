package com.cyecize.summer.areas.routing.interfaces;

import com.cyecize.solet.MemoryFile;

import java.io.IOException;

public interface MultipartFile {

    String save(String relativePath) throws IOException;

    String save(String relativePath, String fileName) throws IOException;

    MemoryFile getUploadedFile();
}
