package com.cyecize.javache.common;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public final class ReflectionUtils {

    public static void addJarFileToClassLoader(String canonicalPath, URLClassLoader classLoader) throws MalformedURLException {
        addUrlToClassLoader(createJarURL(canonicalPath), classLoader);
    }

    public static void addDirToClassLoader(String canonicalPath, URLClassLoader classLoader) throws MalformedURLException {
        addUrlToClassLoader(createDirURL(canonicalPath), classLoader);
    }

    public static URL createJarURL(String canonicalPath) throws MalformedURLException {
        return new URL("jar:file:" + canonicalPath + "!/");
    }

    public static URL createDirURL(String canonicalPath) throws MalformedURLException {
        final String path = PathUtils.appendPath("file:", canonicalPath, "/");
        return new URL(PathUtils.appendPath(path, ""));
    }

    /**
     * Adds a URL to a given URLClassLoader.
     *
     * @param url         - given URL.
     * @param classLoader - given URLClassLoader.
     */
    public static void addUrlToClassLoader(URL url, URLClassLoader classLoader) {
        try {
            final Class<URLClassLoader> sysClassLoaderType = URLClassLoader.class;

            final Method method = sysClassLoaderType.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
