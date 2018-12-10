package com.cyecize.javache.api;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface that Javache web server looks for when scanning for request handlers.
 */
public interface RequestHandler {
    void handleRequest(byte[] requestContent, OutputStream responseStream) throws IOException;

    boolean hasIntercepted();
}