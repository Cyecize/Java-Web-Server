package com.cyecize.toyote.models;

import java.io.InputStream;
import java.util.Map;

public class MultipartEntry {
    private final Map<String, String> contentDispositionData;

    private final String contentType;

    private final InputStream inputStream;

    public MultipartEntry(Map<String, String> contentDispositionData, String contentType, InputStream inputStream) {
        this.contentDispositionData = contentDispositionData;
        this.contentType = contentType;
        this.inputStream = inputStream;
    }

    public Map<String, String> getContentDispositionData() {
        return this.contentDispositionData;
    }

    public String getContentType() {
        return this.contentType;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }
}
