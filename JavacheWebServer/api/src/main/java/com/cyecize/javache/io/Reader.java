package com.cyecize.javache.io;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class Reader {
    public Reader() {
    }

    public String readAllLines(InputStream inputStream) throws IOException {
        return new String(this.readAllBytes(inputStream), StandardCharsets.UTF_8);
    }

    public byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        do {
            int b = inputStream.read();
            outputStream.write(b);
        } while (inputStream.available() > 0);

        return outputStream.toByteArray();
    }
}
