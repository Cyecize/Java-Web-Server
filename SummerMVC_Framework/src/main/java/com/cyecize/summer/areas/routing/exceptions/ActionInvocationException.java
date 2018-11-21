package com.cyecize.summer.areas.routing.exceptions;

public class ActionInvocationException extends RuntimeException {

    public ActionInvocationException(String message) {
        super(message);
    }

    public ActionInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
