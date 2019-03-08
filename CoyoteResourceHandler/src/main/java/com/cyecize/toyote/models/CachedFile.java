package com.cyecize.toyote.models;

public class CachedFile {

    private long lastTimeAccessed;

    private byte[] fileContent;

    private String contentType;

    public CachedFile() {

    }

    public CachedFile(long lastTimeAccessed, byte[] fileContent, String contentType) {
        this.lastTimeAccessed = lastTimeAccessed;
        this.fileContent = fileContent;
        this.contentType = contentType;
    }

    public long getLastTimeAccessed() {
        return lastTimeAccessed;
    }

    public void setLastTimeAccessed(long lastTimeAccessed) {
        this.lastTimeAccessed = lastTimeAccessed;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
