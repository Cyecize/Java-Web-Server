package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface DependencyContainer extends com.cyecize.ioc.services.DependencyContainer {

    void reloadServices(ServiceLifeSpan lifeSpan);

    void addFlashService(Object service);

    <T> T getFlashService(Class<?> serviceType);

    void clearFlashServices();
}
