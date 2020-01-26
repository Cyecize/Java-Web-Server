package com.cyecize.summer.areas.startup.util;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningServiceImpl;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.areas.startup.models.SummerAppContext;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.common.annotations.Controller;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class SummerAppContextProducer {

    public static SummerAppContext createAppContext(DependencyContainer dependencyContainer) {
        final Collection<ServiceDetails> controllers = dependencyContainer.getServicesByAnnotation(Controller.class);

        final Map<String, Set<ActionMethod>> actionsByMethod = new ActionMethodScanningServiceImpl(new PathFormatter())
                .findActionMethods(controllers);

        return new SummerAppContext(dependencyContainer, actionsByMethod);
    }
}
