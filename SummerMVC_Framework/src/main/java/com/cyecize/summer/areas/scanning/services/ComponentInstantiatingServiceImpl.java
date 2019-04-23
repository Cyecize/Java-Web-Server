package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ComponentInstantiationException;
import com.cyecize.summer.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ComponentInstantiatingServiceImpl implements ComponentInstantiatingService {

    private static final String CANNOT_INSTANTIATE_INTERFACE = "Cannot create instance of an interface!";

    private static final String COULD_NOT_FIND_DEPENDENCY_FORMAT = "Could not find instance for dependency \"%s\".";

    private final Set<Object> loadedServicesAndBeans;

    private final Set<Class<?>> loadedClasses;

    public ComponentInstantiatingServiceImpl(Set<Object> loadedClassesAndBeans, Set<Class<?>> loadedClasses) {
        this.loadedServicesAndBeans = loadedClassesAndBeans;
        this.loadedClasses = loadedClasses;
    }

    @Override
    public Set<Class<?>> findClassesByAnnotation(Class<? extends Annotation> annotation) {
        return loadedClasses.stream().filter(c -> c.isAnnotationPresent(annotation)).collect(Collectors.toSet());
    }

    /**
     * Iterates componentClasses and adds the instantiated object to a set.
     * Calls postConstruct for objects with @PostConstruct method and returns the set of loaded
     * components.
     */
    @Override
    public Set<Object> instantiateClasses(Set<Class<?>> componentClasses) throws ComponentInstantiationException {
        Set<Object> instances = new HashSet<>();
        for (Class<?> componentClass : componentClasses) {
            instances.add(this.loadComponent(componentClass));
        }

        return instances;
    }

    /**
     * Check if the class is interface and throw exception if it is.
     * Finds suitable constructor and collects its parameters if any.
     * Returns instance of the componentClass.
     */
    private Object loadComponent(Class<?> componentClass) throws ComponentInstantiationException {
        if (componentClass.isInterface()) {
            throw new ComponentInstantiationException(CANNOT_INSTANTIATE_INTERFACE);
        }

        Constructor<?> constructor = ReflectionUtils.findConstructor(componentClass);

        Object[] constructorParams = new Object[constructor.getParameterCount()];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            constructorParams[i] = this.findDependencyInstance(parameterTypes[i]);
        }

        return this.instantiateComponent(constructor, constructorParams);
    }

    /**
     * Reflection call to create an instance of an objects with optional parameters.
     */
    private Object instantiateComponent(Constructor<?> constructor, Object... params) throws ComponentInstantiationException {
        try {
            return constructor.newInstance(params);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new ComponentInstantiationException(e.getMessage(), e);
        }
    }

    /**
     * Iterates all instantiated services/beans and returns object that matches the parameter type.
     *
     * @throws ComponentInstantiationException if no parameter exists.
     */
    private Object findDependencyInstance(Class<?> dependency) throws ComponentInstantiationException {
        for (Object loadedService : this.loadedServicesAndBeans) {
            if (dependency.isAssignableFrom(loadedService.getClass())) {
                return loadedService;
            }
        }

        throw new ComponentInstantiationException(String.format(COULD_NOT_FIND_DEPENDENCY_FORMAT, dependency.getName()));
    }
}
