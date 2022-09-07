package com.cyecize.solet;

import com.cyecize.http.HttpResponse;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Wrapper class for the client output stream.
 * Using this class, the user can directly write to the output stream, given by the TCP
 * connection coming directly from the client.
 * <p>
 * This is very useful for cases where a large data has to be transferred and using byte arrays
 * could cause {@link OutOfMemoryError}.
 */
public class SoletOutputStream {
    private final OutputStream clientOutputStream;

    private final HttpResponse response;

    private boolean isDirty = false;

    SoletOutputStream(OutputStream clientOutputStream,
                      HttpResponse response) {
        this.clientOutputStream = clientOutputStream;
        this.response = response;
    }

    public void write() throws IOException {
        this.loadMetadata();
    }

    public void write(int b) throws IOException {
        this.loadMetadata();

        this.clientOutputStream.write(b);
    }

    public void write(byte[] b) throws IOException {
        this.loadMetadata();
        this.clientOutputStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.loadMetadata();
        this.clientOutputStream.write(b, off, len);
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    /**
     * Metadata (Status line, headers and CRLF) must be written before anything else.
     * Uses the flag isDirty to ensure data is written only once.
     *
     * @throws IOException -
     */
    private void loadMetadata() throws IOException {
        if (this.isDirty) {
            return;
        }

        this.clientOutputStream.write(this.response.getBytes());
        this.isDirty = true;
    }
}
