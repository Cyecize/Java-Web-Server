package com.cyecize.summer.areas.scanning.services;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.util.Collection;

public interface DependencyContainer extends com.cyecize.ioc.services.DependencyContainer {

    void reloadServices(ServiceLifeSpan lifeSpan);

    void addFlashService(Object service);

    <T> T getFlashService(Class<?> serviceType);

    void clearFlashServices();

    Collection<ServiceDetails> getServicesByLifeSpan(ServiceLifeSpan serviceLifeSpan);
}
