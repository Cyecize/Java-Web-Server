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

    /**
     * Tries to find and load beans.
     *
     * @throws BeanLoadException if something happens.
     */
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

    /**
     * Iterates all loaded bean config classes and scans them for @Bean annotated methods.
     * Iterates those methods and adds the result of the invoked method to the loadedBeans list.
     *
     * @throws InvocationTargetException if the method has any parameters.
     */
    private void findAndLoadBeans() throws InvocationTargetException, IllegalAccessException {
        for (Object beanConfigClass : this.loadedBeanConfigInstances) {
            Method[] beanAnnotatedMethods = Arrays.stream(beanConfigClass.getClass().getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(Bean.class) && m.getReturnType() != void.class && m.getReturnType() != Void.class)
                    .toArray(Method[]::new);
            for (Method beanAnnotatedMethod : beanAnnotatedMethods) {
                this.loadedBeans.add(beanAnnotatedMethod.invoke(beanConfigClass));
            }
        }
    }

    /**
     * Iterates all available classes and filters those with @BeanConfig annotation.
     * Tries to instantiate the class and add it to the list of loaded beans.
     *
     * @throws BeanLoadException if the constructor is invalid.
     */
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
