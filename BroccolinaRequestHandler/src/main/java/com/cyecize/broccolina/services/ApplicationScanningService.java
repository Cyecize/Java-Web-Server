package com.cyecize.broccolina.services;

import com.cyecize.solet.HttpSolet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

public interface ApplicationScanningService {

    /**
     * Adds a URL to the current system classloader.
     * This method works by default on Java 8.
     * On newer versions it is required to first replace the system classloader with an instance of
     * URLClassLoader. This is done at the start of Javache.
     */
    default void addUrlToClassPath(URL url) {
        try {
            URLClassLoader sysClassLoaderInstance = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<URLClassLoader> sysClassLoaderType = URLClassLoader.class;

            Method method = sysClassLoaderType.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysClassLoaderInstance, url);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    List<String> getApplicationNames();

    Map<String, List<Class<HttpSolet>>> findSoletClasses() throws IOException, ClassNotFoundException;
}
