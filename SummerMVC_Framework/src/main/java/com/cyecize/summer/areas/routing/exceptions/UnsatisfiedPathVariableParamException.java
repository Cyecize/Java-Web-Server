package com.cyecize.summer.areas.routing.exceptions;

import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.common.annotations.routing.PathVariable;

public class UnsatisfiedPathVariableParamException extends RuntimeException {

    private final ActionMethod actionMethod;

    private final PathVariable pathVariable;

    public UnsatisfiedPathVariableParamException(ActionMethod actionMethod, PathVariable pathVariable) {
        this.actionMethod = actionMethod;
        this.pathVariable = pathVariable;
    }

    public ActionMethod getActionMethod() {
        return this.actionMethod;
    }

    public PathVariable getPathVariable() {
        return this.pathVariable;
    }
}
