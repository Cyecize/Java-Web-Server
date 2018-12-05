package com.cyecize.javache.io;

import com.cyecize.javache.exceptions.RequestReadException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Reader {

    private static final int TIMEOUT = 10;

    private static final int BIG_REQUEST_THRESHOLD = 1024;

    private static final int GIANT_REQUEST_THRESHOLD = 1024 * 10;

    private static final String REQUEST_TOO_LARGE_FORMAT = "Request length maximum allowed is %d.";

    private int totalRead;

    private int maxSize;

    public Reader() {

    }

    public String readAllLines(InputStream inputStream) throws IOException {
        return new String(this.readAllBytes(inputStream), StandardCharsets.UTF_8);
    }

    public byte[] readAllBytes(InputStream inputStream) throws IOException {
        return this.readAllBytes(inputStream, Integer.MAX_VALUE);
    }

    public byte[] readAllBytes(InputStream inputStream, int maxSize) throws IOException {
        this.maxSize = maxSize;
        this.totalRead = 0;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(inputStream.read());

        int available = inputStream.available();
        System.out.println("available " + available);

        while (true) {
            this.read(available, inputStream, outputStream);
            available = inputStream.available();

            System.out.println("available " + available + ", read " + this.totalRead);

            if (available <= 0) {

                int contentLength = this.tryGetContentLength(outputStream.toByteArray());
                if (contentLength > 0) {
                    System.out.println("content length present " + contentLength);
                    this.readByContentLength(contentLength, inputStream, outputStream);
                    break;
                }

                if (this.totalRead >= BIG_REQUEST_THRESHOLD) {
                    this.readBigData(inputStream, outputStream);
                    break;
                }

                break;
            }
        }

        return outputStream.toByteArray();
    }

    private void read(int length, InputStream inputStream, OutputStream outputStream) throws IOException {
        outputStream.write(inputStream.readNBytes(length));
        this.totalRead += length;
        if (length > this.maxSize) {
            throw new RequestReadException(String.format(REQUEST_TOO_LARGE_FORMAT, this.maxSize));
        }
    }

    private int tryGetContentLength(byte[] bytes) {
        Pattern pattern = Pattern.compile("Content-Length:\\s+(?<length>[0-9]+)");
        String s = new String(bytes);

        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group("length"));
            } catch (Exception ignored) {
            }
        }

        return -1;
    }

    private void readByContentLength(int length, InputStream inputStream, OutputStream outputStream) throws IOException {
        while (this.totalRead < length) {
            int available = inputStream.available() < 0 ? 1 : inputStream.available();
            this.read(available, inputStream, outputStream);
        }
    }

    private void readBigData(InputStream inputStream, OutputStream outputStream) throws IOException {
        System.out.println("big data");
        while (true) {
            int available = this.getAvailableAwait(inputStream, 1500); //1.5s
            System.out.println("big data available " + available);
            if (available <= 0) {
                return;
            }
            this.read(available, inputStream, outputStream);

            if (this.totalRead > GIANT_REQUEST_THRESHOLD) {
                this.readGiantData(inputStream, outputStream);
                return;
            }
        }
    }

    private void readGiantData(InputStream inputStream, OutputStream outputStream) throws IOException {
        System.out.println("Giant data");
        while (true) {
            int available = this.getAvailableAwait(inputStream, 4000); //4s
            System.out.println("giant data available " + available);
            if (available <= 0) {
                return;
            }
            this.read(available, inputStream, outputStream);

            if (this.totalRead > GIANT_REQUEST_THRESHOLD) {
                this.readGiantData(inputStream, outputStream);
                return;
            }
        }
    }

    private int getAvailableAwait(InputStream inputStream, int awaitMisslis) throws IOException {
        long timeout = System.currentTimeMillis() + awaitMisslis;
        while (timeout > System.currentTimeMillis()) {
            if (inputStream.available() > 0) {
                return inputStream.available();
            }
        }
        return 0;
    }
}