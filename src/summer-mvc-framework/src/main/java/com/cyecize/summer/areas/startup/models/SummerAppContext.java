package com.cyecize.summer.areas.startup.models;

import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.startup.services.DependencyContainer;

import java.util.Map;
import java.util.Set;

public class SummerAppContext {

    private final DependencyContainer dependencyContainer;

    private final Map<String, Set<ActionMethod>> actionsByMethod;

    public SummerAppContext(DependencyContainer dependencyContainer, Map<String, Set<ActionMethod>> actionsByMethod) {
        this.dependencyContainer = dependencyContainer;
        this.actionsByMethod = actionsByMethod;
    }

    public DependencyContainer getDependencyContainer() {
        return this.dependencyContainer;
    }

    public Map<String, Set<ActionMethod>> getActionsByMethod() {
        return this.actionsByMethod;
    }
}
