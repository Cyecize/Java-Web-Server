package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.FileScanException;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FileScanServiceImpl implements FileScanService {

    private static final String INVALID_SCAN_FOLDER_FORMAT = "Error scanning folder \"%s\"";

    private static final String CLASS_NOT_FOUND_MESSAGE = "Could not load class!";

    private String scanDir;

    private Set<Class<?>> availableClasses;

    public FileScanServiceImpl(Class startupClass) {
        this.scanDir = this.getScanFolder(startupClass);
        this.availableClasses = new HashSet<>();
    }

    @Override
    public Set<Class<?>> scanFiles() throws FileScanException {
        File file = new File(this.scanDir);
        if (!file.exists() || !file.isDirectory()) {
            throw new FileScanException(String.format(INVALID_SCAN_FOLDER_FORMAT, this.scanDir));
        }
        try {
            for (File mainDir : file.listFiles()) {
                this.scanDir(mainDir, "");
            }
        } catch (ClassNotFoundException ex) {
            throw new FileScanException(CLASS_NOT_FOUND_MESSAGE, ex);
        }
        return this.availableClasses;
    }

    private void scanDir(File dir, String packageName) throws ClassNotFoundException {
        if (dir.isDirectory()) {
            packageName += dir.getName() + ".";
            for (File file : dir.listFiles()) {
                this.scanDir(file, packageName);
            }
        } else if (dir.getName().contains(".class")) { //file is class
            var className = packageName + dir.getName().replace(".class", "");
            Class<?> cls = Class.forName(className);
            this.availableClasses.add(cls);
        }
    }

    private String getScanFolder(Class cls) {
        String name = cls.getName()
                .replace(cls.getSimpleName(), "")
                .replace(".", "/");
        return cls.getResource("").toString()
                .replace("file:/", "")
                .replace(name, "");
    }
}
