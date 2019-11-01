package com.cyecize.summer.areas.scanning.services;

import com.cyecize.http.HttpSession;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.common.extensions.SessionScopeFactory;

import java.util.Hashtable;

public class DefaultSessionFactory implements SessionScopeFactory<Object> {

    private final Hashtable<String, Object> instancesBySession;

    public DefaultSessionFactory() {
        this.instancesBySession = new Hashtable<>();
    }

    @Override
    public Object getInstance(Class<?> serviceType, HttpSoletRequest request, DependencyContainer dependencyContainer) {
        final HttpSession session = request.getSession();

        if (this.instancesBySession.containsKey(session.getId())) {
            return this.instancesBySession.get(session.getId());
        }

        final Object instance = dependencyContainer.getNewInstance(serviceType);

        this.instancesBySession.put(session.getId(), instance);
        return instance;
    }
}
