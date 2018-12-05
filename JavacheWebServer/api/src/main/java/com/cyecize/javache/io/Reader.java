package com.cyecize.javache.io;

import com.cyecize.javache.exceptions.RequestReadException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class Reader {

    private static final String REQUEST_TOO_LARGE_FORMAT = "Request length maximum allowed is %d.";

    public Reader() {
    }

    public String readAllLines(InputStream inputStream) throws IOException {
        return new String(this.readAllBytes(inputStream), StandardCharsets.UTF_8);
    }

    public byte[] readAllBytes(InputStream inputStream, int maxSize) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];

        int read;
        int total = 0;
        int remaining = buffer.length + 1;

        while ((read = inputStream.read(buffer, 0, Math.min(buffer.length, remaining))) != -1) {
            outputStream.write(buffer, 0, read);
            total += read;

            if (total > maxSize) {
                throw new RequestReadException(String.format(REQUEST_TOO_LARGE_FORMAT, maxSize));
            }

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

    public byte[] readAllBytes(InputStream inputStream) throws IOException {
        return this.readAllBytes(inputStream, Integer.MAX_VALUE);
    }
}
