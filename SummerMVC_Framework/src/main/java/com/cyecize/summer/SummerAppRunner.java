package com.cyecize.summer;

import com.cyecize.ioc.MagicInjector;
import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.ioc.handlers.DependencyResolver;
import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.solet.HttpSolet;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningService;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningServiceImpl;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.areas.startup.callbacks.ComponentScopeHandler;
import com.cyecize.summer.areas.startup.models.ScannedObjects;
import com.cyecize.summer.areas.startup.models.SummerAppContext;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.startup.services.DependencyContainerImpl;
import com.cyecize.summer.areas.startup.util.MagicConfigurationProducer;
import com.cyecize.summer.common.annotations.Controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SummerAppRunner {

    public static SummerAppContext run(Class<? extends HttpSolet> startupSolet, DependencyResolver... dependencyResolvers) {
        final ActionMethodScanningService methodScanningService = new ActionMethodScanningServiceImpl(new PathFormatter());

        final MagicConfiguration magicConfiguration = MagicConfigurationProducer.getConfiguration(
                startupSolet,
                List.of(dependencyResolvers),
                List.of(new ComponentScopeHandler())
        );

        final DependencyContainer dependencyContainer = new DependencyContainerImpl(
                MagicInjector.run(startupSolet, magicConfiguration)
        );

        //TODO add service for those
        final Map<Class<?>, Object> loadedControllers = dependencyContainer.getServicesByAnnotation(Controller.class)
                .stream()
                .collect(Collectors.toMap(ServiceDetails::getServiceType, ServiceDetails::getInstance));

        final Map<String, Set<ActionMethod>> actionsByMethod = methodScanningService.findActionMethods(loadedControllers);

        final ScannedObjects scannedObjects = new ScannedObjects(
                loadedControllers,
                actionsByMethod
        );

        return new SummerAppContext(dependencyContainer, scannedObjects);
    }
}
