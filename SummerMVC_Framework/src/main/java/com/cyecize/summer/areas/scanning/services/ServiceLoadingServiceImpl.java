package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.common.annotations.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ServiceLoadingServiceImpl implements ServiceLoadingService {

    private static final String COULD_NOT_FIND_DEPENDENCY_FORMAT = "Could not find dependency \"%s\".";

    private static final String COULD_NOT_FIND_ASSIGNABLE_FORMAT = "Could not find assignable class for \"%s\".";

    private final PostConstructInvokingService constructInvokingService;

    private Map<Class<?>, Object> loadedInstances;

    private List<Class<?>> availableServices;

    public ServiceLoadingServiceImpl(PostConstructInvokingService constructInvokingService) {
        this.constructInvokingService = constructInvokingService;
        this.loadedInstances = new HashMap<>();
    }

    @Override
    public Set<Object> loadServices(Set<Object> beans, Set<Class<?>> availableClasses) throws ServiceLoadException, PostConstructException {
        this.includeBeansToCurrentServices(beans);
        Set<Object> loadedServices = new HashSet<>();
        this.findServiceClasses(availableClasses);
        try {
            for (Class<?> availableService : this.availableServices) {
                loadedServices.add(this.loadService(availableService));
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw new ServiceLoadException(e.getMessage(), e);
        }
        this.constructInvokingService.invokePostConstructMethod(loadedServices);
        loadedServices.addAll(beans);
        return loadedServices;
    }

    private void includeBeansToCurrentServices(Set<Object> beans) {
        for (Object bean : beans) {
            this.loadedInstances.put(bean.getClass(), bean);
        }
    }

    private Object loadService(Class<?> serviceCls) throws IllegalAccessException, InvocationTargetException, InstantiationException, ServiceLoadException, NoSuchMethodException {
        Object alreadyLoadedService = this.findAlreadyLoadedService(serviceCls);
        if (alreadyLoadedService != null) {
            return alreadyLoadedService;
        }
        serviceCls = this.findAssignableClass(serviceCls);
        if (serviceCls.getDeclaredConstructors().length < 1) {
            return this.instantiateService(serviceCls, serviceCls.getConstructor());
        }
        Constructor<?> serviceConstructor = serviceCls.getConstructors()[0];

        Class<?>[] parameterTypes = serviceConstructor.getParameterTypes();
        Object[] parameterInstances = new Object[serviceConstructor.getParameterCount()];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            Object paramInstance = this.loadService(paramType);
            if (paramInstance == null) {
                throw new ServiceLoadException(String.format(COULD_NOT_FIND_DEPENDENCY_FORMAT, paramType.getName()));
            }
            parameterInstances[i] = paramInstance;
        }
        return this.instantiateService(serviceCls, serviceConstructor, parameterInstances);
    }

    private Object findAlreadyLoadedService(Class<?> serviceCls) {
        for (Map.Entry<Class<?>, Object> serviceKvp : this.loadedInstances.entrySet()) {
            if (serviceCls.isAssignableFrom(serviceKvp.getKey())) {
                return serviceKvp.getValue();
            }
        }
        return null;
    }

    private Class<?> findAssignableClass(Class<?> target) throws ServiceLoadException {
        for (Class<?> availableService : this.availableServices) {
            if (target.isAssignableFrom(availableService)) {
                return availableService;
            }
        }
        throw new ServiceLoadException(String.format(COULD_NOT_FIND_ASSIGNABLE_FORMAT, target.getName()));
    }

    private Object instantiateService(Class cls, Constructor constructor, Object... params) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object instance = constructor.newInstance(params);
        this.loadedInstances.put(cls, instance);
        return instance;
    }

    private void findServiceClasses(Set<Class<?>> availableClasses) {
        this.availableServices = new ArrayList<>();
        for (Class<?> cls : availableClasses) {
            if (cls.isAnnotationPresent(Service.class)) {
                this.availableServices.add(cls);
            }
        }
    }
}
