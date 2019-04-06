package com.cyecize.summer.areas.routing.models;

import com.cyecize.summer.common.annotations.routing.ExceptionListener;

import java.lang.reflect.Method;

public class ActionMethod implements Comparable<ActionMethod> {

    private final String pattern;

    private final String baseRoute;

    private final Method method;

    private final String contentType;

    private final Class<?> controllerClass;

    public ActionMethod(String pattern, String baseRoute, Method method, String contentType, Class<?> controllerClass) {
        this.pattern = pattern;
        this.baseRoute = baseRoute;
        this.method = method;
        this.contentType = contentType;
        this.controllerClass = controllerClass;
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getBaseRoute() {
        return this.baseRoute;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getControllerClass() {
        return this.controllerClass;
    }

    @Override
    public int compareTo(ActionMethod actionMethod) {
        if (!this.getMethod().isAnnotationPresent(ExceptionListener.class) || !actionMethod.getMethod().isAnnotationPresent(ExceptionListener.class)) {
            return 0;
        }
        Class<?> c1 = this.getMethod().getAnnotation(ExceptionListener.class).value();
        Class<?> c2 = actionMethod.getMethod().getAnnotation(ExceptionListener.class).value();
        if (c1.isAssignableFrom(c2)) {
            return 1;
        }
        if (c2.isAssignableFrom(c1)) {
            return -1;
        }
        return 0;
    }
}
