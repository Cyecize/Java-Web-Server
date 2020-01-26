package com.cyecize.summer.areas.routing.models;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;

import java.lang.reflect.Method;

public class ActionMethod implements Comparable<ActionMethod> {

    private final String pattern;

    private final String baseRoute;

    private final Method method;

    private final String contentType;

    private final ServiceDetails controller;

    public ActionMethod(String pattern, String baseRoute, Method method, String contentType, ServiceDetails controller) {
        this.pattern = pattern;
        this.baseRoute = baseRoute;
        this.method = method;
        this.contentType = contentType;
        this.controller = controller;
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

    public ServiceDetails getController() {
        return this.controller;
    }

    @Override
    public int compareTo(ActionMethod actionMethod) {
        if (!this.getMethod().isAnnotationPresent(ExceptionListener.class) ||
                !actionMethod.getMethod().isAnnotationPresent(ExceptionListener.class)) {
            return 0;
        }

        final Class<?> c1 = this.getMethod().getAnnotation(ExceptionListener.class).value();
        final Class<?> c2 = actionMethod.getMethod().getAnnotation(ExceptionListener.class).value();

        if (c1.isAssignableFrom(c2)) {
            return 1;
        }

        if (c2.isAssignableFrom(c1)) {
            return -1;
        }

        return 0;
    }
}
