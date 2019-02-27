package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ComponentInstantiationException;
import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;
import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;
import com.cyecize.summer.common.annotations.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ControllerLoadingServiceImpl implements ControllerLoadingService {

    private final ComponentInstantiatingService componentInstantiatingService;

    private Map<Class<?>, Object> loadedControllers;

    public ControllerLoadingServiceImpl(ComponentInstantiatingService componentInstantiatingService) {
        this.componentInstantiatingService = componentInstantiatingService;
        this.loadedControllers = new HashMap<>();
    }

    @Override
    public Map<Class<?>, Object> loadControllers() throws ControllerLoadException {
        try {
            Set<Object> controllers = this.componentInstantiatingService.instantiateClasses(this.componentInstantiatingService.findClassesByAnnotation(Controller.class));
            for (Object controller : controllers) {
                this.loadedControllers.put(controller.getClass(), controller);
            }

            return this.loadedControllers;
        } catch (ComponentInstantiationException e) {
            throw new ControllerLoadException(e.getMessage(), e);
        }
    }

}
