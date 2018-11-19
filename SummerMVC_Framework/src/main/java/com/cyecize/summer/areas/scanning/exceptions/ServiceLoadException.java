package com.cyecize.summer.areas.scanning.exceptions;

public class ServiceLoadException extends Exception {
    public ServiceLoadException(String message) {
        super(message);
    }

    public ServiceLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
