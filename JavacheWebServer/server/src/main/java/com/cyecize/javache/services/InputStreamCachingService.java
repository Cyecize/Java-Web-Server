package com.cyecize.javache.services;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamCachingService {
    void evictCache();

    String getOrCacheRequestContent(InputStream inputStream) throws IOException;
}
