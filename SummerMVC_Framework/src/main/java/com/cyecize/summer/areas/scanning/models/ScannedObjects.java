package com.cyecize.summer.areas.scanning.models;

import com.cyecize.summer.areas.routing.models.ActionMethod;

import java.util.Map;
import java.util.Set;

public class ScannedObjects {

    private Map<Class<?>, Object> loadedControllers;

    private Map<String, Set<ActionMethod>> actionsByMethod;

    private String workingDir;

    public ScannedObjects(Map<Class<?>, Object> loadedControllers, Map<String, Set<ActionMethod>> actionsByMethod, String workingDir) {
        this.loadedControllers = loadedControllers;
        this.actionsByMethod = actionsByMethod;
        this.workingDir = workingDir;
    }

    public Map<Class<?>, Object> getLoadedControllers() {
        return loadedControllers;
    }

    public Map<String, Set<ActionMethod>> getActionsByMethod() {
        return actionsByMethod;
    }

    public String getWorkingDir() {
        return workingDir;
    }
}
