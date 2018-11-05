package com.cyecize.broccolina.services;

import com.cyecize.solet.BaseHttpSolet;
import com.cyecize.solet.HttpSolet;
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

    private final JarFileUnzipService jarFileUnzipService;

    private Map<String, HttpSolet> solets;

    private List<String> applicationNames;

    public ApplicationLoadingServiceImpl(JarFileUnzipService jarFileUnzipService) {
        this.jarFileUnzipService = jarFileUnzipService;
        this.solets = new HashMap<>();
        this.applicationNames = new ArrayList<>();
    }

    @Override
    public List<String> getApplicationNames() {
        return this.applicationNames;
    }

    @Override
    public Map<String, HttpSolet> loadApplications(String applicationsFolderPath) throws IOException {
        try {
            File applicationsFolder = new File(applicationsFolderPath);

            if (applicationsFolder.exists() && applicationsFolder.isDirectory()) {
                List<File> allJarFiles = Arrays.stream(applicationsFolder.listFiles()).filter(this::isJarFile).collect(Collectors.toList());
                for (File applicationJarFile : allJarFiles) {
                    this.jarFileUnzipService.unzipJar(applicationJarFile);

                    this.loadApplicationFromFolder(applicationJarFile.getCanonicalPath()
                            .replace(".jar", File.separator), applicationJarFile.getName()
                            .replace(".jar", "")
                    );
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return this.solets;
    }

    private void loadApplicationFromFolder(String applicationRootFolderPath, String applicationName) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        String classesRootFolderPath = applicationRootFolderPath + "classes" + File.separator;
        String librariesRootFolderPath = applicationRootFolderPath + "lib" + File.separator;

        this.loadApplicationLibraries(librariesRootFolderPath);
        this.loadApplicationClasses(classesRootFolderPath, applicationName);
        this.applicationNames.add("/" + applicationName);
    }

    private void loadApplicationClasses(String classesRootFolderPath, String currentApplicationName) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        File classesRootDirectory = new File(classesRootFolderPath);
        if (!classesRootDirectory.exists() || !classesRootDirectory.isDirectory()) {
            return;
        }
        this.addDirectoryToClassPath(classesRootDirectory.getCanonicalPath() + "/");
        this.loadClass(classesRootDirectory, "", currentApplicationName);
    }

    private void loadClass(File currentFile, String packageName, String applicationName) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (currentFile.isDirectory()) {
            for (File childFile : currentFile.listFiles()) {
                this.loadClass(childFile, (packageName + currentFile.getName() + "."), applicationName);
            }
        } else {
            if (!currentFile.getName().endsWith(".class")) {
                return;
            }

            String className = (packageName.replace("classes.", "")) + currentFile
                    .getName()
                    .replace(".class", "")
                    .replace("/", ".");

            Class currentClassFile = Class.forName(className);

            if (BaseHttpSolet.class.isAssignableFrom(currentClassFile)) {
                this.loadSolet(currentClassFile, applicationName);
            }
        }
    }

    private void loadSolet(Class<BaseHttpSolet> soletClass, String applicationName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        HttpSolet soletInstance = soletClass.getDeclaredConstructor().newInstance();
        WebSolet soletAnnotation = soletClass.getAnnotation(WebSolet.class);
        if (soletAnnotation == null) {
            throw new IllegalArgumentException(String.format(MISSING_SOLET_ANNOTATION_FORMAT, soletClass.getName()));
        }
        String soletRoute = soletAnnotation.value();
        if (!applicationName.equals(ROOT_APPLICATION_FILE_NAME)) {
            soletRoute = "/" + applicationName + soletRoute;
        }
        soletInstance.init(null);
        this.solets.put(soletRoute, soletInstance);
    }

    private void loadApplicationLibraries(String librariesRootFolderPath) {

        File libraryFolder = new File(librariesRootFolderPath);

        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            //throw new IllegalArgumentException(String.format(INVALID_FOLDER_FORMAT, librariesRootFolderPath));
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

    private void addDirectoryToClassPath(String canonicalPath) {
        try {
            this.addUrlToClassPath(new URL("file:/" + canonicalPath));
        } catch (MalformedURLException ignored) {
        }
    }

    private void addJarFileToClassPath(String canonicalPath) {
        try {
            this.addUrlToClassPath(new URL("jar:file:" + canonicalPath + "!/"));
        } catch (MalformedURLException ignored) {
        }
    }

    private void addUrlToClassPath(URL url) {
        URLClassLoader ucl = new URLClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(ucl);

        try {
            URLClassLoader sysClassLoaderInstance = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class sysClassLoaderType = URLClassLoader.class;

            Method method = sysClassLoaderType.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysClassLoaderInstance, new Object[]{url});
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }
}
