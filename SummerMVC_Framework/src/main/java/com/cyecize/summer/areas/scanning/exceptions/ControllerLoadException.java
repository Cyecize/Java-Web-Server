package com.cyecize.summer.areas.scanning.exceptions;

public class ControllerLoadException extends Exception {

    public ControllerLoadException(String message) {
        super(message);
    }

    public ControllerLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
