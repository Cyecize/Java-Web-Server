package com.cyecize.javache.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class ReflectionUtils {

    /**
     * This is a workaround for java 9 and above.
     * Since java 9 the systemClassLoader is no longer URLClassLoader
     * which means that there is no method "addUrl"
     * by doing this we change the default classLoader with URLClassLoader
     *
     * @throws IllegalAccessException
     */
    public static void replaceSystemClassLoader() throws IllegalAccessException {
        final Field scl = Arrays.stream(ClassLoader.class.getDeclaredFields())
                .filter(f -> f.getType() == ClassLoader.class && !f.getName().equals("parent"))
                .findFirst().orElse(null);

        scl.setAccessible(true);
        scl.set(null, new URLClassLoader(new URL[0]));
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
    }

    /**
     * Adds URL to the system classloader assuming that the system
     * classloader is an instance of URLClassLoader.
     * This is not the case for java version 9 and above so
     * at the start of the program a method is run that replaces the new system
     * classloader with an instance of URLClassLoader.
     */
    public static void addJarFileToClassPath(String canonicalPath) throws MalformedURLException {
        final URL url = new URL("jar:file:" + canonicalPath + "!/");
        addUrlToClassPath(url);
    }

    /**
     * Adds a URL to the current system classloader.
     * This method works by default on Java 8.
     * On newer versions it is required to first replace the system classloader with an instance of
     * URLClassLoader. This is done at the start of Javache.
     */
    public static void addUrlToClassPath(URL url) {
        if (!(ClassLoader.getSystemClassLoader() instanceof URLClassLoader)) {
            try {
                replaceSystemClassLoader();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            final URLClassLoader sysClassLoaderInstance = (URLClassLoader) ClassLoader.getSystemClassLoader();
            final Class<URLClassLoader> sysClassLoaderType = URLClassLoader.class;

            final Method method = sysClassLoaderType.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysClassLoaderInstance, url);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
