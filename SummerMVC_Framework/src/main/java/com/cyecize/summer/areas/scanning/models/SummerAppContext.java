package com.cyecize.summer.areas.scanning.models;

import com.cyecize.summer.areas.scanning.services.DependencyContainer;

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
