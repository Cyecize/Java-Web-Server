package com.cyecize.broccolina.services;

import java.io.File;
import java.io.IOException;

public interface JarFileUnzipService {
    void unzipJar(File jarFile) throws IOException;

    void unzipJar(File jarFile, boolean overwriteExistingFiles, String outputDirectory) throws IOException;
}
