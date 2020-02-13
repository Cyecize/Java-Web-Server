package com.cyecize.javache.api;

/**
 * All implementations of this interface will be called after each request.
 */
public interface RequestDestroyHandler {

    void destroy(RequestHandlerSharedData sharedData);
}
