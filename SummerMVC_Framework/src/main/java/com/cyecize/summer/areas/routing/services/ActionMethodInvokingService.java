package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.routing.exceptions.HttpNotFoundException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;

public interface ActionMethodInvokingService {

    ActionMethod findAction(HttpSoletRequest request) throws HttpNotFoundException;

    ActionInvokeResult invokeMethod(ActionMethod method);

    ActionInvokeResult invokeMethod(Exception ex);
}
