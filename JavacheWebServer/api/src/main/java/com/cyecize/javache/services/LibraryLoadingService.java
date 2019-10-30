package com.cyecize.javache.services;

import java.io.File;
import java.net.URL;
import java.util.Map;

public interface LibraryLoadingService {
    void loadLibraries();

    Map<File, URL> getLibURLs();

    Map<File, URL> getApiURLs();
}
