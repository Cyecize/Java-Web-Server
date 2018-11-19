package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;
import com.cyecize.summer.common.annotations.Controller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class ControllerLoadingServiceImpl implements ControllerLoadingService {

    private static final String CANNOT_INSTANTIATE_INTERFACE = "Cannot create instance of an interface!";

    private static final String COULD_NOT_FIND_DEPENDENCY_FORMAT = "Could not find instance for dependency \"%s\".";

    private Set<Object> loadedControllers;

    private Set<Object> loadedServices;

    public ControllerLoadingServiceImpl() {
        this.loadedControllers = new HashSet<>();
    }

    @Override
    public Set<Object> loadControllers(Set<Class<?>> availableClasses, Set<Object> loadedServices) throws ControllerLoadException {
        this.loadedServices = loadedServices;
        Set<Class<?>> controllerClasses = this.findControllerClasses(availableClasses);
        for (Class<?> controllerClass : controllerClasses) {
            this.loadController(controllerClass);
        }
        return this.loadedControllers;
    }

    private void loadController(Class<?> controllerClass) throws ControllerLoadException {
        if (controllerClass.getConstructors().length < 1) {
            throw new ControllerLoadException(CANNOT_INSTANTIATE_INTERFACE);
        }

        Constructor<?> constructor = controllerClass.getConstructors()[0];
        if (constructor.getParameterCount() < 1) {
            this.instantiateController(constructor);
            return;
        }

        Object[] constructorParams = new Object[constructor.getParameterCount()];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            constructorParams[i] = this.findDependencyInstance(parameterTypes[i]);
        }
        this.instantiateController(constructor, constructorParams);
    }

    private Object findDependencyInstance(Class<?> dependency) throws ControllerLoadException {
        for (Object loadedService : this.loadedServices) {
            if (dependency.isAssignableFrom(loadedService.getClass())) {
                return loadedService;
            }
        }
        throw new ControllerLoadException(String.format(COULD_NOT_FIND_DEPENDENCY_FORMAT, dependency.getName()));
    }

    private void instantiateController(Constructor<?> constructor, Object... params) throws ControllerLoadException {
        try {
            this.loadedControllers.add(constructor.newInstance(params));
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ControllerLoadException(e.getMessage(), e);
        }
    }

    private Set<Class<?>> findControllerClasses(Set<Class<?>> availableClasses) {
        Set<Class<?>> controllerClasses = new HashSet<>();
        for (Class<?> availableClass : availableClasses) {
            if (availableClass.isAnnotationPresent(Controller.class)) {
                controllerClasses.add(availableClass);
            }
        }
        return controllerClasses;
    }

}
