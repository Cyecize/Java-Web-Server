package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.common.models.Model;

public interface InterceptorInvokerService {

    boolean preHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler) throws Exception;

    void postHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler, Model model) throws Exception;
}
