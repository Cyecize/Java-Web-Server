package com.cyecize.broccolina.services;

import com.cyecize.javache.services.LoggingService;
import com.cyecize.solet.SoletLogger;

public class SoletLoggerImpl implements SoletLogger {

    private final LoggingService loggingService;

    private final String appName;

    public SoletLoggerImpl(LoggingService loggingService, String appName) {
        this.loggingService = loggingService;
        this.appName = appName;
    }

    @Override
    public void info(Object msg, Object... params) {
        this.loggingService.info(this.getFormattedMsg(msg, params));
    }

    @Override
    public void warning(Object msg, Object... params) {
        this.loggingService.warning(this.getFormattedMsg(msg, params));
    }

    @Override
    public void error(Object msg, Object... params) {
        this.loggingService.error(this.getFormattedMsg(msg, params));
    }

    private String getFormattedMsg(Object msg, Object... params) {
        return String.format("[%s] ", appName) +
                String.format(msg + "", params);
    }

    @Override
    public void printStackTrace(Throwable throwable) {
        this.loggingService.printStackTrace(throwable);
    }
}
