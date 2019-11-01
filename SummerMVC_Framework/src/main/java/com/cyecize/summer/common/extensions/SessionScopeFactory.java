package com.cyecize.summer.common.extensions;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;

public interface SessionScopeFactory<T> {

    T getInstance(Class<?> serviceType, HttpSoletRequest request, DependencyContainer dependencyContainer);
}
