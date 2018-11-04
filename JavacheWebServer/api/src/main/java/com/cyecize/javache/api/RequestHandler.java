package com.cyecize.javache.api;

import java.io.InputStream;
import java.io.OutputStream;

public interface RequestHandler {
    void handleRequest(InputStream requestStream, OutputStream responseStream);

    boolean hasIntercepted();
}