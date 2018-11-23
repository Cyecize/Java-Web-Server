package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.BeanLoadException;
import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;

import java.util.Set;

public interface BeanLoadingService {
    Set<Object> loadBeans(Set<Class<?>> availableClasses) throws BeanLoadException, PostConstructException;
}
