package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.common.extensions.InterceptorAdapter;
import com.cyecize.summer.common.models.Model;

import java.util.Collection;
import java.util.stream.Collectors;

public class InterceptorInvokerServiceImpl implements InterceptorInvokerService {

    private Collection<InterceptorAdapter> interceptors;

    public InterceptorInvokerServiceImpl(DependencyContainer dependencyContainer) {
        this.interceptors = dependencyContainer.getImplementations(InterceptorAdapter.class)
                .stream()
                .map(sd -> (InterceptorAdapter) sd.getInstance())
                .collect(Collectors.toList());
    }

    @Override
    public boolean preHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler) throws Exception {

        for (InterceptorAdapter interceptor : this.interceptors) {
            if (!interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler, Model model) throws Exception {
        for (InterceptorAdapter interceptor : this.interceptors) {
            interceptor.postHandle(request, response, handler, model);
        }
    }
}
