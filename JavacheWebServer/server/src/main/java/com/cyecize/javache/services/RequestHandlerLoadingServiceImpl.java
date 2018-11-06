package com.cyecize.javache.services;

import com.cyecize.WebConstants;
import com.cyecize.javache.api.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class RequestHandlerLoadingServiceImpl implements RequestHandlerLoadingService {

    private static final String LIB_FOLDER_PATH = WebConstants.WORKING_DIRECTORY + "lib/";

    private static final String INVALID_FOLDER_MESSAGE = "Library Folder does not exist or it's not a folder!";

    private List<RequestHandler> requestHandlers;

    public RequestHandlerLoadingServiceImpl() {

    }

    @Override
    public void loadRequestHandlers(List<String> requestHandlerPriority) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.requestHandlers = new LinkedList<>();
        URLClassLoader ucl = new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(ucl);
        this.loadLibraryFiles(requestHandlerPriority);
    }

    @Override
    public List<RequestHandler> getRequestHandlers() {
        return Collections.unmodifiableList(this.requestHandlers);
    }

    private void loadLibraryFiles(List<String> requestHandlerPriority) throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        File libraryFolder = new File(LIB_FOLDER_PATH);

        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            throw new IllegalArgumentException(INVALID_FOLDER_MESSAGE);
        }

        List<File> allJarFiles = Arrays.stream(libraryFolder.listFiles())
                .filter(this::isJarFile)
                .collect(Collectors.toList());

        for (File jFile : allJarFiles) {
            this.addJarFileToClassPath(jFile.getCanonicalPath());
        }

        for (String currentRequestHandlerName : requestHandlerPriority) {
            File jarFile = allJarFiles.stream()
                    .filter(x -> this.trimFileNameExtension(x).equals(currentRequestHandlerName))
                    .findFirst().orElse(null);

            if (jarFile != null) {
                JarFile fileAsJar = new JarFile(jarFile.getCanonicalPath());
                this.loadJarFile(fileAsJar);
            }
        }
    }

    private void addJarFileToClassPath(String canonicalPath) throws MalformedURLException {
        URL url = new URL("jar:file:" + canonicalPath + "!/");
        //TODO this method does not work for Java 9 and beyond since they URLClassLoader is no longer used. Find an alternative.
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] { url });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void loadJarFile(JarFile jarFile) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Enumeration<JarEntry> jarFileEntries = jarFile.entries();

        while (jarFileEntries.hasMoreElements()) {
            JarEntry currentEntry = jarFileEntries.nextElement();

            if (!currentEntry.isDirectory() && currentEntry.getName().endsWith(".class")) {

                String className = currentEntry
                        .getName()
                        .replace(".class", "")
                        .replace("/", ".");

                Class currentClassFile = Thread.currentThread().getContextClassLoader().loadClass(className);

                if (RequestHandler.class.isAssignableFrom(currentClassFile)) {
                    this.loadRequestHandler(currentClassFile);
                }
            }
        }
    }

    private void loadRequestHandler(Class<?> requestHandlerClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        RequestHandler requestHandlerObject = (RequestHandler) requestHandlerClass
                .getDeclaredConstructor(String.class)
                .newInstance(WebConstants.WORKING_DIRECTORY);
        this.requestHandlers.add(requestHandlerObject);
    }

    private String trimFileNameExtension(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf("."));
    }

    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }
}
