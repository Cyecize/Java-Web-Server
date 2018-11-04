package com.cyecize.javache.io;

import java.io.*;

public final class Reader {
    public Reader() {
    }

    public String readAllLines(InputStream inputStream) throws IOException {

        StringBuilder result = new StringBuilder();

        do {
            result.append((char) inputStream.read());
        } while (inputStream.available() > 0);

        return result.toString();
    }
}
