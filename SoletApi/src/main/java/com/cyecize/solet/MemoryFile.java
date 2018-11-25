package com.cyecize.solet;

import java.io.InputStream;

public interface MemoryFile {

    String getContentType();

    String getFileName();

    String getFieldName();

    InputStream getInputStream();

    byte[] getBytes();

}
