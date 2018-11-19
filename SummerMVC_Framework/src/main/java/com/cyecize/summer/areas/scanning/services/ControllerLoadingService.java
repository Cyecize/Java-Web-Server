package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;

import java.util.Set;

public interface ControllerLoadingService {
    Set<Object> loadControllers(Set<Class<?>> availableClasses, Set<Object> loadedServices) throws ControllerLoadException;
}
