package com.cyecize.summer.areas.scanning.models;

import com.cyecize.summer.areas.routing.models.ActionMethod;

import java.util.Map;
import java.util.Set;

public class ScannedObjects {

    private Set<Class<?>> scannedClasses;

    private Set<Object> loadedServicesAndObjects;

    private Map<Class<?>, Object> loadedControllers;

    private Map<String, Set<Object>> loadedComponents;

    private Map<String, Set<ActionMethod>> actionsByMethod;

    private String workingDir;

    public ScannedObjects(Set<Class<?>> scannedClasses, Set<Object> loadedServicesAndObjects, Map<Class<?>, Object> loadedControllers, Map<String, Set<Object>> loadedComponents, Map<String, Set<ActionMethod>> actionsByMethod, String workingDir) {
        this.scannedClasses = scannedClasses;
        this.loadedServicesAndObjects = loadedServicesAndObjects;
        this.loadedControllers = loadedControllers;
        this.loadedComponents = loadedComponents;
        this.actionsByMethod = actionsByMethod;
        this.workingDir = workingDir;
    }

    public Set<Class<?>> getScannedClasses() {
        return scannedClasses;
    }

    public void setScannedClasses(Set<Class<?>> scannedClasses) {
        this.scannedClasses = scannedClasses;
    }

    public Set<Object> getLoadedServicesAndObjects() {
        return loadedServicesAndObjects;
    }

    public void setLoadedServicesAndObjects(Set<Object> loadedServicesAndObjects) {
        this.loadedServicesAndObjects = loadedServicesAndObjects;
    }

    public Map<Class<?>, Object> getLoadedControllers() {
        return loadedControllers;
    }

    public void setLoadedControllers(Map<Class<?>, Object> loadedControllers) {
        this.loadedControllers = loadedControllers;
    }

    public Map<String, Set<Object>> getLoadedComponents() {
        return loadedComponents;
    }

    public void setLoadedComponents(Map<String, Set<Object>> loadedComponents) {
        this.loadedComponents = loadedComponents;
    }

    public Map<String, Set<ActionMethod>> getActionsByMethod() {
        return actionsByMethod;
    }

    public void setActionsByMethod(Map<String, Set<ActionMethod>> actionsByMethod) {
        this.actionsByMethod = actionsByMethod;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }
}
