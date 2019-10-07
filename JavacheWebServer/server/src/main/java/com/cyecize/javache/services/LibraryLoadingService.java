package com.cyecize.javache.services;

import java.io.File;
import java.util.List;

public interface LibraryLoadingService {
    void loadLibraries();

    List<File> getJarLibs();
}
