package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.util.Set;

public interface DependencyContainer {

    void addServices(Set<Object> services);

    void reloadServices(ServiceLifeSpan lifeSpan);

    Object reloadController(Object controller);

    Set<Object> getServicesAndBeans();
}
