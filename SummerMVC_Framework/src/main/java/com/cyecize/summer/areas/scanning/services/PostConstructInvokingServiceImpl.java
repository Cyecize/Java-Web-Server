package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;
import com.cyecize.summer.common.annotations.PostConstruct;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

public class PostConstructInvokingServiceImpl implements PostConstructInvokingService {

    /**
     * Iterates a collection of instantiated objects and for each one gets all postConstruct methods.
     * Then iterates all postConstruct methods and invokes them.
     */
    @Override
    public void invokePostConstructMethod(Collection<Object> instances) throws PostConstructException {
        for (Object instance : instances) {
            Method[] methods = this.findPostConstructMethodsForClass(instance.getClass());
            for (Method method : methods) {
                this.invokeMethod(method, instance);
            }
        }
    }

    /**
     * Finds methods in a class where @PostConstruct annotation is present, there are no parameters and the return type is void.
     */
    private Method[] findPostConstructMethodsForClass(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(PostConstruct.class) && m.getParameterCount() == 0 && (m.getReturnType() == void.class || m.getReturnType() == Void.class))
                .toArray(Method[]::new);
    }

    /**
     * Invokes a method.
     *
     * @throws PostConstructException if error occurs inside the post construct method.
     */
    private void invokeMethod(Method method, Object instance) throws PostConstructException {
        method.setAccessible(true);
        try {
            method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PostConstructException(e.getMessage(), e);
        }
    }
}
