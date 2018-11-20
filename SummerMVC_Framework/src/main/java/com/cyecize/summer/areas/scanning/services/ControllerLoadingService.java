package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;

import java.util.Map;
import java.util.Set;


public interface ControllerLoadingService {
    Map<Class<?>,Object> loadControllers(Set<Class<?>> availableClasses, Set<Object> loadedServices) throws ControllerLoadException;
}
