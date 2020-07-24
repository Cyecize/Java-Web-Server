package com.cyecize.summer.areas.template.exceptions;

public class EmptyViewException extends Exception {

    public EmptyViewException(String message) {
        super(message);
    }

    public EmptyViewException(String message, Throwable cause) {
        super(message, cause);
    }
}
