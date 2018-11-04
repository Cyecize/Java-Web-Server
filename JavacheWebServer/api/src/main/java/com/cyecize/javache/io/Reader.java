package com.cyecize.javache.io;

import java.io.*;

public final class Reader {
    public Reader() {}

    public String readAllLines(InputStream inputStream) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder result = new StringBuilder();
        while(buffer.ready()) {
            result.append((char)buffer.read());
        }

        return result.toString();
    }
}
