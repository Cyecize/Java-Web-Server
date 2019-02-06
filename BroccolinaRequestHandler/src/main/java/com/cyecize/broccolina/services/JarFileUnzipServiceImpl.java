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
        this.unzipJar(jarFile, true, jarFile.getCanonicalPath().replace(".jar", ""));
    }

    @Override
    public void unzipJar(File jarFile, boolean overwriteExistingFiles, String outputDirectory) throws IOException {
        String rootCanonicalPath = jarFile.getCanonicalPath();

        JarFile fileAsJarArchive = new JarFile(rootCanonicalPath);

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

            InputStream in = new BufferedInputStream(fileAsJarArchive.getInputStream(currentEntry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(currentEntryAsFile));

            if (overwriteExistingFiles || !currentEntryAsFile.exists() || (currentEntryAsFile.exists()) && !FileUtils.filesMatch(in, new FileInputStream(currentEntryAsFile))) {
                in.transferTo(out);
            }

            out.flush();
            out.close();
            in.close();
        }

        fileAsJarArchive.close();
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
