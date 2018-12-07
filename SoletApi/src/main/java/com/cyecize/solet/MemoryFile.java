package com.cyecize.solet;

import java.io.InputStream;

public interface MemoryFile {

    long getFileLength();

    String getContentType();

    String getFileName();

    String getFieldName();

    String getFilePath();

    InputStream getInputStream();

    byte[] getBytes();

}
