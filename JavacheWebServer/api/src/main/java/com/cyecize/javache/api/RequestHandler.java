package com.cyecize.javache.api;

import com.cyecize.javache.services.JavacheConfigService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface that Javache web server looks for when scanning for request handlers.
 */
public interface RequestHandler {
    void handleRequest(byte[] requestContent, OutputStream responseStream, JavacheConfigService config) throws IOException;

    boolean hasIntercepted();
}