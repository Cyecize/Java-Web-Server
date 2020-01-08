package com.cyecize.summer.areas.scanning.services;

import com.cyecize.ioc.models.ServiceBeanDetails;
import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.common.annotations.SessionFactory;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.extensions.SessionScopeFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SessionScopeManagerImpl implements SessionScopeManager {

    private final Map<ServiceDetails, SessionScopeFactory> scopeFactoryByServiceDetails;

    private DependencyContainer dependencyContainer;

    public SessionScopeManagerImpl() {
        this.scopeFactoryByServiceDetails = new HashMap<>();
    }

    @Override
    public void initialize(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
        final Collection<ServiceDetails> sessionScopedServices = this.dependencyContainer.getServicesByLifeSpan(ServiceLifeSpan.SESSION);

        for (ServiceDetails serviceDetails : sessionScopedServices) {
            this.scopeFactoryByServiceDetails.put(serviceDetails, this.getSessionFactory(serviceDetails));
        }
    }

    @Override
    public void setSessionScopedServices(HttpSoletRequest request) {

        for (Map.Entry<ServiceDetails, SessionScopeFactory> kvp : this.scopeFactoryByServiceDetails.entrySet()) {
            final SessionScopeFactory<?> sessionScopeFactory = kvp.getValue();
            final ServiceDetails serviceDetails = kvp.getKey();

            serviceDetails.setInstance(sessionScopeFactory.getInstance(
                    serviceDetails,
                    request,
                    this.dependencyContainer
            ));
        }
    }

    private SessionScopeFactory getSessionFactory(ServiceDetails serviceDetails) {
        SessionScopeFactory sessionScopeFactory = null;

        if (serviceDetails instanceof ServiceBeanDetails) {
            final Method originMethod = ((ServiceBeanDetails) serviceDetails).getOriginMethod();
            if (originMethod.isAnnotationPresent(SessionFactory.class)) {
                sessionScopeFactory = this.dependencyContainer.getService(originMethod.getAnnotation(SessionFactory.class).value());
            }
        } else if (serviceDetails.getServiceType().isAnnotationPresent(SessionFactory.class)) {
            final SessionFactory sessionFactory = serviceDetails.getServiceType().getAnnotation(SessionFactory.class);
            sessionScopeFactory = this.dependencyContainer.getService(sessionFactory.value());
        }

        if (sessionScopeFactory == null) {
            sessionScopeFactory = new DefaultSessionFactory();
        }

        return sessionScopeFactory;
    }
}
