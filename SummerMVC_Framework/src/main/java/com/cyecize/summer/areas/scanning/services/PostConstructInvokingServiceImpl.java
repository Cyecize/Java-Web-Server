package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;
import com.cyecize.summer.common.annotations.PostConstruct;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

public class PostConstructInvokingServiceImpl implements PostConstructInvokingService {


    @Override
    public void invokePostConstructMethod(Collection<Object> instances) throws PostConstructException {
        for (Object instance : instances) {
            Method[] methods = this.findPostConstructMethodsForClass(instance.getClass());
            for (Method method : methods) {
                this.invokeMethod(method, instance);
            }
        }
    }

    private Method[] findPostConstructMethodsForClass(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(PostConstruct.class) && m.getParameterCount() == 0 && (m.getReturnType() == void.class || m.getReturnType() == Void.class))
                .toArray(Method[]::new);
    }

    private void invokeMethod(Method method, Object instance) throws PostConstructException {
        method.setAccessible(true);
        try {
            method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new PostConstructException(e.getMessage(), e);
        }
    }
}
