package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.common.annotations.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ServiceLoadingServiceImpl implements ServiceLoadingService {

    private static final String COULD_NOT_FIND_DEPENDENCY_FORMAT = "Could not find dependency \"%s\".";

    private static final String COULD_NOT_FIND_ASSIGNABLE_FORMAT = "Could not find assignable class for \"%s\".";

    private Map<Class<?>, Object> loadedInstances;

    private List<Class<?>> availableServices;

    public ServiceLoadingServiceImpl() {
        this.loadedInstances = new HashMap<>();
    }

    /**
     * Includes the beans to the map of loaded services/beans.
     * Filters out services from all available classes.
     * Iterates all available service type classes and creates instances of those classes.
     * Returns a set of all instantiated services and beans.
     */
    @Override
    public Set<Object> loadServices(Set<Object> beans, Set<Class<?>> availableClasses) throws ServiceLoadException {
        Set<Object> loadedServices = new HashSet<>();

        this.includeBeansToCurrentServices(beans);
        this.findServiceClasses(availableClasses);

        try {
            for (Class<?> availableService : this.availableServices) {
                loadedServices.add(this.loadService(availableService));
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw new ServiceLoadException(e.getMessage(), e);
        }
        loadedServices.addAll(beans);

        this.loadedInstances = null;
        return loadedServices;
    }

    /**
     * Iterates the set of beans and adds each bean to the map of loaded instances
     * where the key is the type of the bean.
     */
    private void includeBeansToCurrentServices(Set<Object> beans) {
        for (Object bean : beans) {
            this.loadedInstances.put(bean.getClass(), bean);
        }
    }

    /**
     * Checks if the service has already been loaded (as a parameter to another service) and returns the
     * object if that is the case.
     * Gets the first constructor and the service and collects its parameters.
     * For each parameter, it calls loadService recursively and throws ServiceLoadException if
     * the parameter is null.
     * <p>
     * Returns instance of the given type.
     */
    private Object loadService(Class<?> serviceCls) throws IllegalAccessException, InvocationTargetException, InstantiationException, ServiceLoadException, NoSuchMethodException {
        Object alreadyLoadedService = this.findAlreadyLoadedService(serviceCls);
        if (alreadyLoadedService != null) {
            return alreadyLoadedService;
        }

        //in case if serviceCls being interface, find the assignable implementation
        serviceCls = this.findAssignableClass(serviceCls);

        if (serviceCls.getDeclaredConstructors().length < 1) {
            return this.instantiateService(serviceCls, serviceCls.getConstructor());
        }

        Constructor<?> serviceConstructor = serviceCls.getConstructors()[0];

        Class<?>[] parameterTypes = serviceConstructor.getParameterTypes();
        Object[] parameterInstances = new Object[serviceConstructor.getParameterCount()];
        for (int i = 0; i < parameterTypes.length; i++) {

            Class<?> paramType = parameterTypes[i];
            Object paramInstance = this.loadService(paramType); //possible infinite loop
            if (paramInstance == null) {
                throw new ServiceLoadException(String.format(COULD_NOT_FIND_DEPENDENCY_FORMAT, paramType.getName()));
            }
            parameterInstances[i] = paramInstance;

        }
        return this.instantiateService(serviceCls, serviceConstructor, parameterInstances);
    }

    /**
     * Searches for a service that might have already been loaded.
     * Returns null if no service was found.
     */
    private Object findAlreadyLoadedService(Class<?> serviceCls) {
        for (Map.Entry<Class<?>, Object> serviceKvp : this.loadedInstances.entrySet()) {
            if (serviceCls.isAssignableFrom(serviceKvp.getKey())) {
                return serviceKvp.getValue();
            }
        }
        return null;
    }

    /**
     * Iterates available services and returns a class that is assignable from the given type.
     *
     * @throws ServiceLoadException if no assignable class is found.
     */
    private Class<?> findAssignableClass(Class<?> target) throws ServiceLoadException {
        for (Class<?> availableService : this.availableServices) {
            if (target.isAssignableFrom(availableService)) {
                return availableService;
            }
        }
        throw new ServiceLoadException(String.format(COULD_NOT_FIND_ASSIGNABLE_FORMAT, target.getName()));
    }

    /**
     * Creates an instance of an object by a given constructor and optional parameters.
     * Puts the instance in a map where the key is the class type.
     */
    private Object instantiateService(Class cls, Constructor constructor, Object... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object instance = constructor.newInstance(params);
        this.loadedInstances.put(cls, instance);
        return instance;
    }

    /**
     * Iterates a set of classes and filters out the ones who are @Service annotated.
     * Puts those classes in a list of classes.
     */
    private void findServiceClasses(Set<Class<?>> availableClasses) {
        this.availableServices = new ArrayList<>();
        for (Class<?> cls : availableClasses) {
            if (cls.isAnnotationPresent(Service.class)) {
                this.availableServices.add(cls);
            }
        }
    }
}
