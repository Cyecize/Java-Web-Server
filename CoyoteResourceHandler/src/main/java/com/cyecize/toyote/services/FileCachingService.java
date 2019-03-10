package com.cyecize.toyote.services;

import com.cyecize.toyote.models.CachedFile;

import java.io.FileNotFoundException;

public interface FileCachingService {

    void clearCache();

    boolean canCache(String fileRoute, long fileLength);

    boolean cacheFile(String fileRoute, byte[] fileContent, String contentType);

    boolean hasCachedFile(String fileRoute);

    boolean removeFile(String fileRoute);

    CachedFile getCachedFile(String fileRoute) throws FileNotFoundException;

}
