package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ComponentInstantiationException;
import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.extensions.InterceptorAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.cyecize.summer.constants.IocConstants.COMPONENT_MAP_INTERCEPTORS;

public class ComponentLoadingServiceImpl implements ComponentLoadingService {

    private final ComponentInstantiatingService componentInstantiatingService;

    private Map<String, Set<Object>> components;

    public ComponentLoadingServiceImpl(ComponentInstantiatingService componentInstantiatingService) {
        this.componentInstantiatingService = componentInstantiatingService;
        this.components = new HashMap<>();
        this.components.put(COMPONENT_MAP_INTERCEPTORS, new HashSet<>());
    }

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
        //add more here
    }
}
