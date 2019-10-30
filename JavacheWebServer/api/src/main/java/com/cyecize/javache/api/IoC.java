package com.cyecize.javache.api;

import com.cyecize.ioc.services.DependencyContainer;

import java.net.URL;
import java.net.URLClassLoader;

public class IoC {
    private static DependencyContainer javacheDependencyContainer;

    private static DependencyContainer requestHandlersDependencyContainer;

    private static URLClassLoader apiClassLoader = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());

    private static URLClassLoader requestHandlersClassLoader = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());

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

    public static void setApiClassLoader(URLClassLoader apiClassLoader) {
        IoC.apiClassLoader = apiClassLoader;
    }

    public static URLClassLoader getApiClassLoader() {
        return apiClassLoader;
    }

    public static void setRequestHandlersClassLoader(URLClassLoader requestHandlersClassLoader) {
        IoC.requestHandlersClassLoader = requestHandlersClassLoader;
    }

    public static URLClassLoader getRequestHandlersClassLoader() {
        return requestHandlersClassLoader;
    }
}
