package com.cyecize.javache.io;

import java.io.*;

public final class Reader {
    public Reader() {
    }

    public String readAllLines(InputStream inputStream) throws IOException {

        StringBuilder result = new StringBuilder();

        do {
            int val = inputStream.read();
            if (val > -1) {
                result.append((char) val);
            }
        } while (inputStream.available() > 0);

        return result.toString();
    }
}
