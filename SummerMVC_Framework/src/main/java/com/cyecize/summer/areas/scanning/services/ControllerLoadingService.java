package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;
import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;

import java.util.Map;

public interface ControllerLoadingService {
    Map<Class<?>,Object> loadControllers() throws ControllerLoadException, PostConstructException;
}
