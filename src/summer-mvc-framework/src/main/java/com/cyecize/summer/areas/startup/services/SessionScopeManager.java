package com.cyecize.summer.areas.startup.services;

import com.cyecize.solet.HttpSoletRequest;

public interface SessionScopeManager {

    void initialize(DependencyContainer dependencyContainer);

    void setSessionScopedServices(HttpSoletRequest request);
}
