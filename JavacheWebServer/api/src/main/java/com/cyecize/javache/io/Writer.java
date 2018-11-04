package com.cyecize.javache.io;

import java.io.*;

public final class Writer {
    public Writer() {}

    public void writeBytes(byte[] byteData, OutputStream outputStream) throws IOException {
        DataOutputStream buffer = new DataOutputStream(outputStream);
        buffer.write(byteData);
    }
}
