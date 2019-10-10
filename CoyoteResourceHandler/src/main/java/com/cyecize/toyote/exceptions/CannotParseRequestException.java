package com.cyecize.toyote.exceptions;

import java.io.IOException;

public class CannotParseRequestException extends IOException {
    public CannotParseRequestException(String message) {
        super(message);
    }

    public CannotParseRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
