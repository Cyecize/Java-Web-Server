package com.cyecize.javache.embedded.services;

import com.cyecize.broccolina.services.ApplicationScanningService;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.common.ReflectionUtils;
import com.cyecize.javache.embedded.internal.JavacheEmbeddedComponent;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.solet.BaseHttpSolet;
import com.cyecize.solet.HttpSolet;

import java.io.File;
import java.io.IOException;
import java.util.*;

@JavacheEmbeddedComponent
public class EmbeddedApplicationScanningService implements ApplicationScanningService {

    private final Map<String, List<Class<HttpSolet>>> soletClasses;

    private final JavacheConfigService configService;

    private final String rootAppName;

    private boolean isRootDir;

    @Autowired
    public EmbeddedApplicationScanningService(JavacheConfigService configService) {
        this.soletClasses = new HashMap<>();
        this.configService = configService;
        this.rootAppName = configService.getConfigParam(JavacheConfigValue.MAIN_APP_JAR_NAME, String.class);
        this.soletClasses.put(this.rootAppName, new ArrayList<>());
        this.isRootDir = true;

        this.loadLibraries();
    }

    /**
     * Return "" since we are running only one application in javache embedded.
     */
    @Override
    public List<String> getApplicationNames() {
        return Collections.singletonList(this.rootAppName);
    }

    @Override
    public Map<String, List<Class<HttpSolet>>> findSoletClasses() throws ClassNotFoundException {
        this.loadClass(new File(this.configService.getConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, String.class)), "");
        return this.soletClasses;
    }

    /**
     * Recursive method for loading classes, starts with empty packageName.
     * If the file is directory, iterate all files inside and call loadClass with the current file name
     * appended to the packageName.
     * <p>
     * If the file is file and the file name ends with .class, load it and check if the class
     * is assignable from BaseHttpSolet. If it is, add it to the map of solet classes.
     */
    private void loadClass(File currentFile, String packageName) throws ClassNotFoundException {
        if (currentFile.isDirectory()) {

            //If the folder is the root dir, do not append package name since the name is outside the java packages.
            boolean appendPackage = !this.isRootDir;

            //Since the root dir is reached only once, set it to false.
            this.isRootDir = false;

            for (File childFile : currentFile.listFiles()) {
                if (appendPackage) {
                    this.loadClass(childFile, (packageName + currentFile.getName() + "."));
                } else {
                    this.loadClass(childFile, (packageName));
                }
            }
        } else {
            if (!currentFile.getName().endsWith(".class")) {
                return;
            }

            final String className = packageName + currentFile
                    .getName()
                    .replace(".class", "")
                    .replace("/", ".");


            final Class currentClassFile = Class.forName(className, true, Thread.currentThread().getContextClassLoader());

            if (BaseHttpSolet.class.isAssignableFrom(currentClassFile)) {
                this.soletClasses.get(this.rootAppName).add(currentClassFile);
            }
        }
    }

    /**
     * Checks if there is folder that matches the folder name in the config file (lib by default)
     * Iterates all elements and adds the .jar files to the system's classpath.
     */
    private void loadLibraries() {
        String workingDir = this.configService.getConfigParam(JavacheConfigValue.JAVACHE_WORKING_DIRECTORY, String.class);
        if (!workingDir.endsWith("/") && !workingDir.endsWith("\\")) {
            workingDir += "/";
        }

        final File libFolder = new File(workingDir + this.configService.getConfigParam(JavacheConfigValue.APPLICATION_DEPENDENCIES_FOLDER_NAME, String.class));

        if (!libFolder.exists()) {
            return;
        }

        for (File file : libFolder.listFiles()) {
            if (file.getName().endsWith(".jar")) {
                try {
                    ReflectionUtils.addJarFileToClassPath(file.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
