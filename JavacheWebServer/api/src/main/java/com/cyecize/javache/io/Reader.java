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

        byte[] buffer = new byte[4096];

        int read;
        int remaining = buffer.length + 1;
        while ((read = inputStream.read(buffer, 0, Math.min(buffer.length, remaining))) != -1) {
            outputStream.write(buffer, 0, read);
            if (inputStream.available() <= 0) {
                break;
            }

            long time = System.nanoTime() + 50000;
            while (time > System.nanoTime()) {
                //stupid await
            }
        }

        return outputStream.toByteArray();
    }
}
