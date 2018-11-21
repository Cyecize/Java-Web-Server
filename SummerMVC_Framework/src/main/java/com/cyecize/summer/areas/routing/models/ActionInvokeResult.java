package com.cyecize.summer.areas.routing.models;

public class ActionInvokeResult {

    private Object invocationResult;

    private String contentType;

    public ActionInvokeResult(Object invocationResult, String contentType) {
        this.invocationResult = invocationResult;
        this.contentType = contentType;
    }

    public Object getInvocationResult() {
        return invocationResult;
    }

    public String getContentType() {
        return contentType;
    }
}
