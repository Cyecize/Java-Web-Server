package com.cyecize.javache.io;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class Writer {

    public Writer() {
    }

    /**
     * Writes data in string representation.
     */
    public void writeData(String data, OutputStream outputStream) throws IOException {
        DataOutputStream buffer = new DataOutputStream(outputStream);
        buffer.write(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes data in byte array representation.
     */
    public void writeBytes(byte[] bytes, OutputStream outputStream) throws IOException {
        DataOutputStream buffer = new DataOutputStream(outputStream);
        buffer.write(bytes);
    }
}
