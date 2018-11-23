package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;

import java.util.Map;
import java.util.Set;

public interface ComponentLoadingService {

    Map<String, Set<Object>> getComponents() throws ServiceLoadException, PostConstructException;
}
