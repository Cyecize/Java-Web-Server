package com.cyecize.solet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MultipartMemoryFile implements MemoryFile {

    private final String fileName;

    private final String fieldName;

    private final byte[] bytes;

    public MultipartMemoryFile(String fileName, String fieldName, byte[] bytes) {
        this.fileName = fileName;
        this.fieldName = fieldName;
        this.bytes = bytes;
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
