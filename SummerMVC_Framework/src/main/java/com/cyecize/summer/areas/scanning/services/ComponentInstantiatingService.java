package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.ComponentInstantiationException;
import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface ComponentInstantiatingService {

    Set<Class<?>> findClassesByAnnotation(Class<? extends Annotation> annotation);

    Set<Object> instantiateClasses(Set<Class<?>> componentClasses) throws ComponentInstantiationException, PostConstructException;
}
