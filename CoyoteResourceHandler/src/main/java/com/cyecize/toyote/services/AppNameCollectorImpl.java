package com.cyecize.toyote.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AppNameCollectorImpl implements AppNameCollector {

    private static final String APPLICATIONS_FOLDER_NAME = "webapps/";
    private static final String ROOT_APPLICATION_FILE_NAME = "ROOT";
    private static final String APPLICATIONS_FOLDER_NON_EXISTENT = "Invalid applications folder \"%s\"";

    private List<String> applicationNames;

    public AppNameCollectorImpl() {

    }

    @Override
    public List<String> getApplicationNames(String workingDir) {
        if (this.applicationNames != null)
            return this.applicationNames;
        this.loadApplicationNames(workingDir + APPLICATIONS_FOLDER_NAME);
        return this.applicationNames;
    }

    private void loadApplicationNames(String applicationsFolder) {
        this.applicationNames = new ArrayList<>();

        File file = new File(applicationsFolder);
        if (!file.exists() || !file.isDirectory()) {
            throw new RuntimeException(String.format(APPLICATIONS_FOLDER_NON_EXISTENT, applicationsFolder));
        }

        Arrays.stream(file.listFiles()).filter(this::isJarFile).forEach(f -> {
            String s = "/" + f.getName().replace(".jar", "");
            System.out.println("Toyote" + s);
            this.applicationNames.add(s);
        });

    }

    private boolean isJarFile(File file) {
        return file.isFile() && file.getName().endsWith(".jar");
    }
}
