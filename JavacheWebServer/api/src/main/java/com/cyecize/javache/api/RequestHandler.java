package com.cyecize.javache.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface that Javache web server looks for when scanning for request handlers.
 */
public interface RequestHandler {

    /**
     * Called once from Javache Web Server
     */
    void init();

    /**
     * @param inputStream    request stream.
     * @param responseStream response stream.
     * @param sharedData     POJO which lives for the duration of the request and can be used
     *                       to store and share data between request handlers.
     * @return if the request handler has intercepted.
     */
    boolean handleRequest(InputStream inputStream, OutputStream responseStream, RequestHandlerSharedData sharedData) throws IOException;

    /**
     * The response of this method can be configured for some request handlers.
     *
     * @return the order of the request handler (lower is first)
     */
    int order();
}