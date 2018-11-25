package com.cyecize.javache.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RequestHandler {
    void handleRequest(InputStream inputStream, OutputStream responseStream) throws IOException;

    boolean hasIntercepted();
}