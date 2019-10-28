package com.cyecize.toyote.exceptions;

public class CannotParseRequestException extends RuntimeException {
    public CannotParseRequestException(String message) {
        super(message);
    }

    public CannotParseRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
