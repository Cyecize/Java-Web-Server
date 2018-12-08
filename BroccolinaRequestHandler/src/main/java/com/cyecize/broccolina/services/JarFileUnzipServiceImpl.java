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

            String currentEntryCanonicalPath = jarFolder.getCanonicalPath() + File.separator + currentEntry.getName();
            File currentEntryAsFile = new File(currentEntryCanonicalPath);

            if (currentEntry.isDirectory()) {
                currentEntryAsFile.mkdir();
                continue;
            }

            InputStream in = new BufferedInputStream(fileAsJarArchive.getInputStream(currentEntry));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(currentEntryAsFile));
            byte[] buffer = new byte[2048];
            while (true) {
                int nBytes = in.read(buffer);
                if (nBytes <= 0) {
                    break;
                }
                out.write(buffer, 0, nBytes);
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
