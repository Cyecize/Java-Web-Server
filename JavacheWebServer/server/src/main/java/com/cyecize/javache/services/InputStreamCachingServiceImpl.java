package com.cyecize.javache.services;

import com.cyecize.javache.io.Reader;

import java.io.*;

public final class InputStreamCachingServiceImpl implements InputStreamCachingService {

    private byte[] bytes;

    public InputStreamCachingServiceImpl() {

    }

    @Override
    public byte[] getOrCacheInputStream(InputStream inputStream) throws IOException {
        return this.getOrCacheInputStream(inputStream, Integer.MAX_VALUE);
    }

    @Override
    public byte[] getOrCacheInputStream(InputStream inputStream, int maxRequestLength) throws IOException {
        if (this.bytes == null) {
            this.bytes = new Reader().readAllBytes(inputStream, maxRequestLength);
        }
        return this.bytes;
    }

    @Override
    public void evictCache() {
        this.bytes = null;
    }
}
