package com.cyecize.javache.embedded.services;

import com.cyecize.javache.embedded.internal.JavacheEmbeddedComponent;
import com.cyecize.javache.services.LibraryLoadingService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@JavacheEmbeddedComponent
public class EmbeddedLibraryLoadingService implements LibraryLoadingService {
    @Override
    public void loadLibraries() {

    }

    @Override
    public List<File> getJarLibs() {
        return new ArrayList<>();
    }
}
