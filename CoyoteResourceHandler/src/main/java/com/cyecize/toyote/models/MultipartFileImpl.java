package com.cyecize.toyote.models;

import com.cyecize.http.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class MultipartFileImpl implements MultipartFile {

    private final int fileLength;

    private final String contentType;

    private final String fileName;

    private final String fieldName;

    private final InputStream inputStream;

    public MultipartFileImpl(int fileLength, String contentType, String fileName, String fieldName, InputStream inputStream) {
        this.fileLength = fileLength;
        this.contentType = contentType;
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.inputStream = inputStream;
    }

    @Override
    public long getFileLength() {
        return this.fileLength;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public InputStream getInputStream() {
        return this.inputStream;
    }

    @Override
    public byte[] getBytes() {
        try {
            return this.inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
