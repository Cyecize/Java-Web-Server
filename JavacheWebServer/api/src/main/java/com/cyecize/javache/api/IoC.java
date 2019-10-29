package com.cyecize.javache.api;

import com.cyecize.ioc.services.DependencyContainer;

public class IoC {
    private static DependencyContainer javacheDependencyContainer;

    private static DependencyContainer requestHandlersDependencyContainer;

    public static void setJavacheDependencyContainer(DependencyContainer dependencyContainer) {
        if (dependencyContainer != null) {
            javacheDependencyContainer = dependencyContainer;
        }
    }

    public static DependencyContainer getJavacheDependencyContainer() {
        return javacheDependencyContainer;
    }

    public static void setRequestHandlersDependencyContainer(DependencyContainer dependencyContainer) {
        requestHandlersDependencyContainer = dependencyContainer;
    }

    public static DependencyContainer getRequestHandlersDependencyContainer() {
        return requestHandlersDependencyContainer;
    }
}
