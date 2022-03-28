package com.cyecize.summer.common.models;

import com.cyecize.http.HttpStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class JsonResponse {

    @JsonProperty
    private final Map<String, Object> response;

    private HttpStatus status;

    public JsonResponse() {
        this.response = new HashMap<>();
    }

    public JsonResponse(HttpStatus status) {
        this();
        this.setStatusCode(status);
    }

    public JsonResponse(HttpStatus status, Map<String, Object> response) {
        this.setStatusCode(status);
        this.response = response;
    }

    public JsonResponse addAttribute(String name, Object attribute) {
        this.response.put(name, attribute);
        return this;
    }

    public Object getAttribute(String name) {
        return this.response.get(name);
    }

    public JsonResponse setStatusCode(HttpStatus statusCode) {
        this.status = statusCode;
        return this;
    }

    public HttpStatus getStatusCode() {
        return this.status;
    }
}
