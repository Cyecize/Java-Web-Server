package com.cyecize.javache.api;

import java.io.IOException;
import java.io.OutputStream;

public interface RequestHandler {
    void handleRequest(String requestContent, OutputStream responseStream) throws IOException;

    boolean hasIntercepted();
}