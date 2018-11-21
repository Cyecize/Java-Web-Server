package com.cyecize.summer.areas.routing.models;

import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.GetMapping;
import com.cyecize.summer.common.annotations.routing.PostMapping;

import java.lang.reflect.Method;

public class ActionMethod {

    private String pattern;

    private Method method;

    private String contentType;

    private Class<?> controllerClass;

    public ActionMethod(String pattern, Method method, Class<?> controllerClass) {
        this.pattern = pattern;
        this.method = method;
        this.controllerClass = controllerClass;
        this.extractContentType();
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Method getMethod() {
        return this.method;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    private void extractContentType() {
        if (this.method.isAnnotationPresent(GetMapping.class)) {
            this.contentType = this.method.getAnnotation(GetMapping.class).produces();
        } else if (this.method.isAnnotationPresent(PostMapping.class)) {
            this.contentType = this.method.getAnnotation(PostMapping.class).produces();
        } else if (this.getMethod().isAnnotationPresent(ExceptionListener.class)) {
            this.contentType = this.method.getAnnotation(ExceptionListener.class).produces();
        }
    }
}
