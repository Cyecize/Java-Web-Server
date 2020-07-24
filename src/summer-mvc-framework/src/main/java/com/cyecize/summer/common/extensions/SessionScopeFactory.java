package com.cyecize.summer.common.extensions;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.startup.services.DependencyContainer;

public interface SessionScopeFactory<T> {

    T getInstance(ServiceDetails serviceDetails, HttpSoletRequest request, DependencyContainer dependencyContainer);
}
