package com.cyecize.summer.areas.routing.services;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.areas.routing.models.ActionMethod;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface ActionMethodScanningService {
    Map<String, Set<ActionMethod>> findActionMethods(Collection<ServiceDetails> controllers);
}
