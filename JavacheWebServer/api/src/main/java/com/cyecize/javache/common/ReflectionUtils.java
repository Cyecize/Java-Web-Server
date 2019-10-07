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
        if (!(ClassLoader.getSystemClassLoader() instanceof URLClassLoader)) {
            try {
                replaceSystemClassLoader();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        final URL url = new URL("jar:file:" + canonicalPath + "!/");
        final Class<URLClassLoader> uclType = URLClassLoader.class;

        try {
            final Method method = uclType.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(ClassLoader.getSystemClassLoader(), url);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
