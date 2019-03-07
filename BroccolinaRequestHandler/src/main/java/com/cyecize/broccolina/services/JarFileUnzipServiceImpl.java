package com.cyecize.broccolina.services;

import com.cyecize.broccolina.utils.FileUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileUnzipServiceImpl implements JarFileUnzipService {

    public JarFileUnzipServiceImpl() {

    }

    @Override
    public void unzipJar(File jarFile) throws IOException {
        this.unzipJar(jarFile, false);
    }

    @Override
    public void unzipJar(File jarFile, boolean overwriteExistingFiles) throws IOException {
        this.unzipJar(jarFile, overwriteExistingFiles, jarFile.getCanonicalPath().replace(".jar", ""));
    }

    @Override
    public void unzipJar(File jarFile, boolean overwriteExistingFiles, String outputDirectory) throws IOException {
        String rootCanonicalPath = jarFile.getCanonicalPath();

        try (JarFile fileAsJarArchive = new JarFile(rootCanonicalPath)) {
            Enumeration<JarEntry> jarEntries = fileAsJarArchive.entries();

            File jarFolder = new File(outputDirectory);

            if (jarFolder.exists() && jarFolder.isDirectory() && overwriteExistingFiles) {
                this.deleteFolder(jarFolder);
            }

            if (!jarFolder.exists()) {
                jarFolder.mkdir();
            }

            while (jarEntries.hasMoreElements()) {
                JarEntry currentEntry = jarEntries.nextElement();

                String currentEntryCanonicalPath = jarFolder.getCanonicalPath() + File.separator + currentEntry.getName();
                File currentEntryAsFile = new File(currentEntryCanonicalPath);

                if (currentEntry.isDirectory()) {
                    currentEntryAsFile.mkdir();
                    continue;
                }

                if (currentEntryAsFile.exists()) {
                    try (InputStream existingFileInputStream = new FileInputStream(currentEntryAsFile)) {
                        try (InputStream jarFileInputStream1 = fileAsJarArchive.getInputStream(currentEntry)) {
                            if (FileUtils.filesMatch(jarFileInputStream1, existingFileInputStream)) {
                                continue;
                            }
                        }
                    }
                }

                try (OutputStream fileOutputStream = new FileOutputStream(currentEntryAsFile)) {
                    try (InputStream jarEntryInputStream = fileAsJarArchive.getInputStream(currentEntry)) {
                        jarEntryInputStream.transferTo(fileOutputStream);
                    }
                }
            }
        }
    }

    /**
     * Recursive method for deleting a folder.
     */
    private void deleteFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                deleteFolder(file);
            }
            file.delete();
        }
    }

}
