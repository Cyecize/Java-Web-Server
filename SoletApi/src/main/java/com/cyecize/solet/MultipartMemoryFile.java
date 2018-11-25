package com.cyecize.solet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MultipartMemoryFile implements MemoryFile {

    private final String fileName;

    private final String fieldName;

    private final byte[] bytes;

    private final String contentType;

    public MultipartMemoryFile(String fileName, String fieldName, byte[] bytes, String contentType) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.bytes = bytes;
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
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public byte[] getBytes() {
        return this.bytes;
    }
}
