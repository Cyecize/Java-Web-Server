package com.cyecize.broccolina.services;

import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.JavacheComponent;
import com.cyecize.javache.common.ReflectionUtils;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.solet.BaseHttpSolet;
import com.cyecize.solet.HttpSolet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@JavacheComponent
public class ApplicationScanningServiceImpl implements ApplicationScanningService {

    private final JarFileUnzipService jarFileUnzipService;

    private final JavacheConfigService configService;

    private final List<String> applicationNames;

    private final Map<String, List<Class<HttpSolet>>> soletClasses;

    private final String applicationsFolderPath;

    private final String compileOutputFolderName;

    private final String applicationLibFolderName;

    private final boolean skipExtractingAppsWithExistingFolder;

    @Autowired
    public ApplicationScanningServiceImpl(JarFileUnzipService jarFileUnzipService, JavacheConfigService configService) {
        this.jarFileUnzipService = jarFileUnzipService;
        this.configService = configService;

        this.applicationNames = new ArrayList<>();
        this.soletClasses = new HashMap<>();

        this.compileOutputFolderName = this.configService.getConfigParam(JavacheConfigValue.APP_COMPILE_OUTPUT_DIR_NAME, String.class);
        this.applicationsFolderPath = configService.getConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, String.class)
                + this.configService.getConfigParam(JavacheConfigValue.WEB_APPS_DIR_NAME, String.class);

        this.applicationLibFolderName = this.configService.getConfigParam(JavacheConfigValue.APPLICATION_DEPENDENCIES_FOLDER_NAME, String.class);

        this.skipExtractingAppsWithExistingFolder = this.configService.getConfigParam(JavacheConfigValue.BROCOLLINA_SKIP_EXTRACTING_IF_FOLDER_EXISTS, Boolean.class);
    }

    @Override
    public List<String> getApplicationNames() {
        return this.applicationNames;
    }

    /**
     * Starts scanning apps from javache's webapps folder.
     * Iterates over every jar file in the folder and for each jar file,
     * calls the jarUnzipService to extract the file.
     * Extracts the app name, and starts an application scan.
     * <p>
     * Returns map of application name and a list of solet classes.
     */
    @Override
    public Map<String, List<Class<HttpSolet>>> findSoletClasses() throws IOException, ClassNotFoundException {
        final File applicationsFolder = new File(this.applicationsFolderPath);

        if (applicationsFolder.exists() && applicationsFolder.isDirectory()) {
            final List<File> allJarFiles = Arrays.stream(Objects.requireNonNull(applicationsFolder.listFiles()))
                    .filter(this::isJarFile)
                    .collect(Collectors.toList());

            for (File applicationJarFile : allJarFiles) {
                final String appName = applicationJarFile.getName().replace(".jar", "");
                final String extractedJarFolderName = applicationJarFile.getCanonicalPath().replace(".jar", File.separator);

                if (!this.skipExtractingAppsWithExistingFolder || !Files.exists(Paths.get(extractedJarFolderName))) {
                    this.jarFileUnzipService.unzipJar(applicationJarFile, this.configService.getConfigParam(JavacheConfigValue.BROCCOLINA_FORCE_OVERWRITE_FILES, Boolean.class));
                }

                this.loadApplicationFromFolder(extractedJarFolderName, appName);
            }
        }

        return this.soletClasses;
    }

    /**
     * Loads application libraries.
     * Loads application classes.
     * Adds the application name to the applicationNames list.
     */
    private void loadApplicationFromFolder(String applicationRootFolderPath, String applicationName) throws IOException, ClassNotFoundException {
        final String classesRootFolderPath = applicationRootFolderPath + this.compileOutputFolderName + File.separator;
        final String librariesRootFolderPath = applicationRootFolderPath + this.applicationLibFolderName + File.separator;
        final File classesRootDirectory = new File(classesRootFolderPath);

        if (!classesRootDirectory.exists() || !classesRootDirectory.isDirectory()) {
            return;
        }

        final URLClassLoader classLoader = this.createNewClassLoader(classesRootDirectory.getCanonicalPath() + File.separator);

        this.loadApplicationLibraries(librariesRootFolderPath, classLoader);
        this.loadClass(classesRootDirectory, "", applicationName, classLoader);
        this.applicationNames.add("/" + applicationName);
    }


    /**
     * Recursive method for loading classes, starts with empty packageName.
     * If the file is directory, iterate all files inside and call loadClass with the current file name
     * appended to the packageName.
     * <p>
     * If the file is file and the file name ends with .class, load it and check if the class
     * is assignable from BaseHttpSolet. If it is, add it to the map of solet classes.
     */
    private void loadClass(File currentFile, String packageName, String applicationName, URLClassLoader classLoader) throws ClassNotFoundException {
        if (currentFile.isDirectory()) {
            for (File childFile : currentFile.listFiles()) {
                this.loadClass(childFile, (packageName + currentFile.getName() + "."), applicationName, classLoader);
            }
        } else {
            if (!currentFile.getName().endsWith(".class")) {
                return;
            }

            final String className = (packageName.replace(this.compileOutputFolderName + ".", "")) + currentFile
                    .getName()
                    .replace(".class", "")
                    .replace("/", ".");

            final Class currentClassFile = Class.forName(className, true, classLoader);

            if (BaseHttpSolet.class.isAssignableFrom(currentClassFile)) {
                if (!this.soletClasses.containsKey(applicationName)) {
                    this.soletClasses.put(applicationName, new ArrayList<>());
                }

                this.soletClasses.get(applicationName).add(currentClassFile);
            }
        }
    }

    /**
     * Iterates the given directory's files and filters jar files
     * then adds them to the system classpath.
     */
    private void loadApplicationLibraries(String librariesRootFolderPath, URLClassLoader classLoader) {
        final File libraryFolder = new File(librariesRootFolderPath);

        if (!libraryFolder.exists() || !libraryFolder.isDirectory()) {
            return;
        }

        Arrays.stream(Objects.requireNonNull(libraryFolder.listFiles()))
                .filter(this::isJarFile)
                .forEach(jf -> {
                    try {
                        this.addJarFileToClassLoader(jf.getCanonicalPath(), classLoader);
                    } catch (IOException ignored) {
                    }
                });
    }

    private URLClassLoader createNewClassLoader(String canonicalPath) {
        try {
            return new URLClassLoader(
                    new URL[]{new URL("file:/" + canonicalPath)},
                    Thread.currentThread().getContextClassLoader()
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a proper URL format for .jar files and adds it to a given classloader.
     */
    private void addJarFileToClassLoader(String canonicalPath, URLClassLoader classLoader) {
        try {
            ReflectionUtils.addUrlToClassLoader(new URL("jar:file:" + canonicalPath + "!/"), classLoader);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Checks if a file's name ends with .jar
     */
    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }
}
