package com.cyecize.summer.areas.routing.models;

public class ActionInvokeResult {

    private final ActionMethod actionMethod;

    private final Object invocationResult;

    private final String contentType;

    public ActionInvokeResult(ActionMethod actionMethod, Object invocationResult, String contentType) {
        this.actionMethod = actionMethod;
        this.invocationResult = invocationResult;
        this.contentType = contentType;
    }

    public ActionMethod getActionMethod() {
        return this.actionMethod;
    }

    public Object getInvocationResult() {
        return this.invocationResult;
    }

    public String getContentType() {
        return this.contentType;
    }
}
