package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface DependencyContainer {

    void addServices(Set<Object> services);

    void reloadServices(ServiceLifeSpan lifeSpan);

    void addPlatformBean(Object object);

    void evictPlatformBeans();

    <T> T reloadComponent(T component);

    <T> T reloadComponent(T component, ServiceLifeSpan lifeSpan);

    <T> T getObject(Class<T> objType);

    Set<Object> getServicesAndBeans();

    Set<Object> getServicesByAnnotation(Class<? extends Annotation> annotationType);

    Set<Object> getPlatformBeans();
}
