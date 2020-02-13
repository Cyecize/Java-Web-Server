package com.cyecize.javache.services;

/**
 * Specification of the Javache Logging Service.
 * The implementation can be found in Javache Web Server.
 */
public interface LoggingService {

    void info(Object msg, Object... params);

    void warning(Object msg, Object... params);

    void error(Object msg, Object... params);

    void printStackTrace(Throwable throwable);
}
