package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;

import java.util.Map;

public interface ControllerLoadingService {
    Map<Class<?>,Object> loadControllers() throws ControllerLoadException;
}
