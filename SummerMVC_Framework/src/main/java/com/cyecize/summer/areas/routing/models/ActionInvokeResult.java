package com.cyecize.summer.areas.routing.models;

public class ActionInvokeResult {

    private Object invocationResult;

    public ActionInvokeResult(Object invocationResult) {
        this.invocationResult = invocationResult;
    }

    public Object getInvocationResult() {
        return invocationResult;
    }

    public void setInvocationResult(Object invocationResult) {
        this.invocationResult = invocationResult;
    }
}
