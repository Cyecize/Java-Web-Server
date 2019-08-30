package com.cyecize.javache.services;

import com.cyecize.WebConstants;
import com.cyecize.javache.api.RequestHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
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

    private final JavacheConfigService configService;

    private List<RequestHandler> requestHandlers;

    public RequestHandlerLoadingServiceImpl(JavacheConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void loadRequestHandlers(List<String> requestHandlerPriority) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.requestHandlers = new LinkedList<>();
        this.loadLibraryFiles(requestHandlerPriority);
    }

    @Override
    public List<RequestHandler> getRequestHandlers() {
        return Collections.unmodifiableList(this.requestHandlers);
    }

    /**
     * Scans the lib folder for jar files and adds them to the classpath.
     * Then iterates the list of request handlers and for each request handler name
     * looks for a corresponding jar file in the lib folder.
     * Then loads that request handler.
     */
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

    /**
     * Adds URL to the system classloader assuming that the system
     * classloader is an instance of URLClassLoader.
     * This is not the case for java version 9 and above so
     * at the start of the program a method is run that replaces the new system
     * classloader with an instance of URLClassLoader.
     */
    private void addJarFileToClassPath(String canonicalPath) throws MalformedURLException {
        URL url = new URL("jar:file:" + canonicalPath + "!/");
        Class<URLClassLoader> uclType = URLClassLoader.class;
        try {
            Method method = uclType.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(ClassLoader.getSystemClassLoader(), url);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Iterates all entries in a jar file and searches for a
     * class that is assignable from RequestHandler interface and loads it.
     */
    private void loadJarFile(JarFile jarFile) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Enumeration<JarEntry> jarFileEntries = jarFile.entries();

        while (jarFileEntries.hasMoreElements()) {
            JarEntry currentEntry = jarFileEntries.nextElement();

            if (!currentEntry.isDirectory() && currentEntry.getName().endsWith(".class")) {

                String className = currentEntry.getName()
                        .replace(".class", "")
                        .replace("/", ".");

                Class currentClassFile = Class.forName(className, true, Thread.currentThread().getContextClassLoader());

                if (RequestHandler.class.isAssignableFrom(currentClassFile)) {
                    this.loadRequestHandler(currentClassFile);
                }
            }
        }
    }

    /**
     * Creates an instance of a request handler
     * and adds the instance to a linkedList.
     */
    private void loadRequestHandler(Class<RequestHandler> requestHandlerClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        RequestHandler requestHandler;
        try {
            requestHandler = requestHandlerClass.getDeclaredConstructor(String.class, JavacheConfigService.class).newInstance(WebConstants.WORKING_DIRECTORY, this.configService);
        } catch (NoSuchMethodException ignored) {
            requestHandler = requestHandlerClass.getConstructor().newInstance();
        }
        this.requestHandlers.add(requestHandler);
    }

    /**
     * Removes the file name extension.
     */
    private String trimFileNameExtension(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf("."));
    }

    /**
     * Checks if a file name ends with .jar
     */
    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }
}
