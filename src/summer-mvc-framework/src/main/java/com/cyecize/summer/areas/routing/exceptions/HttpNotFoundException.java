package com.cyecize.summer.areas.routing.exceptions;

public class HttpNotFoundException extends Exception {

    public HttpNotFoundException(String message) {
        super(message);
    }

    public HttpNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
