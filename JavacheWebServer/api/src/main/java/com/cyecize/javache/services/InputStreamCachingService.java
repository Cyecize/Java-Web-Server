package com.cyecize.javache.services;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamCachingService {
    void evictCache();

    InputStream getOrCacheInputStream(InputStream inputStream) throws IOException;
}
