package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.util.Set;

public interface DependencyContainer {

    void addServices(Set<Object> services);

    void reloadServices(ServiceLifeSpan lifeSpan);

    void addPlatformBean(Object object);

    void evictPlatformBeans();

    Object reloadComponent(Object component);

    <T> T getObject(Class<T> objType);

    Set<Object> getServicesAndBeans();

    Set<Object> getPlatformBeans();
}
