package com.cyecize.summer.common.models;

import com.cyecize.http.HttpStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private String viewName;

    private HttpStatus status;

    private Map<String, Object> attributes;

    public ModelAndView() {
        this.attributes = new HashMap<>();
    }

    public ModelAndView(String viewName) {
        this();
        this.viewName = viewName;
    }

    public ModelAndView(String viewName, HttpStatus status) {
        this(viewName);
        this.setStatus(status);
    }

    public ModelAndView(String viewName, Map<String, Object> attributes) {
        this(viewName, HttpStatus.OK);
        this.attributes = attributes;
    }

    public ModelAndView(String viewName, HttpStatus status, Map<String, Object> attributes) {
        this.viewName = viewName;
        this.setStatus(status);
        this.attributes = attributes;
    }

    public void addObject(String name, Object object) {
        this.attributes.put(name, object);
    }

    public void addRange(Map<String, ?> objects) {
        this.attributes.putAll(objects);
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setView(String view) {
        this.viewName = view;
    }

    public String getView() {
        return this.viewName;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(this.attributes);
    }
}
