package com.cyecize.summer.areas.routing.interfaces;

import com.cyecize.http.MultipartFile;

import java.io.IOException;

public interface UploadedFile {

    String save(String relativePath) throws IOException;

    String save(String relativePath, String fileName) throws IOException;

    MultipartFile getUploadedFile();
}
