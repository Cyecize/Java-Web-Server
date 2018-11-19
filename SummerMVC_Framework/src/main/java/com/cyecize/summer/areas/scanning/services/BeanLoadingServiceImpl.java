package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.BeanLoadException;
import com.cyecize.summer.common.annotations.Bean;
import com.cyecize.summer.common.annotations.BeanConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class BeanLoadingServiceImpl implements BeanLoadingService {

    private static final String INVALID_BEAN_CONSTRUCTOR = "Bean Config Classes must have public empty constructors.";

    private List<Object> loadedBeanConfigInstances;

    private Set<Object> loadedBeans;

    public BeanLoadingServiceImpl() {
        this.loadedBeanConfigInstances = new ArrayList<>();
        this.loadedBeans = new HashSet<>();
    }

    @Override
    public Set<Object> loadBeans(Set<Class<?>> availableClasses) throws BeanLoadException {
        try {
            this.findBeanConfigClasses(availableClasses);
            this.findAndLoadBeans();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new BeanLoadException(e.getMessage(), e);
        }
        return this.loadedBeans;
    }

    private void findAndLoadBeans() throws InvocationTargetException, IllegalAccessException {
        for (Object beanConfigClass : this.loadedBeanConfigInstances) {
            Method[] beanAnnotatedMethods = Arrays.stream(beanConfigClass.getClass().getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class) && m.getReturnType() != void.class && m.getReturnType() != Void.class)
                    .toArray(Method[]::new);
            for (Method beanAnnotatedMethod : beanAnnotatedMethods) {
                loadedBeans.add(beanAnnotatedMethod.invoke(beanConfigClass));
            }
        }
    }

    private void findBeanConfigClasses(Set<Class<?>> availableClasses) throws BeanLoadException {
        for (Class<?> cls : availableClasses) {
            if (cls.isAnnotationPresent(BeanConfig.class)) {
                try {
                    this.loadedBeanConfigInstances.add(cls.getConstructor().newInstance());
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    throw new BeanLoadException(INVALID_BEAN_CONSTRUCTOR, e);
                }
            }
        }
    }
}
