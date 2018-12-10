package com.cyecize.javache.embedded.services;

import com.cyecize.broccolina.services.ApplicationScanningService;
import com.cyecize.javache.ConfigConstants;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.solet.BaseHttpSolet;
import com.cyecize.solet.HttpSolet;

import java.io.File;
import java.util.*;

public class EmbeddedApplicationScanningService implements ApplicationScanningService {

    private static final String CLASSES_PACKAGE_PREFIX = "classes.";

    private final JavacheConfigService configService;

    private final String workingDir;

    private String rootAppName;

    private Map<String, List<Class<HttpSolet>>> soletClasses;

    public EmbeddedApplicationScanningService(JavacheConfigService configService, String workingDir) {
        this.configService = configService;
        this.workingDir = workingDir;
        this.soletClasses = new HashMap<>();
        this.rootAppName = configService.getConfigParam(ConfigConstants.MAIN_APP_JAR_NAME, String.class);
        this.soletClasses.put(this.rootAppName, new ArrayList<>());
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
        this.loadClass(new File(this.workingDir), "");
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
            for (File childFile : currentFile.listFiles()) {
                this.loadClass(childFile, (packageName + currentFile.getName() + "."));
            }
        } else {
            if (!currentFile.getName().endsWith(".class")) {
                return;
            }

            String className = packageName.replace(CLASSES_PACKAGE_PREFIX, "") + currentFile
                    .getName()
                    .replace(".class", "")
                    .replace("/", ".");

            Class currentClassFile = Class.forName(className, true, Thread.currentThread().getContextClassLoader());

            if (BaseHttpSolet.class.isAssignableFrom(currentClassFile)) {
                this.soletClasses.get(this.rootAppName).add(currentClassFile);
            }
        }
    }
}
