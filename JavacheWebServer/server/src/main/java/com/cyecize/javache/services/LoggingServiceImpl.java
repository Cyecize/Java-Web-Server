package com.cyecize.javache.services;

import com.cyecize.ioc.annotations.Service;

@Service
public class LoggingServiceImpl implements LoggingService {

    public LoggingServiceImpl(){

    }

    @Override
    public void info(Object msg, Object... params) {
        this.print("INFO", msg, params);
    }

    @Override
    public void warning(Object msg, Object... params) {
        this.print("WARNING", msg, params);
    }

    @Override
    public void error(Object msg, Object... params) {
        this.print("ERROR", msg, params);
    }

    @Override
    public void printStackTrace(StackTraceElement[] stackTraces) {
        for (StackTraceElement stackTrace : stackTraces) {
            this.error(stackTrace.toString());
        }
    }

    private void print(String type, Object msg, Object... params){
        System.out.println(String.format("[%s] %s", type, String.format(msg + "", params)));
    }
}
