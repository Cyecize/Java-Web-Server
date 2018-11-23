package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;

import java.util.Set;

public interface ServiceLoadingService {
    Set<Object> loadServices(Set<Object> beans, Set<Class<?>> availableClasses) throws ServiceLoadException, PostConstructException;
}
