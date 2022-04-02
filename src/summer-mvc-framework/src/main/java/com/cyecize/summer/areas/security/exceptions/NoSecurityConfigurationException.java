package com.cyecize.summer.areas.security.exceptions;

public class NoSecurityConfigurationException extends RuntimeException {

    public NoSecurityConfigurationException(String message) {
        super(message);
    }

    public NoSecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
