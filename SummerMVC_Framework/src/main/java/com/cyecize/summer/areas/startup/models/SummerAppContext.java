package com.cyecize.summer.areas.startup.models;

import com.cyecize.summer.areas.startup.services.DependencyContainer;

public class SummerAppContext {

    private final DependencyContainer dependencyContainer;

    private final ScannedObjects scannedObjects;

    public SummerAppContext(DependencyContainer dependencyContainer, ScannedObjects scannedObjects) {
        this.dependencyContainer = dependencyContainer;
        this.scannedObjects = scannedObjects;
    }

    public DependencyContainer getDependencyContainer() {
        return this.dependencyContainer;
    }

    public ScannedObjects getScannedObjects() {
        return this.scannedObjects;
    }
}
