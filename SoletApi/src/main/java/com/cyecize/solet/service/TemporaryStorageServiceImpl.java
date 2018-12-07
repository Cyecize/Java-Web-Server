package com.cyecize.solet.service;

import com.cyecize.solet.MemoryFile;
import com.cyecize.solet.MultipartMemoryFile;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.io.FileDeleteStrategy;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TemporaryStorageServiceImpl implements TemporaryStorageService {

    private final String tempDir;

    private List<String> currentTempFiles;

    public TemporaryStorageServiceImpl(String tempDir) {
        this.tempDir = tempDir;
        this.currentTempFiles = new ArrayList<>();
    }

    @Override
    public MemoryFile saveMultipartFile(FileItemStream fileItemStream) throws IOException {
        InputStream inputStream = fileItemStream.openStream();
        if (inputStream.available() <= 0) {
            return null;
        }

        String fileCanonicalPath = this.tempDir + UUID.randomUUID().toString() + fileItemStream.getName();
        FileOutputStream fos = new FileOutputStream(fileCanonicalPath);

        long transferred = inputStream.transferTo(fos);

        fos.close();
        inputStream.close();

        this.currentTempFiles.add(fileCanonicalPath);

        if (transferred <= 0) {
            return null;
        }
        return new MultipartMemoryFile(fileItemStream.getName(), fileItemStream.getFieldName(), fileCanonicalPath, fileItemStream.getContentType(), transferred);
    }

    @Override
    public void removeTemporaryFiles() {
        Timer timer = new Timer(5000, e -> {
            for (String currentTempFile : this.currentTempFiles) {
                try {
                    FileDeleteStrategy.FORCE.delete(new File(currentTempFile));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}
