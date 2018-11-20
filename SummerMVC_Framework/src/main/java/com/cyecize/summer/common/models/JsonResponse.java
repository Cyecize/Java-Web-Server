package com.cyecize.summer.common.models;

import com.cyecize.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class JsonResponse {

    private Map<String, Object> response;

    private HttpStatus status;

    public JsonResponse() {
        this.response = new HashMap<>();
        this.status = HttpStatus.OK;
    }

    public JsonResponse(HttpStatus status) {
        this();
        this.status = status;
    }

    public JsonResponse(HttpStatus status, Map<String, Object> response) {
        this.status = status;
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
