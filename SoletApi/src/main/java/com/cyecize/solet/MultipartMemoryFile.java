package com.cyecize.solet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MultipartMemoryFile implements MemoryFile {

    private final String fileName;

    private final String fieldName;

    private final String filePath;

    private final String contentType;

    public MultipartMemoryFile(String fileName, String fieldName, String filePath, String contentType) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.filePath = filePath;
        this.contentType = contentType;
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
        try {
            return new FileInputStream(this.filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getBytes() {
        try {
            InputStream inputStream = this.getInputStream();
            if (inputStream != null) {
                return inputStream.readAllBytes();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
