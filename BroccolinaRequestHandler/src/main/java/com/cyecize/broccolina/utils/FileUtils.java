package com.cyecize.broccolina.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class FileUtils {

    public static boolean filesMatch(InputStream firstFile, InputStream secondFile) throws IOException {
        final byte[] buffer1 = new byte[4096];
        final byte[] buffer2 = new byte[4096];

        if (firstFile.available() != secondFile.available()) {
            return false;
        }

        while (firstFile.available() > 0) {
            firstFile.read(buffer1);
            secondFile.read(buffer2);

            if (Arrays.compare(buffer1, buffer2) != 0) {
                return false;
            }
        }

        return true;
    }
}
