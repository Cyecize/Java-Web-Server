package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.security.interceptors.SecurityInterceptor;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.security.models.SecurityConfig;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.extensions.InterceptorAdapter;
import com.cyecize.summer.common.models.Model;

import java.util.Set;

public class InterceptorInvokerServiceImpl implements InterceptorInvokerService {

    private final Set<Object> interceptors;

    private final DependencyContainer dependencyContainer;

    public InterceptorInvokerServiceImpl(Set<Object> interceptors, DependencyContainer dependencyContainer) {
        this.interceptors = interceptors;
        this.dependencyContainer = dependencyContainer;
        this.addPlatformInterceptors();
    }

    @Override
    public boolean preHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler, boolean reload) throws Exception {
        for (Object interceptor : this.interceptors) {
            Object instanceOfInterceptor = interceptor;
            if (reload && interceptor.getClass().getAnnotation(Component.class).lifespan() == ServiceLifeSpan.REQUEST) {
                instanceOfInterceptor = this.dependencyContainer.reloadComponent(interceptor);
            }
            InterceptorAdapter interceptorAdapter = (InterceptorAdapter) instanceOfInterceptor;
            if (!interceptorAdapter.preHandle(request, response, handler)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler, Model model) throws Exception {
        for (Object interceptor : this.interceptors) {
            ((InterceptorAdapter) interceptor).postHandle(request, response, handler, model);
        }
    }

    /**
     * Add interceptors here such as the Security interceptor
     */
    private void addPlatformInterceptors() {
        this.interceptors.add(new SecurityInterceptor(dependencyContainer.getObject(SecurityConfig.class), dependencyContainer.getObject(Principal.class)));
    }
}
