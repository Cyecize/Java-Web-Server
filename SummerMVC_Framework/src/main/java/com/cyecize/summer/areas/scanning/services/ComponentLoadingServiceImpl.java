package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ComponentInstantiationException;
import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.extensions.InterceptorAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.cyecize.summer.constants.IocConstants.COMPONENT_MAP_DATA_ADAPTERS;
import static com.cyecize.summer.constants.IocConstants.COMPONENT_MAP_INTERCEPTORS;
import static com.cyecize.summer.constants.IocConstants.COMPONENT_MAP_VALIDATORS;

public class ComponentLoadingServiceImpl implements ComponentLoadingService {

    private final ComponentInstantiatingService componentInstantiatingService;

    private Map<String, Set<Object>> components;

    public ComponentLoadingServiceImpl(ComponentInstantiatingService componentInstantiatingService) {
        this.componentInstantiatingService = componentInstantiatingService;
        this.components = new HashMap<>();
        this.components.put(COMPONENT_MAP_INTERCEPTORS, new HashSet<>());
        this.components.put(COMPONENT_MAP_VALIDATORS, new HashSet<>());
        this.components.put(COMPONENT_MAP_DATA_ADAPTERS, new HashSet<>());
    }

    /**
     * Finds and loads all @Component annotated classes.
     * Iterates the collection and adds each component to its group based on its type.
     * Returns a map where the key is the type of component and the value is a set of components.
     */
    @Override
    public Map<String, Set<Object>> getComponents() throws ServiceLoadException, PostConstructException {
        try {
            Set<Object> components = this.componentInstantiatingService.instantiateClasses(this.componentInstantiatingService.findClassesByAnnotation(Component.class));
            for (Object component : components) {
                this.handleComponentType(component);
            }
        } catch (ComponentInstantiationException e) {
            throw new ServiceLoadException(e.getMessage(), e);
        }
        return this.components;
    }

    /**
     * Checks if object is of a certain type of component and adds it
     * to the map if it is a supported one.
     */
    private void handleComponentType(Object component) {
        if (component instanceof InterceptorAdapter) {
            this.components.get(COMPONENT_MAP_INTERCEPTORS).add(component);
        }
        if (component instanceof ConstraintValidator) {
            this.components.get(COMPONENT_MAP_VALIDATORS).add(component);
        }
        if (component instanceof DataAdapter) {
            this.components.get(COMPONENT_MAP_DATA_ADAPTERS).add(component);
        }
        //add more here
    }
}
