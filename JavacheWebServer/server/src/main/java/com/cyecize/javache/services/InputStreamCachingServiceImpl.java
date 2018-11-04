package com.cyecize.javache.services;

import com.cyecize.javache.io.Reader;

import java.io.IOException;
import java.io.InputStream;

public final class InputStreamCachingServiceImpl implements InputStreamCachingService {

    private String content;

    public InputStreamCachingServiceImpl() {

    }

    @Override
    public String getOrCacheRequestContent(InputStream inputStream) throws IOException {
        if (content == null) {
            this.content = new Reader().readAllLines(inputStream);
        }
        return this.content;
    }

    @Override
    public void evictCache() {
        this.content = null;
    }
}
