package com.cyecize.summer.areas.scanning.services;

import com.cyecize.ioc.exceptions.AlreadyInitializedException;
import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.ioc.services.ObjectInstantiationService;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.constants.IocConstants;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
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
        for (ServiceDetails serviceDetails : this.getOrCacheServicesByLifeSpan(lifeSpan)) {
            this.reload(serviceDetails);
        }
    }

    private Collection<ServiceDetails> getOrCacheServicesByLifeSpan(ServiceLifeSpan serviceLifeSpan) {
        if (!this.cachedServicesByLifespan.containsKey(serviceLifeSpan)) {
            final Set<ServiceDetails> servicesByAnnotation = new HashSet<>();

            for (Class<? extends Annotation> serviceAnnotation : IocConstants.SERVICE_ANNOTATIONS) {
                servicesByAnnotation.addAll(this.getServicesByAnnotation(serviceAnnotation).stream().filter(sd -> {
                    try {
                        final Method method = sd.getAnnotation().annotationType().getDeclaredMethod(IocConstants.SERVICE_ANNOTATION_LIFESPAN_METHOD_NAME);
                        method.setAccessible(true);

                        final ServiceLifeSpan lifeSpan = (ServiceLifeSpan) method.invoke(sd.getAnnotation());

                        return lifeSpan == serviceLifeSpan;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toSet()));
            }

            this.cachedServicesByLifespan.put(serviceLifeSpan, servicesByAnnotation);
        }

        return this.cachedServicesByLifespan.get(serviceLifeSpan);
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
    public void init(Collection<Class<?>> collection, Collection<ServiceDetails> collection1, ObjectInstantiationService objectInstantiationService) throws AlreadyInitializedException {

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
    public <T> T getService(Class<T> serviceType) {
        return this.dependencyContainer.getService(serviceType);
    }

    @Override
    public ServiceDetails getServiceDetails(Class<?> serviceType) {
        return this.dependencyContainer.getServiceDetails(serviceType);
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
