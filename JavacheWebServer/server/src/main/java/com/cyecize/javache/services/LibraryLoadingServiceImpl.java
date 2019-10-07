package com.cyecize.javache.services;

import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.common.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JavacheComponent
public class LibraryLoadingServiceImpl implements LibraryLoadingService {

    private static final String INVALID_FOLDER_MESSAGE_FORMAT = "Library Folder \"%s\" does not exist or it's not a folder!";

    private final JavacheConfigService configService;

    private List<File> jarFiles;

    public LibraryLoadingServiceImpl(JavacheConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void loadLibraries() {
        final String libDir = this.getLibraryDirectory();
        final File libraryFolder = new File(libDir);

        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            throw new IllegalArgumentException(String.format(INVALID_FOLDER_MESSAGE_FORMAT, libDir));
        }

        this.jarFiles = Arrays.stream(libraryFolder.listFiles())
                .filter(this::isJarFile)
                .collect(Collectors.toList());

        for (File jFile : this.jarFiles) {
            try {
                ReflectionUtils.addJarFileToClassPath(jFile.getCanonicalPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public List<File> getJarLibs() {
        return this.jarFiles;
    }

    private String getLibraryDirectory() {
        return this.configService.getConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, String.class) +
                this.configService.getConfigParam(JavacheConfigValue.LIB_DIR_NAME, String.class);
    }

    /**
     * Checks if a file name ends with .jar
     */
    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }
}
