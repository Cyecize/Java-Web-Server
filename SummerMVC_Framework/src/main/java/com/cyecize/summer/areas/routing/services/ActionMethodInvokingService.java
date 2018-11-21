package com.cyecize.summer.areas.routing.services;

import com.cyecize.summer.areas.routing.exceptions.HttpNotFoundException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;


public interface ActionMethodInvokingService {

    ActionInvokeResult invokeMethod() throws HttpNotFoundException;
}
