package com.cyecize.broccolina.services;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileUnzipServiceImpl implements JarFileUnzipService {

    public JarFileUnzipServiceImpl() {

    }

    @Override
    public void unzipJar(File jarFile) throws IOException {
        String rootCanonicalPath = jarFile.getCanonicalPath();

        JarFile fileAsJarArchive = new JarFile(rootCanonicalPath);

        Enumeration<JarEntry> jarEntries = fileAsJarArchive.entries();

        File jarFolder = new File(rootCanonicalPath.replace(".jar", ""));

        if (jarFolder.exists() && jarFolder.isDirectory()) {
            deleteFolder(jarFolder);
        }

        jarFolder.mkdir();

        while (jarEntries.hasMoreElements()) {
            JarEntry currentEntry = jarEntries.nextElement();

            String currentEntryCannonicalPath = jarFolder.getCanonicalPath() + File.separator + currentEntry.getName();
            File currentEntryAsFile = new File(currentEntryCannonicalPath);

            if (currentEntry.isDirectory()) {
                currentEntryAsFile.mkdir();
                continue;
            }

            InputStream currentEntryInputStream = fileAsJarArchive.getInputStream(currentEntry);
            OutputStream currentEntryOutputStream = new FileOutputStream(currentEntryAsFile.getCanonicalPath());

            while (currentEntryInputStream.available() > 0) {
                currentEntryOutputStream.write(currentEntryInputStream.read());
            }

            currentEntryInputStream.close();
            currentEntryOutputStream.close();
        }

        fileAsJarArchive.close();
    }

    private void deleteFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                deleteFolder(file);
            }
            file.delete();
        }
    }

}
