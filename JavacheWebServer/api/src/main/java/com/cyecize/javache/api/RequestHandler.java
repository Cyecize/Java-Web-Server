package com.cyecize.javache.api;

import com.cyecize.javache.services.JavacheConfigService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface RequestHandler {
    void handleRequest(byte[] requestContent, OutputStream responseStream, JavacheConfigService config) throws IOException;

    boolean hasIntercepted();
}