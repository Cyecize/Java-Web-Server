package com.cyecize.summer.areas.routing.exceptions;

public class ViewNotFoundException extends Exception {

    public ViewNotFoundException(String message) {
        super(message);
    }

    public ViewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
