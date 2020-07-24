package com.cyecize.summer.areas.routing.exceptions;

public class UnsatisfiedRequestParamException extends RuntimeException{

    private final String paramName;

    public UnsatisfiedRequestParamException(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return this.paramName;
    }
}
