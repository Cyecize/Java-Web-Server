package com.cyecize.summer.areas.startup.util;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningServiceImpl;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.areas.startup.models.ScannedObjects;
import com.cyecize.summer.areas.startup.models.SummerAppContext;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.common.annotations.Controller;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class SummerAppContextProducer {

    public static SummerAppContext createAppContext(DependencyContainer dependencyContainer) {
        final Map<Class<?>, Object> loadedControllers = dependencyContainer.getServicesByAnnotation(Controller.class)
                .stream()
                .collect(Collectors.toMap(ServiceDetails::getServiceType, ServiceDetails::getInstance));

        final Map<String, Set<ActionMethod>> actionsByMethod = new ActionMethodScanningServiceImpl(new PathFormatter())
                .findActionMethods(loadedControllers);

        final ScannedObjects scannedObjects = new ScannedObjects(
                loadedControllers,
                actionsByMethod
        );

        return new SummerAppContext(dependencyContainer, scannedObjects);
    }
}
