package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;
import com.cyecize.summer.common.extensions.InterceptorAdapter;

import java.util.*;

import static com.cyecize.summer.constants.IocConstants.COMPONENT_MAP_DATA_ADAPTERS;
import static com.cyecize.summer.constants.IocConstants.COMPONENT_MAP_INTERCEPTORS;
import static com.cyecize.summer.constants.IocConstants.COMPONENT_MAP_VALIDATORS;

public class ComponentLoadingServiceImpl implements ComponentLoadingService {

    private Map<String, Set<Object>> components;

    public ComponentLoadingServiceImpl() {
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
    public Map<String, Set<Object>> getComponents(Collection<Object> components) {
        for (Object component : components) {
            this.handleComponentType(component);
        }

        return this.components;
    }

    /**
     * Checks if object is of a certain type of component and adds it
     * to the map if it is a supported one.
     */
    //TODO refactor
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
