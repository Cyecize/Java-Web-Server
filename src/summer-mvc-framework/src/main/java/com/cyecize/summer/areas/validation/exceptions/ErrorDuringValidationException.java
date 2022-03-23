package com.cyecize.summer.areas.validation.exceptions;

public class ErrorDuringValidationException extends RuntimeException {

    public ErrorDuringValidationException(String message) {
        super(message);
    }

    public ErrorDuringValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
