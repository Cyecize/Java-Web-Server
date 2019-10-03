package com.cyecize.summer.areas.scanning.services;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ComponentLoadingService {

    Map<String, Set<Object>> getComponents(Collection<Object> components);
}
