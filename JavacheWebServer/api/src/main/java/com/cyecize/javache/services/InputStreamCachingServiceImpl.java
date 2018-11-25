package com.cyecize.javache.services;

import com.cyecize.javache.io.Reader;

import java.io.*;

public final class InputStreamCachingServiceImpl implements InputStreamCachingService {

    private byte[] bytes;

    public InputStreamCachingServiceImpl() {

    }

    @Override
    public InputStream getOrCacheInputStream(InputStream inputStream) throws IOException {
        if (this.bytes == null) {
            this.bytes = new Reader().readAllBytes(inputStream);
        }
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public void evictCache() {
        this.bytes = null;
    }
}
