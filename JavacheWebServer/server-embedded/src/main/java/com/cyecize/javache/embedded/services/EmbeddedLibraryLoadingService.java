package com.cyecize.javache.embedded.services;

import com.cyecize.javache.embedded.internal.JavacheEmbeddedComponent;
import com.cyecize.javache.services.LibraryLoadingService;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@JavacheEmbeddedComponent
public class EmbeddedLibraryLoadingService implements LibraryLoadingService {

    @Override
    public void loadLibraries() {

    }

    @Override
    public Map<File, URL> getLibURLs() {
        return new HashMap<>();
    }

    @Override
    public Map<File, URL> getApiURLs() {
        return new HashMap<>();
    }
}
