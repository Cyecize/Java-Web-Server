package com.cyecize.solet.service;

import com.cyecize.solet.MemoryFile;
import org.apache.commons.fileupload.FileItemStream;

import java.io.IOException;

public interface TemporaryStorageService {

    MemoryFile saveMultipartFile(FileItemStream fileItemStream) throws IOException;

    void removeTemporaryFiles();
}
