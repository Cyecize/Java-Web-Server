package com.cyecize.summer;

import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningService;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningServiceImpl;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.areas.scanning.exceptions.*;
import com.cyecize.summer.areas.scanning.models.ScannedObjects;
import com.cyecize.summer.areas.scanning.services.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class SummerBootApplication {

    public static DependencyContainer dependencyContainer = new DependencyContainerImpl();

    public static <T extends DispatcherSolet> void run(T startupSolet, Consumer<Collection<Class<?>>> loadedClassesHandler) {
        FileScanService fileScanService = new FileScanServiceImpl(startupSolet.getClass());
        BeanLoadingService beanLoadingService = new BeanLoadingServiceImpl();
        ServiceLoadingService serviceLoadingService = new ServiceLoadingServiceImpl();
        ActionMethodScanningService methodScanningService = new ActionMethodScanningServiceImpl(new PathFormatter());

        try {
            Set<Class<?>> loadedClasses = fileScanService.scanFiles();
            Set<Object> loadedBeans = beanLoadingService.loadBeans(loadedClasses);
            Set<Object> loadedServicesAndBeans = serviceLoadingService.loadServices(loadedBeans, loadedClasses);

            ComponentInstantiatingService componentInstantiatingService = new ComponentInstantiatingServiceImpl(loadedServicesAndBeans, loadedClasses);
            ControllerLoadingService controllerLoadingService = new ControllerLoadingServiceImpl(componentInstantiatingService);
            ComponentLoadingService componentLoadingService = new ComponentLoadingServiceImpl(componentInstantiatingService);

            Map<Class<?>, Object> loadedControllers = controllerLoadingService.loadControllers();
            Map<String, Set<Object>> loadedComponents = componentLoadingService.getComponents();
            Map<String, Set<ActionMethod>> actionsByMethod = methodScanningService.findActionMethods(loadedControllers);

            dependencyContainer.addServices(loadedServicesAndBeans);

            if (loadedClassesHandler != null) {
                loadedClassesHandler.accept(loadedClasses);
            }

            startupSolet.initSummerBoot(new ScannedObjects(loadedClasses, loadedServicesAndBeans, loadedControllers, loadedComponents, actionsByMethod, fileScanService.getAppRootDir()));

            loadedClasses = null;
            loadedBeans = null;
            componentInstantiatingService = null;
            componentLoadingService = null;
            controllerLoadingService = null;
        } catch (FileScanException | ServiceLoadException | BeanLoadException | ControllerLoadException | PostConstructException ex) {
            ex.printStackTrace();
        } finally {
            fileScanService = null;
            beanLoadingService = null;
            serviceLoadingService = null;
            methodScanningService = null;
        }
    }

    public static <T extends DispatcherSolet> void run(T startupSolet) {
        run(startupSolet, null);
    }
}
