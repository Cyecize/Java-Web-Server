package com.cyecize.summer.areas.startup.models;

import com.cyecize.summer.areas.routing.models.ActionMethod;

import java.util.Map;
import java.util.Set;

public class ScannedObjects {

    private final Map<Class<?>, Object> loadedControllers;

    private final Map<String, Set<ActionMethod>> actionsByMethod;

    public ScannedObjects(Map<Class<?>, Object> loadedControllers, Map<String, Set<ActionMethod>> actionsByMethod) {
        this.loadedControllers = loadedControllers;
        this.actionsByMethod = actionsByMethod;
    }

    public Map<Class<?>, Object> getLoadedControllers() {
        return loadedControllers;
    }

    public Map<String, Set<ActionMethod>> getActionsByMethod() {
        return actionsByMethod;
    }
}
