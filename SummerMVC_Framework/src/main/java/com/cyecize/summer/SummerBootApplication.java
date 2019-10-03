package com.cyecize.summer;

import com.cyecize.ioc.MagicInjector;
import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningService;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningServiceImpl;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.areas.scanning.exceptions.*;
import com.cyecize.summer.areas.scanning.models.ScannedObjects;
import com.cyecize.summer.areas.scanning.services.*;
import com.cyecize.summer.areas.template.annotations.TemplateService;
import com.cyecize.summer.common.annotations.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SummerBootApplication {

    public static DependencyContainer dependencyContainer = new DependencyContainerImpl();

    public static <T extends DispatcherSolet> void run(T startupSolet, Consumer<Collection<Class<?>>> loadedClassesHandler) {
        ActionMethodScanningService methodScanningService = new ActionMethodScanningServiceImpl(new PathFormatter());

        MagicConfiguration configuration = new MagicConfiguration()
                .scanning()
                .addCustomBeanAnnotation(Bean.class)
                .addCustomServiceAnnotations(Set.of(Service.class, Component.class, /*TemplateService.class,*/ BeanConfig.class, Controller.class))
                .and()
                .build();

        var container = MagicInjector.run(startupSolet.getClass(), configuration);

        try {
            Collection<Class<?>> loadedClasses = container.getAllScannedClasses();
            Set<Object> loadedServicesAndBeans = container.getAllServices()
                    .stream()
                    .map(ServiceDetails::getActualInstance)
                    .collect(Collectors.toSet());


            Map<Class<?>, Object> loadedControllers = container.getServicesByAnnotation(Controller.class)
                    .stream()
                    .collect(Collectors.toMap(ServiceDetails::getServiceType, ServiceDetails::getActualInstance));

            Map<String, Set<Object>> loadedComponents = new ComponentLoadingServiceImpl().getComponents(
                    container.getServicesByAnnotation(Component.class).stream()
                            .map(ServiceDetails::getActualInstance)
                            .collect(Collectors.toSet())
            );

            Map<String, Set<ActionMethod>> actionsByMethod = methodScanningService.findActionMethods(loadedControllers);

            dependencyContainer.addServices(loadedServicesAndBeans);

            if (loadedClassesHandler != null) {
                loadedClassesHandler.accept(loadedClasses);
            }

            startupSolet.initSummerBoot(new ScannedObjects(
                    new HashSet<>(loadedClasses),
                    loadedServicesAndBeans,
                    loadedControllers,
                    loadedComponents,
                    actionsByMethod,
                    startupSolet.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().substring(1)
            ));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static <T extends DispatcherSolet> void run(T startupSolet) {
        run(startupSolet, null);
    }
}
