package com.cyecize.broccolina.services;

import com.cyecize.solet.BaseHttpSolet;
import com.cyecize.solet.HttpSolet;
import com.cyecize.solet.SoletConfigImpl;
import com.cyecize.solet.WebSolet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;

public class ApplicationLoadingServiceImpl implements ApplicationLoadingService {

    private static final String ROOT_APPLICATION_FILE_NAME = "ROOT";

    private static final String MISSING_SOLET_ANNOTATION_FORMAT = "Missing solet annotation for class named %s.";

    private static final String APPLICATION_LIB_FOLDER_NAME = "lib";

    private static final String APPLICATION_CLASSES_FOLDER_NAME = "classes";

    private final JarFileUnzipService jarFileUnzipService;

    private final String applicationsFolderPath;

    private final String assetsDir;

    private Map<String, HttpSolet> solets;

    private List<String> applicationNames;

    public ApplicationLoadingServiceImpl(JarFileUnzipService jarFileUnzipService, String applicationsFolderPath, String assetsDir) {
        this.jarFileUnzipService = jarFileUnzipService;
        this.applicationsFolderPath = applicationsFolderPath;
        this.assetsDir = assetsDir;
        this.solets = new HashMap<>();
        this.applicationNames = new ArrayList<>();
        this.makeAppAssetDir(this.assetsDir);
    }

    @Override
    public List<String> getApplicationNames() {
        return this.applicationNames;
    }

    /**
     * Starts scanning apps from javache's webapps folder.
     * Iterates over every jar file in the folder and for each jar file,
     * calls the jarUnzipService to extract the file.
     * Extracts the app name, creates an asset directory and
     * starts an application scan.
     * <p>
     * Returns map of loaded solets.
     */
    @Override
    public Map<String, HttpSolet> loadApplications() throws IOException {
        try {
            File applicationsFolder = new File(this.applicationsFolderPath);

            if (applicationsFolder.exists() && applicationsFolder.isDirectory()) {
                List<File> allJarFiles = Arrays.stream(applicationsFolder.listFiles()).filter(this::isJarFile).collect(Collectors.toList());
                for (File applicationJarFile : allJarFiles) {
                    this.jarFileUnzipService.unzipJar(applicationJarFile);

                    String appName = applicationJarFile.getName().replace(".jar", "");
                    this.makeAppAssetDir(this.assetsDir + appName + File.separator);
                    this.loadApplicationFromFolder(applicationJarFile.getCanonicalPath().replace(".jar", File.separator), appName);
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return this.solets;
    }

    /**
     * Loads application libraries.
     * Loads application classes.
     * Adds the application name to the applicationNames list.
     */
    private void loadApplicationFromFolder(String applicationRootFolderPath, String applicationName) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String classesRootFolderPath = applicationRootFolderPath + APPLICATION_CLASSES_FOLDER_NAME + File.separator;
        String librariesRootFolderPath = applicationRootFolderPath + APPLICATION_LIB_FOLDER_NAME + File.separator;

        this.loadApplicationLibraries(librariesRootFolderPath);
        this.loadApplicationClasses(classesRootFolderPath, applicationName);
        this.applicationNames.add("/" + applicationName);
    }

    /**
     * If the directory does not exist, return.
     * Adds the directory to the classpath.
     * Starts a recursion for loading classes and finding solets.
     */
    private void loadApplicationClasses(String classesRootFolderPath, String currentApplicationName) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        File classesRootDirectory = new File(classesRootFolderPath);
        if (!classesRootDirectory.exists() || !classesRootDirectory.isDirectory()) {
            return;
        }
        this.addDirectoryToClassPath(classesRootDirectory.getCanonicalPath() + File.separator);
        this.loadClass(classesRootDirectory, "", currentApplicationName);
    }

    /**
     * Recursive method for loading classes, starts with empty packageName.
     * If the file is directory, iterate all files inside and call loadClass with the current file name
     * appended to the packageName.
     * <p>
     * If the file is file and the file name ends with .class, load it and check if the class
     * is assignable from BaseHttpSolet. If it is, load the solet.
     */
    private void loadClass(File currentFile, String packageName, String applicationName) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (currentFile.isDirectory()) {
            for (File childFile : currentFile.listFiles()) {
                this.loadClass(childFile, (packageName + currentFile.getName() + "."), applicationName);
            }
        } else {
            if (!currentFile.getName().endsWith(".class")) {
                return;
            }

            String className = (packageName.replace(APPLICATION_CLASSES_FOLDER_NAME + ".", "")) + currentFile
                    .getName()
                    .replace(".class", "")
                    .replace("/", ".");

            Class currentClassFile = Class.forName(className, true, Thread.currentThread().getContextClassLoader());

            if (BaseHttpSolet.class.isAssignableFrom(currentClassFile)) {
                this.loadSolet(currentClassFile, applicationName);
            }
        }
    }

    /**
     * Creates an instance of the solet.
     * If the application name is different than ROOT.jar, add the appName to the route.
     * Put the solet in a solet map with a key being the soletRoute.
     */
    private void loadSolet(Class<BaseHttpSolet> soletClass, String applicationName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        HttpSolet soletInstance = soletClass.getDeclaredConstructor().newInstance();
        WebSolet soletAnnotation = this.getSoletAnnotation(soletInstance.getClass());
        if (soletAnnotation == null) {
            throw new IllegalArgumentException(String.format(MISSING_SOLET_ANNOTATION_FORMAT, soletClass.getName()));
        }

        String soletRoute = soletAnnotation.value();
        if (!applicationName.equals(ROOT_APPLICATION_FILE_NAME)) {
            soletRoute = "/" + applicationName + soletRoute;
        }

        if (!soletInstance.isInitialized()) {
            soletInstance.init(new SoletConfigImpl());
        }

        soletInstance.setAppNamePrefix("/" + applicationName);
        soletInstance.setAssetsFolder(this.assetsDir + applicationName);
        this.solets.put(soletRoute, soletInstance);
    }

    /**
     * Recursive method for getting @WebSolet annotation from a given class.
     * Recursion is required since only parent class could have @WebSolet annotation
     * and not the child.
     */
    private WebSolet getSoletAnnotation(Class<?> soletClass) {
        WebSolet solet = soletClass.getAnnotation(WebSolet.class);
        if (solet == null && soletClass.getSuperclass() != null) {
            return getSoletAnnotation(soletClass.getSuperclass());
        }
        return solet;
    }

    /**
     * Iterates the given directory's files and filters jar files
     * then adds them to the system classpath.
     */
    private void loadApplicationLibraries(String librariesRootFolderPath) {

        File libraryFolder = new File(librariesRootFolderPath);
        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            return;
        }

        Arrays.stream(libraryFolder.listFiles()).filter(this::isJarFile)
                .forEach(jf -> {
                    try {
                        this.addJarFileToClassPath(jf.getCanonicalPath());
                    } catch (IOException ignored) {
                    }
                });
    }

    /**
     * Creates a proper URL for directory and adds it to the system classloader.
     */
    private void addDirectoryToClassPath(String canonicalPath) {
        try {
            this.addUrlToClassPath(new URL("file:/" + canonicalPath));
        } catch (MalformedURLException ignored) {
        }
    }

    /**
     * Creates a proper URL format for .jar files and adds it to the system classloader.
     */
    private void addJarFileToClassPath(String canonicalPath) {
        try {
            this.addUrlToClassPath(new URL("jar:file:" + canonicalPath + "!/"));
        } catch (MalformedURLException ignored) {
        }
    }

    /**
     * Adds a URL to the current system classloader.
     * This method works by default on Java 8.
     * On newer versions it is required to first replace the system classloader with an instance of
     * URLClassLoader. This is done at the start of Javache.
     */
    private void addUrlToClassPath(URL url) {
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

    /**
     * Creates asset directory for the current app in javache's assets directory.
     */
    private void makeAppAssetDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /**
     * Checks if a file's name ends with .jar
     */
    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }
}
