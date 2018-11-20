package com.cyecize.summer.areas.routing.models;

import java.lang.reflect.Method;

public class ActionMethod {

    private String pattern;

    private Method method;

    private Class<?> controllerClass;

    public ActionMethod(String pattern, Method method, Class<?> controllerClass) {
        this.pattern = pattern;
        this.method = method;
        this.controllerClass = controllerClass;
    }

    public String getPattern() {
        return this.pattern;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }
}
