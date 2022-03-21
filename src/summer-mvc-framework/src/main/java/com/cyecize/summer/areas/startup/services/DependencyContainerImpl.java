package com.cyecize.summer.areas.startup.services;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.common.annotations.Bean;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.constants.IocConstants;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyContainerImpl implements DependencyContainer {

    private final com.cyecize.ioc.services.DependencyContainer dependencyContainer;

    private final Map<ServiceLifeSpan, Collection<ServiceDetails>> cachedServicesByLifespan;

    private final Collection<Object> flashServices;

    public DependencyContainerImpl(com.cyecize.ioc.services.DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
        this.cachedServicesByLifespan = new HashMap<>();
        this.flashServices = new ArrayList<>();
    }

    @Override
    public void reloadServices(ServiceLifeSpan lifeSpan) {
        for (ServiceDetails serviceDetails : this.getServicesByLifeSpan(lifeSpan)) {
            this.reload(serviceDetails);
        }
    }

    @Override
    public void addFlashService(Object service) {
        this.flashServices.add(service);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFlashService(Class<?> serviceType) {
        for (Object flashService : this.flashServices) {
            if (serviceType.isAssignableFrom(flashService.getClass())) {
                return (T) flashService;
            }
        }

        return null;
    }

    @Override
    public void clearFlashServices() {
        this.flashServices.clear();
    }

    @Override
    public Collection<ServiceDetails> getServicesByLifeSpan(ServiceLifeSpan serviceLifeSpan) {
        if (!this.cachedServicesByLifespan.containsKey(serviceLifeSpan)) {
            final Set<ServiceDetails> servicesByAnnotation = new HashSet<>();

            for (Class<? extends Annotation> serviceAnnotation : IocConstants.SERVICE_ANNOTATIONS) {
                servicesByAnnotation.addAll(this.getServicesByAnnotation(serviceAnnotation)
                        .stream()
                        .filter(sd -> this.filterServiceDetails(sd, serviceLifeSpan))
                        .collect(Collectors.toSet()));
            }

            servicesByAnnotation.addAll(this.getServicesByAnnotation(Bean.class).stream()
                    .filter(sd -> this.filterServiceDetails(sd, serviceLifeSpan))
                    .collect(Collectors.toList())
            );

            this.cachedServicesByLifespan.put(serviceLifeSpan, servicesByAnnotation);
        }

        return this.cachedServicesByLifespan.get(serviceLifeSpan);
    }

    private boolean filterServiceDetails(ServiceDetails sd, ServiceLifeSpan serviceLifeSpan) {
        try {
            final Method method = sd.getAnnotation().annotationType()
                    .getDeclaredMethod(IocConstants.SERVICE_ANNOTATION_LIFESPAN_METHOD_NAME);
            method.setAccessible(true);

            final ServiceLifeSpan lifeSpan = (ServiceLifeSpan) method.invoke(sd.getAnnotation());

            return lifeSpan == serviceLifeSpan;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reload(ServiceDetails serviceDetails) {
        this.dependencyContainer.reload(serviceDetails);
    }

    @Override
    public void reload(Class<?> serviceType) {
        this.dependencyContainer.reload(serviceType);
    }

    @Override
    public void update(Object service) {
        this.dependencyContainer.update(service);
    }

    @Override
    public void update(Class<?> serviceType, Object serviceInstance) {
        this.dependencyContainer.update(serviceType, serviceInstance);
    }

    @Override
    public void update(Class<?> serviceType, Object serviceInstance, boolean destroyOldInstance) {
        this.dependencyContainer.update(serviceType, serviceInstance, destroyOldInstance);
    }

    @Override
    public <T> T getService(Class<T> serviceType) {
        return this.dependencyContainer.getService(serviceType);
    }

    @Override
    public <T> T getService(Class<?> serviceType, String qualifier) {
        return this.dependencyContainer.getService(serviceType, qualifier);
    }

    @Override
    public <T> T getNewInstance(Class<?> serviceType) {
        return this.dependencyContainer.getNewInstance(serviceType);
    }

    @Override
    public <T> T getNewInstance(Class<?> serviceType, String qualifier) {
        return this.dependencyContainer.getNewInstance(serviceType, qualifier);
    }

    @Override
    public ServiceDetails getServiceDetails(Class<?> serviceType) {
        return this.dependencyContainer.getServiceDetails(serviceType);
    }

    @Override
    public ServiceDetails getServiceDetails(Class<?> serviceType, String qualifier) {
        return this.dependencyContainer.getServiceDetails(serviceType, qualifier);
    }

    @Override
    public Collection<Class<?>> getAllScannedClasses() {
        return this.dependencyContainer.getAllScannedClasses();
    }

    @Override
    public Collection<ServiceDetails> getImplementations(Class<?> serviceType) {
        return this.dependencyContainer.getImplementations(serviceType);
    }

    @Override
    public Collection<ServiceDetails> getServicesByAnnotation(Class<? extends Annotation> annotationType) {
        return this.dependencyContainer.getServicesByAnnotation(annotationType);
    }

    @Override
    public Collection<ServiceDetails> getAllServices() {
        return this.dependencyContainer.getAllServices();
    }
}
