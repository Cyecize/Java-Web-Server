package com.cyecize.javache.exceptions;

import java.io.IOException;

public class RequestReadException extends IOException {

    public RequestReadException(String message) {
        super(message);
    }

    public RequestReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
