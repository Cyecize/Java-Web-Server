package com.cyecize.javache.services;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamCachingService {
    void evictCache();

    byte[] getOrCacheInputStream(InputStream inputStream) throws IOException;

    byte[] getOrCacheInputStream(InputStream inputStream, int maxRequestLength) throws IOException;
}
