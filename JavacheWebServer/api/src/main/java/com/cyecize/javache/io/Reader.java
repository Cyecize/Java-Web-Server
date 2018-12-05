package com.cyecize.javache.io;

import com.cyecize.javache.exceptions.RequestReadException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Reader {

    private static final int BIG_REQUEST_THRESHOLD = 1024;

    private static final int GIANT_REQUEST_THRESHOLD = 1024 * 100;

    private static final String REQUEST_TOO_LARGE_FORMAT = "Request length maximum allowed is %d.";

    private static final Pattern CONTENT_LENGTH_PATTERN = Pattern.compile("Content-Length:\\s+(?<length>[0-9]+)");

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

    /**
     * Reads bytes until there are not bytes left in the input stream.
     * Then checks if the currently read bytes contain info about the content length and
     * if there is, proceeds to read bytes until the length is met.
     * If the total read bytes are over the big data threshold, call bigData handler.
     */
    public byte[] readAllBytes(InputStream inputStream, int maxSize) throws IOException {
        this.maxSize = maxSize;
        this.totalRead = 0;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //Read single byte to activate available() method
        outputStream.write(inputStream.read());
        int available = inputStream.available();

        while (true) {
            this.read(available, inputStream, outputStream);
            available = inputStream.available();
            if (available <= 0) {

                int contentLength = this.tryGetContentLength(outputStream.toByteArray());
                if (contentLength > 0) {
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

    /**
     * Search the currently read bytes for header Content-Length and return its value if present.
     * Return -1 if no header is present.
     */
    private int tryGetContentLength(byte[] bytes) {
        String s = new String(bytes);
        Matcher matcher = CONTENT_LENGTH_PATTERN.matcher(s);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group("length"));
            } catch (Exception ignored) {
            }
        }

        return -1;
    }

    /**
     * Reads the inputStream, knowing the length of the body and the header section.
     */
    private void readByContentLength(int length, InputStream inputStream, ByteArrayOutputStream outputStream) throws IOException {
        int headersLength = this.getContentLengthForHttpRequest(outputStream);
        int totalLength = length + headersLength;

        while (this.totalRead < totalLength) {
            int available = inputStream.available() < 1 ? 1 : inputStream.available();
            this.read(available, inputStream, outputStream);
        }
    }

    /**
     * Scans the currently read request and adds the total length of the content until CRLF.
     * Thus, Content-Length of request headers.
     */
    private int getContentLengthForHttpRequest(ByteArrayOutputStream currentlyReadBytes) throws IOException {
        int length = 0;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(currentlyReadBytes.toByteArray())));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equals("")) {
                return length;
            }
            length += line.length() + 2; //accounting for \r\n
        }
        return length;
    }

    /**
     * Read data handler for inputStream with length over 1024B and no Content-Length header.
     * Adds higher timeout value to better with with slower networks.
     */
    private void readBigData(InputStream inputStream, OutputStream outputStream) throws IOException {
        while (true) {
            int available = this.getAvailableAwait(inputStream, 1500); //1.5s
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

    /**
     * Read data handler for inputStream with length over 1MB and no Content-Length header.
     */
    private void readGiantData(InputStream inputStream, OutputStream outputStream) throws IOException {
        while (true) {
            int available = this.getAvailableAwait(inputStream, 4000); //4s

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

    /**
     * Runs a white loop for a given period and checks if the inputStream has available data.
     */
    private int getAvailableAwait(InputStream inputStream, int awaitMisslis) throws IOException {
        long timeout = System.currentTimeMillis() + awaitMisslis;
        while (timeout > System.currentTimeMillis()) {
            if (inputStream.available() > 0) {
                return inputStream.available();
            }
        }
        return 0;
    }

    /**
     * Read data from inputStream by given length.
     * Check if the total read data is more than the maximum allowed and throw Exception if it is.
     */
    private void read(int length, InputStream inputStream, OutputStream outputStream) throws IOException {
        outputStream.write(inputStream.readNBytes(length));
        this.totalRead += length;
        if (length > this.maxSize) {
            throw new RequestReadException(String.format(REQUEST_TOO_LARGE_FORMAT, this.maxSize));
        }
    }
}