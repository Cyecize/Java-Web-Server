package com.cyecize.solet;

import java.io.InputStream;

public interface MemoryFile {

    String getFileName();

    String getFieldName();

    InputStream getInputStream();

    byte[] getBytes();

}
