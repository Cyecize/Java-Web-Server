package com.cyecize.javache.api;

import com.cyecize.ioc.services.DependencyContainer;

public class IoC {
    private static DependencyContainer javacheDependencyContainer;

    public void setDependencyContainer(DependencyContainer dependencyContainer) {
        if (dependencyContainer != null) {
            javacheDependencyContainer = dependencyContainer;
        }
    }

    public static DependencyContainer getJavacheDependencyContainer() {
        return javacheDependencyContainer;
    }
}
