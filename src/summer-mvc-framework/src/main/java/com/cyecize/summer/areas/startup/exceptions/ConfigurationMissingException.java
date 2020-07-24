package com.cyecize.summer.areas.startup.exceptions;

public class ConfigurationMissingException extends RuntimeException {

    public ConfigurationMissingException(String message) {
        super(message);
    }

    public ConfigurationMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
