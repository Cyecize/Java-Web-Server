package com.cyecize.javache.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface that Javache web server looks for when scanning for request handlers.
 */
public interface RequestHandler {
    void init();

    boolean handleRequest(InputStream inputStream, OutputStream responseStream, RequestHandlerSharedData sharedData) throws IOException;

    int order();
}