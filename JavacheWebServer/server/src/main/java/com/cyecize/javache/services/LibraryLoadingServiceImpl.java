package com.cyecize.javache.services;

import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.common.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@JavacheComponent
public class LibraryLoadingServiceImpl implements LibraryLoadingService {

    private static final String INVALID_FOLDER_MESSAGE_FORMAT = "Library Folder \"%s\" does not exist or it's not a folder!";

    private final JavacheConfigService configService;

    private final Map<File, URL> libURLs;

    private final Map<File, URL> apiURLs;

    public LibraryLoadingServiceImpl(JavacheConfigService configService) {
        this.configService = configService;
        this.libURLs = new HashMap<>();
        this.apiURLs = new HashMap<>();
    }

    @Override
    public void loadLibraries() {
        final String libDir = this.getLibraryDirectory();
        final String apiDir = this.getApiDirectory();
        this.setLibraries(new File(libDir), this.libURLs);
        this.setLibraries(new File(apiDir), this.apiURLs);

        int b = 10;
    }

    private void setLibraries(File dir, Map<File, URL> libs) {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException(String.format(INVALID_FOLDER_MESSAGE_FORMAT, dir));
        }

        Arrays.stream(dir.listFiles())
                .filter(this::isJarFile)
                .forEach(f -> {
                    try {
                        libs.put(f, ReflectionUtils.createJarURL(f.getCanonicalPath()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Map<File, URL> getLibURLs() {
        return this.libURLs;
    }

    @Override
    public Map<File, URL> getApiURLs() {
        return this.apiURLs;
    }

    private String getLibraryDirectory() {
        return this.configService.getConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, String.class) +
                this.configService.getConfigParam(JavacheConfigValue.LIB_DIR_NAME, String.class);
    }

    private String getApiDirectory() {
        return this.configService.getConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, String.class) +
                this.configService.getConfigParam(JavacheConfigValue.API_DIR_NAME, String.class);
    }

    /**
     * Checks if a file name ends with .jar
     */
    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }
}
