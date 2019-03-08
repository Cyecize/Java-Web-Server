package com.cyecize.toyote.services;

import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.models.CachedFile;
import com.cyecize.toyote.models.FrequencyCounter;

import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileCachingServiceImpl implements FileCachingService {

    private final JavacheConfigService configService;

    private Map<String, CachedFile> cache;

    private Map<String, FrequencyCounter> fileFrequencyAccess;

    private long maximumAllowedFilesInCache;

    private long maxCachedFileSize;

    public FileCachingServiceImpl(JavacheConfigService configService) {
        this.configService = configService;
        this.cache = new HashMap<>();
        this.fileFrequencyAccess = new HashMap<>();

        this.maximumAllowedFilesInCache = configService.getConfigParam(ConfigConstants.TOYOTE_MAX_NUMBER_OF_CACHED_FILES, Integer.class);
        this.maxCachedFileSize = configService.getConfigParam(ConfigConstants.TOYOYE_CACHED_FILE_MAX_SIZE, Integer.class);
    }

    @Override
    public void clearCache() {
        this.cache = new HashMap<>();
    }

    @Override
    public boolean cacheFile(String fileRoute, byte[] fileContent, String contentType) {
        if (fileContent.length > this.maxCachedFileSize) {
            return false;
        }

        if (this.cache.size() <= this.maximumAllowedFilesInCache) {
            CachedFile cachedFile = new CachedFile(new Date().getTime(), fileContent, contentType);
            this.cache.put(fileRoute, cachedFile);

            return true;
        }

        long fileAccessesCount = this.getFileAccessesCount(fileRoute);

        String candidateForReplacement = this.findFileCandidateForReplacement(fileAccessesCount, fileContent.length);
        if (candidateForReplacement != null) {
            this.removeFile(candidateForReplacement);
            this.cache.put(fileRoute, new CachedFile(new Date().getTime(), fileContent, contentType));
            return true;
        }

        return false;
    }

    @Override
    public boolean hasCachedFile(String fileRoute) {
        this.registerFileAccess(fileRoute);
        return this.cache.containsKey(fileRoute);
    }

    @Override
    public boolean removeFile(String fileRoute) {
        return this.cache.remove(fileRoute) != null;
    }

    @Override
    public CachedFile getCachedFile(String fileRoute) throws FileNotFoundException {
        if (!this.cache.containsKey(fileRoute)) {
            throw new FileNotFoundException(String.format("File %s not present in cache.", fileRoute));
        }

        CachedFile cachedFile = this.cache.get(fileRoute);
        this.updateCachedFile(cachedFile);

        return cachedFile;
    }

    private void registerFileAccess(String fileRoute) {
        if (!this.fileFrequencyAccess.containsKey(fileRoute)) {
            this.fileFrequencyAccess.put(fileRoute, new FrequencyCounter());
        }

        this.fileFrequencyAccess.get(fileRoute).count();
    }

    private long getFileAccessesCount(String fileRoute) {
        return this.fileFrequencyAccess.getOrDefault(fileRoute, new FrequencyCounter()).getCount();
    }

    private void updateCachedFile(CachedFile cachedFile) {
        cachedFile.setLastTimeAccessed(new Date().getTime());
    }

    private String findFileCandidateForReplacement(long fileAccessesCount, long fileSize) {
        Map.Entry<String, CachedFile> stringCachedFileEntry = this.cache.entrySet().stream()
                .filter(f -> {
                    long currentFileAccessesCount = this.getFileAccessesCount(f.getKey());
                    CachedFile currentFile = f.getValue();
                    long currentFileSize = currentFile.getFileContent().length;

                    return (fileAccessesCount > currentFileAccessesCount && Math.abs(fileAccessesCount - currentFileAccessesCount) > 10) ||
                            (fileSize > currentFileSize && Math.abs(fileSize - currentFileSize) > 100000);

                }).max(Comparator.comparingLong(f -> f.getValue().getLastTimeAccessed())).orElse(null);

        if (stringCachedFileEntry == null) {
            return null;
        }

        return stringCachedFileEntry.getKey();
    }
}
