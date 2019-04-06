package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.annotations.Service;
import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyContainerImpl implements DependencyContainer {

    private Set<Object> allServicesAndBeans;

    private Set<Object> platformBeans;

    private Map<ServiceLifeSpan, List<Class<?>>> cachedClassesByLifeSpan;

    public DependencyContainerImpl() {
        this.allServicesAndBeans = new HashSet<>();
        this.platformBeans = new HashSet<>();
        this.cachedClassesByLifeSpan = new HashMap<>();
    }

    @Override
    public void addServices(Set<Object> services) {
        this.allServicesAndBeans.addAll(services);
    }

    /**
     * Gets classes with the given lifeSpan.
     * Filters out the currently instantiated services that have the given serviceLifeSpan.
     * Iterates the classes that need to be loaded and reloads them.
     */
    @Override
    public void reloadServices(ServiceLifeSpan lifeSpan) {
        List<Class<?>> classesToReload = this.getOrCacheClassesByLifespan(lifeSpan);
        this.allServicesAndBeans = this.allServicesAndBeans.stream()
                .filter((service) -> !this.isServiceToBeReloaded(service.getClass(), lifeSpan))
                .collect(Collectors.toSet());
        try {
            for (Class<?> clsToReload : classesToReload) {
                this.loadService(clsToReload, lifeSpan);
            }
        } catch (ServiceLoadException ex) {
            //this should not be reached since services have been
            // successfully initialized before in the ServiceLoadingService
            ex.printStackTrace();
        }
    }

    @Override
    public void addPlatformBean(Object object) {
        this.platformBeans.add(object);
    }

    @Override
    public void evictPlatformBeans() {
        this.platformBeans = new HashSet<>();
    }

    /**
     * Gets the fist constructor of the given component.
     * Collects all required parameters (if any) and returns new instance of the component.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T reloadComponent(T component) {
        try {
            Class componentClass = component.getClass();
            Constructor<T> constructor = componentClass.getConstructors()[0];
            if (constructor.getParameterCount() < 1) {
                return (T) constructor.newInstance();
            }
            Object[] paramInstances = new Object[constructor.getParameterCount()];
            Class<?>[] paramTypes = constructor.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                paramInstances[i] = this.getObject(paramTypes[i]);
            }
            return (T) constructor.newInstance(paramInstances);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            //should not be reached
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Reloads component only if the lifeSpan if the component is equal
     * to the required lifeSpan.
     */
    @Override
    public <T> T reloadComponent(T component, ServiceLifeSpan lifeSpan) {
        if (component.getClass().getAnnotation(Component.class).lifespan() != lifeSpan) {
            return component;
        }

        return this.reloadComponent(component);
    }

    /**
     * Iterates platform beans to find assignable object for objType parameter.
     * If there is none, looks in allServicesAndBeans.
     * Returns the type or null if the type is not found.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> objType) {
        return (T) this.platformBeans.stream().filter(pb -> objType.isAssignableFrom(pb.getClass()))
                .findFirst().orElse(this.allServicesAndBeans.stream()
                        .filter(sb -> objType.isAssignableFrom(sb.getClass())).findFirst().orElse(null)
                );
    }

    @Override
    public Set<Object> getServicesAndBeans() {
        return this.allServicesAndBeans;
    }

    @Override
    public Set<Object> getServicesByAnnotation(Class<? extends Annotation> annotationType) {
        return this.allServicesAndBeans.stream()
                .filter(s -> s.getClass().isAnnotationPresent(annotationType))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Object> getPlatformBeans() {
        return this.platformBeans;
    }

    /**
     * Checks if there is an already loaded service. If there is, return it.
     * Otherwise get the first constructor and collect its parameters (if any)
     * return an instance of the service type.
     */
    private Object loadService(Class<?> serviceClass, ServiceLifeSpan serviceLifeSpan) throws ServiceLoadException {
        Object alreadyLoadedService = this.findAlreadyLoadedService(serviceClass);
        if (alreadyLoadedService != null) {
            return alreadyLoadedService;
        }
        Constructor<?> constructor = serviceClass.getConstructors()[0];
        if (constructor.getParameterCount() < 1) {
            return this.instantiateService(constructor);
        }
        Object[] paramInstances = new Object[constructor.getParameterCount()];
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            paramInstances[i] = this.findAssignableService(paramTypes[i], serviceLifeSpan);
        }
        return this.instantiateService(constructor, paramInstances);
    }

    /**
     * Looks for assignable object for a given parameter.
     * First checks the already loaded services.
     * If there is no assignable object in the already loaded services, checks
     * in the cached types. If a class is found, load that class and return it.
     */
    private Object findAssignableService(Class<?> param, ServiceLifeSpan lifeSpan) throws ServiceLoadException {
        for (Object service : this.allServicesAndBeans) {
            if (param.isAssignableFrom(service.getClass())) {
                return service;
            }
        }
        for (Class<?> cls : this.cachedClassesByLifeSpan.get(lifeSpan)) {
            if (param.isAssignableFrom(cls)) {
                return this.loadService(cls, lifeSpan);
            }
        }
        return null; //This should only be reached by platform services
    }

    /**
     * Iterate loaded services/beans and find assignable object for a given type.
     */
    private Object findAlreadyLoadedService(Class<?> serviceClass) {
        return this.allServicesAndBeans.stream()
                .filter(s -> serviceClass.isAssignableFrom(s.getClass()))
                .findFirst().orElse(null);
    }

    /**
     * Instantiate class with given constructor and optional parameters.
     */
    private Object instantiateService(Constructor<?> serviceConstructor, Object... params) throws ServiceLoadException {
        try {
            Object service = serviceConstructor.newInstance(params);
            this.allServicesAndBeans.add(service);
            return service;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ServiceLoadException(e.getMessage(), e);
        }
    }

    /**
     * Adds services to a map with key of ServiceLifeSpan and returns the matching classes.
     * If the check has been performed, returns the value of the map to enhance performance.
     */
    private List<Class<?>> getOrCacheClassesByLifespan(ServiceLifeSpan lifeSpan) {
        if (this.cachedClassesByLifeSpan.containsKey(lifeSpan)) {
            return this.cachedClassesByLifeSpan.get(lifeSpan);
        }
        List<Class<?>> classes = new ArrayList<>();
        for (Object service : this.allServicesAndBeans) {
            if (this.isServiceToBeReloaded(service.getClass(), lifeSpan)) {
                classes.add(service.getClass());
            }
        }
        this.cachedClassesByLifeSpan.put(lifeSpan, classes);
        return classes;
    }

    /**
     * Checks if serviceType contains annotation @Service and if it has, compares
     * the lifeSpan with the given one.
     */
    private boolean isServiceToBeReloaded(Class<?> serviceClass, ServiceLifeSpan lifeSpan) {
        if (!serviceClass.isAnnotationPresent(Service.class)) {
            return false;
        }
        Service serviceAnnotation = serviceClass.getAnnotation(Service.class);
        return serviceAnnotation.lifespan() == lifeSpan;
    }
}
