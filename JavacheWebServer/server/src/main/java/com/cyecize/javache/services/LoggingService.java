package com.cyecize.javache.services;

public interface LoggingService {

    void info(Object msg, Object... params);

    void warning(Object msg, Object... params);

    void error(Object msg, Object... params);

    void printStackTrace(StackTraceElement[] stackTraces);
}
