package com.cyecize.summer;

import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConfigImpl;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningService;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningServiceImpl;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.areas.scanning.exceptions.BeanLoadException;
import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;
import com.cyecize.summer.areas.scanning.exceptions.FileScanException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.areas.scanning.services.*;

import java.util.Map;
import java.util.Set;

import static com.cyecize.summer.constants.IocConstants.*;

public class SummerBootApplication {

    public static <T extends DispatcherSolet> void run(T startupSolet) {
        FileScanService fileScanService = new FileScanServiceImpl(startupSolet.getClass());
        BeanLoadingService beanLoadingService = new BeanLoadingServiceImpl();
        ServiceLoadingService serviceLoadingService = new ServiceLoadingServiceImpl();
        ControllerLoadingService controllerLoadingService = new ControllerLoadingServiceImpl();
        ActionMethodScanningService methodScanningService = new ActionMethodScanningServiceImpl(new PathFormatter());
        try {
            Set<Class<?>> loadedClasses = fileScanService.scanFiles();
            Set<Object> loadedBeans = beanLoadingService.loadBeans(loadedClasses);
            Set<Object> loadedServicesAndBeans = serviceLoadingService.loadServices(loadedBeans, loadedClasses);
            Map<Class<?>, Object> loadedControllers = controllerLoadingService.loadControllers(loadedClasses, loadedServicesAndBeans);
            Map<String, Set<ActionMethod>> actionsByMethod = methodScanningService.findActionMethods(loadedControllers);
            actionsByMethod.forEach((k,v) -> {
                System.out.println("For method " + k);
                v.forEach(a -> {
                    System.out.println("----- " + a.getPattern());
                });
            });

            SoletConfig soletConfig = new SoletConfigImpl();
            soletConfig.setAttribute(SOLET_CFG_LOADED_SERVICES_AND_BEANS_NAME, loadedServicesAndBeans);
            soletConfig.setAttribute(SOLET_CFG_LOADED_CONTROLLERS_NAME, loadedControllers);
            soletConfig.setAttribute(SOLET_CFG_LOADED_ACTIONS, actionsByMethod);
            startupSolet.init(soletConfig);
            loadedClasses = null;
            loadedBeans = null;
        } catch (FileScanException | ServiceLoadException | BeanLoadException | ControllerLoadException ex) {
            ex.printStackTrace();
        } finally {
            fileScanService = null;
            beanLoadingService = null;
            serviceLoadingService = null;
            controllerLoadingService = null;
            methodScanningService = null;
            System.gc();
        }
    }
}
